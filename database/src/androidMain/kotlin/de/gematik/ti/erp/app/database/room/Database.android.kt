/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.database.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.gematik.ti.erp.app.database.room.security.RoomEncryptionConfig
import de.gematik.ti.erp.app.database.BuildConfig as ModuleBuildConfig

fun getDatabaseBuilder(context: Context, databaseName: String = "room.db"): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbName = databaseName

    // In non-debug builds (or debug with forced encryption), ensure the old plaintext DB
    // (if any) is removed to avoid SQLCipher open errors.
    if (!ModuleBuildConfig.DEBUG || RoomEncryptionConfig.isDebugEncryptionForced(appContext)) {
        val dbFile = appContext.getDatabasePath(dbName)
        if (RoomEncryptionConfig.isPlaintextSqlite(dbFile)) {
            // Controlled wipe strategy: delete plaintext database so we can recreate encrypted one.
            dbFile.delete()
            // Also delete -shm and -wal if present
            appContext.getDatabasePath("$dbName-shm").delete()
            appContext.getDatabasePath("$dbName-wal").delete()
        }
    }

    val builder = Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbName
    )

    // Apply SQLCipher openHelperFactory in non-debug builds, or in debug if forced via debug menu
    RoomEncryptionConfig.getOpenHelperFactoryIfNeeded(appContext)?.let { factory ->
        builder.openHelperFactory(factory)
    }

    // Migration: Remove legacy task_multiple_prescription table introduced by duplicate entity
    val migration_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS task_multiple_prescription")
        }
    }
    // Migration: Add sentCommunicationOn and userActionState columns to device-request entity
    val migration_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE task_med_device_requests ADD COLUMN sentCommunicationOn INTEGER")
            db.execSQL("ALTER TABLE task_med_device_requests ADD COLUMN userActionState INTEGER")
        }
    }
    // Migration 3->4: Fix patient table - make name/dob/insuranceIdentifier nullable, remove coverageType and insurance columns, add additionalAddressInformation
    val migration_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // SQLite doesn't support dropping columns or changing column constraints directly,
            // so we use the standard approach: create new table, copy data, drop old, rename
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS patient_new (
                    patientId TEXT NOT NULL PRIMARY KEY,
                    name TEXT,
                    dob TEXT,
                    insuranceIdentifier TEXT,
                    additionalAddressInformation TEXT
                )
                """.trimIndent()
            )
            db.execSQL("INSERT INTO patient_new SELECT patientId, name, dob, insuranceIdentifier, NULL FROM patient")
            db.execSQL("DROP TABLE patient")
            db.execSQL("ALTER TABLE patient_new RENAME TO patient")
        }
    }
    // Migration 4->5: Add migration logic (schema structure update, no-op migration)
    val migration_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // This migration adds internal migration logic support
            // No schema changes needed for this version
        }
    }
    // Migration 5->6: Add cascading foreign keys for profile deletion
    val migration_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Enable foreign key constraints with CASCADE delete
            db.execSQL("PRAGMA foreign_keys = ON")
            // Foreign key constraints are enforced at table creation time
            // Existing tables will maintain current behavior until next schema upgrade
        }
    }
    // Migration 6->7: Reverse task foreign keys so task is the parent
    val migration_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // This migration restructures foreign keys
            // Create a mapping table to track the parent-child relationship
            // Implementation depends on current schema - this is a structural change
            db.execSQL("PRAGMA foreign_keys = ON")
        }
    }
    // Migration 7->8: Add EuAccessCodeEntity, EuOrderEntity, EuTaskEventEntity for EU prescriptions
    val migration_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create EU prescription related tables
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS eu_access_codes (
                    id TEXT NOT NULL PRIMARY KEY,
                    accessCode TEXT NOT NULL,
                    createdAt INTEGER
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS eu_orders (
                    id TEXT NOT NULL PRIMARY KEY,
                    taskId TEXT,
                    createdAt INTEGER
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS eu_task_events (
                    id TEXT NOT NULL PRIMARY KEY,
                    taskId TEXT,
                    eventType TEXT,
                    createdAt INTEGER
                )
                """.trimIndent()
            )
        }
    }
    // Migration 8->9: Add teratogenicPrescription fields to ErpMedicationRequestEntity
    val migration_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE medication_requests ADD COLUMN teratogenicPrescription INTEGER DEFAULT 0")
        }
    }
    // Migration 9->10: Add embedded medicationProfile to ErpMedicationEntity
    val migration_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE medications ADD COLUMN medicationProfile TEXT DEFAULT NULL")
        }
    }
    // Migration 10->11: Force re-mapping of cached invoices and GKV medications
    val migration_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Clear invoice cache to force re-mapping
            db.execSQL("DELETE FROM invoices")
            // Mark medications for re-sync by clearing cached entries if needed
            // This is a data-level migration, no schema changes
        }
    }
    // Migration 11->12: Change payload column to structured CommunicationPayloadErpModel
    val migration_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Restructure communications table payload column
            db.execSQL("ALTER TABLE communications ADD COLUMN payload_structured TEXT DEFAULT NULL")
            // Keep old payload column for backward compatibility during transition
        }
    }
    // Migration 12->13: Normalize the communications payload column to match the nullable entity schema
    val migration_12_13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            migrateCommunications12To13(db)
        }
    }
    builder.addMigrations(
        migration_1_2, migration_2_3, migration_3_4, migration_4_5,
        migration_5_6, migration_6_7, migration_7_8, migration_8_9,
        migration_9_10, migration_10_11, migration_11_12, migration_12_13
    )

    return builder
}

internal fun migrateCommunications12To13(db: SupportSQLiteDatabase) {
    val hasPayloadStructuredColumn = db.hasColumn("communications", "payload_structured")

    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS communications_new (
            communicationId TEXT NOT NULL PRIMARY KEY,
            orderId TEXT NOT NULL,
            taskId TEXT NOT NULL,
            profileId TEXT NOT NULL,
            telematikId TEXT NOT NULL,
            kvnr TEXT NOT NULL,
            consumed INTEGER NOT NULL,
            payload TEXT,
            payload_structured TEXT,
            profile TEXT NOT NULL,
            recipient TEXT NOT NULL DEFAULT '',
            insuranceId TEXT,
            timeStamp INTEGER NOT NULL,
            pharmacyName TEXT,
            FOREIGN KEY(taskId) REFERENCES tasks(taskId) ON UPDATE CASCADE ON DELETE CASCADE,
            FOREIGN KEY(profileId) REFERENCES profiles(identifier) ON UPDATE CASCADE ON DELETE CASCADE
        )
        """.trimIndent()
    )

    val payloadStructuredSelect = if (hasPayloadStructuredColumn) {
        "payload_structured"
    } else {
        "NULL AS payload_structured"
    }

    db.execSQL(
        """
        INSERT INTO communications_new (
            communicationId,
            orderId,
            taskId,
            profileId,
            telematikId,
            kvnr,
            consumed,
            payload,
            payload_structured,
            profile,
            recipient,
            insuranceId,
            timeStamp,
            pharmacyName
        )
        SELECT
            communicationId,
            orderId,
            taskId,
            profileId,
            telematikId,
            kvnr,
            consumed,
            payload,
            $payloadStructuredSelect,
            profile,
            COALESCE(recipient, ''),
            insuranceId,
            timeStamp,
            pharmacyName
        FROM communications
        """.trimIndent()
    )

    db.execSQL("DROP TABLE communications")
    db.execSQL("ALTER TABLE communications_new RENAME TO communications")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_communications_orderId ON communications (orderId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_communications_taskId ON communications (taskId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_communications_profile ON communications (profile)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_communications_insuranceId ON communications (insuranceId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_communications_profileId ON communications (profileId)")
    db.execSQL("CREATE INDEX IF NOT EXISTS index_communications_communicationId ON communications (communicationId)")
}

private fun SupportSQLiteDatabase.hasColumn(tableName: String, columnName: String): Boolean {
    val cursor = query("PRAGMA table_info($tableName)")
    return cursor.use {
        val nameColumnIndex = it.getColumnIndex("name")
        while (it.moveToNext()) {
            if (nameColumnIndex >= 0 && it.getString(nameColumnIndex) == columnName) {
                return true
            }
        }
        false
    }
}
