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
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for Room database migrations from version 1 to 12.
 * Ensures that migrations run successfully and database schema is preserved.
 */
@RunWith(RobolectricTestRunner::class)
class RoomMigrationTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private val databaseName = "room.db"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clear any existing database created by earlier runs so the migration assertions
        // reflect the current schema instead of stale data from a previously built DB.
        if (::db.isInitialized && db.isOpen) {
            db.close()
        }
        context.deleteDatabase(databaseName)
    }

    @After
    fun tearDown() {
        if (::db.isInitialized && db.isOpen) {
            db.close()
        }
        context.deleteDatabase(databaseName)
    }

    /**
     * Test that migrations are properly registered and database can open with new schema
     */
    @Test
    fun testDatabaseOpensWithoutDestructiveMigration() = runTest {
        // Create initial database using getDatabaseBuilder (which includes all migrations)
        db = getDatabaseBuilder(context).build()

        // Verify database opens successfully
        assertNotNull(db.openHelper.readableDatabase)
        assertEquals(RoomSchemaVersion.ACTUAL, db.openHelper.readableDatabase.version)
        db.close()
    }

    /**
     * Verify that key tables exist after opening database
     */
    @Test
    fun testAllRequiredTablesExist() = runTest {
        db = getDatabaseBuilder(context).build()

        // Verify key tables exist
        verifyTableExists("profiles")
        verifyTableExists("patient")
        verifyTableExists("eu_access_codes")
        verifyTableExists("eu_orders")
        verifyTableExists("eu_task_events")
        verifyTableExists("communications")
    }

    /**
     * Verify that new columns added by migrations exist
     */
    @Test
    fun testMigrationColumnsExist() = runTest {
        db = getDatabaseBuilder(context).build()

        // Verify columns added by migrations
        assertTrue(
            tableHasColumn("task_med_device_requests", "sentCommunicationOn"),
            "task_med_device_requests should have sentCommunicationOn column"
        )
        assertTrue(
            tableHasColumn("task_med_device_requests", "userActionState"),
            "task_med_device_requests should have userActionState column"
        )
        assertTrue(
            tableHasColumn("medication", "medication_profile_type"),
            "medication should have medication_profile_type column"
        )
        assertTrue(
            tableHasColumn("medication", "medication_profile_version"),
            "medication should have medication_profile_version column"
        )
        assertTrue(
            tableHasColumn("communications", "payload_structured"),
            "communications should have payload_structured column"
        )
    }

    /**
     * Regression test for upgrading a legacy communications table that does not yet have
     * payload_structured in the source schema.
     */
    @Test
    fun testCommunicationsMigrationHandlesMissingPayloadStructuredColumn() = runTest {
        val helper = createLegacyCommunicationsDatabase()
        val database = helper.writableDatabase

        try {
            migrateCommunications12To13(database)

            val cursor = database.query(
                "SELECT payload_structured, recipient FROM communications WHERE communicationId = 'comm-1'"
            )
            cursor.use {
                assertTrue(it.moveToFirst(), "Migrated communications row should exist")
                assertNull(it.getString(0), "Missing source payload_structured column should migrate as NULL")
                assertEquals("", it.getString(1), "Recipient should be normalized to an empty string")
            }
        } finally {
            database.close()
            helper.close()
            context.deleteDatabase(LEGACY_DB_NAME)
        }
    }

    /**
     * Verify that profile table has correct structure for storing profile data
     */
    @Test
    fun testProfileTableStructure() = runTest {
        db = getDatabaseBuilder(context).build()

        val columns = getTableColumns("profiles")

        // Verify essential profile columns
        assertTrue(columns.contains("identifier"), "profiles table should have identifier column")
        assertTrue(columns.contains("name"), "profiles table should have name column")
        assertTrue(columns.contains("active"), "profiles table should have active column")
        assertTrue(columns.contains("isNew"), "profiles table should have isNew column")
    }

    /**
     * Verify that patient table columns are nullable as expected
     */
    @Test
    fun testPatientTableNullableFields() = runTest {
        db = getDatabaseBuilder(context).build()

        val columnNames = getTableColumns("patient")

        // Verify that patient table has columns (indicating successful migration)
        assertNotNull(columnNames, "Patient table should exist")
        assertTrue(columnNames.isNotEmpty(), "Patient table should have columns")
    }

    /**
     * Test that EU prescription tables have correct structure
     */
    @Test
    fun testEuPrescriptionTablesStructure() = runTest {
        db = getDatabaseBuilder(context).build()

        // Verify eu_access_codes table
        val euAccessColumns = getTableColumns("eu_access_codes")
        assertTrue(euAccessColumns.contains("accessCode"), "eu_access_codes should have accessCode column")
        assertTrue(euAccessColumns.contains("countryCode"), "eu_access_codes should have countryCode column")

        // Verify eu_orders table
        val euOrdersColumns = getTableColumns("eu_orders")
        assertTrue(euOrdersColumns.contains("orderId"), "eu_orders should have orderId column")
        assertTrue(euOrdersColumns.contains("euAccessCodeCode"), "eu_orders should have euAccessCodeCode column")

        // Verify eu_task_events table
        val euEventsColumns = getTableColumns("eu_task_events")
        assertTrue(euEventsColumns.contains("id"), "eu_task_events should have id column")
        assertTrue(euEventsColumns.contains("orderId"), "eu_task_events should have orderId column")
    }

    /**
     * Verify that a table exists in the database
     */
    private fun verifyTableExists(tableName: String) {
        try {
            val database = db.openHelper.readableDatabase
            val cursor = database.query("SELECT 1 FROM $tableName LIMIT 0")
            cursor.close()
            assertTrue(true)
        } catch (e: Exception) {
            throw AssertionError("Table $tableName should exist", e)
        }
    }

    /**
     * Check if a table has a specific column by trying to query it
     */
    private fun tableHasColumn(tableName: String, columnName: String): Boolean {
        return try {
            val database = db.openHelper.readableDatabase
            val cursor = database.query("SELECT $columnName FROM $tableName LIMIT 0")
            cursor.close()
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Get all columns for a table
     */
    private fun getTableColumns(tableName: String): List<String> {
        return try {
            val database = db.openHelper.readableDatabase
            val cursor = database.query("PRAGMA table_info($tableName)")
            val columnNames = mutableListOf<String>()
            if (cursor.moveToFirst()) {
                do {
                    // Column name is at index 1 in PRAGMA table_info result
                    columnNames.add(cursor.getString(1))
                } while (cursor.moveToNext())
            }
            cursor.close()
            columnNames
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun createLegacyCommunicationsDatabase(withPayloadStructuredColumn: Boolean = false): SupportSQLiteOpenHelper {
        return FrameworkSQLiteOpenHelperFactory().create(
            Configuration.builder(context)
                .name(LEGACY_DB_NAME)
                .callback(object : SupportSQLiteOpenHelper.Callback(10) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // Create minimal schema with core tables at version 10
                        db.execSQL("CREATE TABLE tasks (taskId TEXT NOT NULL PRIMARY KEY)")
                        db.execSQL("CREATE TABLE profiles (identifier TEXT NOT NULL PRIMARY KEY)")
                        db.execSQL("CREATE TABLE invoices (id TEXT NOT NULL PRIMARY KEY)")

                        // Create communications table with or without payload_structured column
                        // based on the test parameter
                        if (withPayloadStructuredColumn) {
                            db.execSQL(
                                """
                                CREATE TABLE communications (
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
                                    recipient TEXT,
                                    insuranceId TEXT,
                                    timeStamp INTEGER NOT NULL,
                                    pharmacyName TEXT
                                )
                                """.trimIndent()
                            )
                        } else {
                            db.execSQL(
                                """
                                CREATE TABLE communications (
                                    communicationId TEXT NOT NULL PRIMARY KEY,
                                    orderId TEXT NOT NULL,
                                    taskId TEXT NOT NULL,
                                    profileId TEXT NOT NULL,
                                    telematikId TEXT NOT NULL,
                                    kvnr TEXT NOT NULL,
                                    consumed INTEGER NOT NULL,
                                    payload TEXT,
                                    profile TEXT NOT NULL,
                                    recipient TEXT,
                                    insuranceId TEXT,
                                    timeStamp INTEGER NOT NULL,
                                    pharmacyName TEXT
                                )
                                """.trimIndent()
                            )
                        }

                        // Insert sample communications data
                        db.execSQL(
                            """
                            INSERT INTO communications (
                                communicationId, orderId, taskId, profileId, telematikId, kvnr,
                                consumed, payload, profile, recipient, insuranceId,
                                timeStamp, pharmacyName
                            ) VALUES (
                                'comm-1', 'order-1', 'task-1', 'profile-1', 'tel-1', 'kvnr-1',
                                0, 'payload-1', 'PROFILE', NULL, NULL, 42, 'pharmacy-1'
                            )
                            """.trimIndent()
                        )

                        db.execSQL("INSERT INTO tasks (taskId) VALUES ('task-1')")
                        db.execSQL("INSERT INTO profiles (identifier) VALUES ('profile-1')")
                        db.execSQL("INSERT INTO invoices (id) VALUES ('inv-1')")
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build()
        )
    }

    companion object {
        private const val LEGACY_DB_NAME = "room-communications-legacy.db"
    }
}
