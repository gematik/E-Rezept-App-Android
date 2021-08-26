/*
 * Copyright (c) 2021 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.db

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.gematik.ti.erp.app.db.converter.TruststoreConverter
import de.gematik.ti.erp.app.di.TruststoreModule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private val TEST_DB = "migration-test"

    private val migration = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)

    private val truststoreConverter = TruststoreConverter(TruststoreModule.provideTruststoreMoshi())

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    // Placeholder for future migrations
    @Test
    fun migratesFromVersion1ToVersionX() {
        helper.createDatabase(TEST_DB, 1).apply {
            close()
        }
        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
            TEST_DB
        )
            .addTypeConverter(truststoreConverter)
            .addMigrations(*migration).build().apply {
                openHelper.writableDatabase
                close()
            }
    }

    @Test
    fun migratesFromVersion4ToVersion5() {
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                "INSERT INTO `medicationDispense` (`taskId`, `patientIdentifier`, `uniqueIdentifier`, `wasSubstituted`, `dosageInstruction`, `performer`, `whenHandedOver`, `text`, `type`)" +
                    "VALUES ('test1', 'test2', 'test3', 1, 'test4', 'test5', 'test6', 'test7', 123)"
            )
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5).use { db ->
            db.query("SELECT `taskId`, `patientIdentifier`, `uniqueIdentifier`, `wasSubstituted`, `dosageInstruction`, `performer`, `whenHandedOver`, `text`, `type` FROM `medicationDispense`")
                .let {
                    it.moveToFirst()
                    assertEquals("test1", it.getString((it.getColumnIndex("taskId"))))
                    assertEquals("test2", it.getString((it.getColumnIndex("patientIdentifier"))))
                    assertEquals("test3", it.getString((it.getColumnIndex("uniqueIdentifier"))))
                    assertEquals(1, it.getInt((it.getColumnIndex("wasSubstituted"))))
                    assertEquals("test4", it.getString((it.getColumnIndex("dosageInstruction"))))
                    assertEquals("test5", it.getString((it.getColumnIndex("performer"))))
                    assertEquals("test6", it.getString((it.getColumnIndex("whenHandedOver"))))
                    assertEquals("test7", it.getString((it.getColumnIndex("text"))))
                    assertEquals(null, it.getString((it.getColumnIndex("type"))))
                }
        }
    }
}
