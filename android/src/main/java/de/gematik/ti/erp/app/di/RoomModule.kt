/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.gematik.ti.erp.app.db.AppDatabase
import de.gematik.ti.erp.app.db.MIGRATION_10_11
import de.gematik.ti.erp.app.db.MIGRATION_11_12
import de.gematik.ti.erp.app.db.MIGRATION_12_13
import de.gematik.ti.erp.app.db.MIGRATION_13_14
import de.gematik.ti.erp.app.db.MIGRATION_14_15
import de.gematik.ti.erp.app.db.MIGRATION_15_16
import de.gematik.ti.erp.app.db.MIGRATION_16_17
import de.gematik.ti.erp.app.db.MIGRATION_17_18
import de.gematik.ti.erp.app.db.MIGRATION_18_19
import de.gematik.ti.erp.app.db.MIGRATION_19_20
import de.gematik.ti.erp.app.db.MIGRATION_1_2
import de.gematik.ti.erp.app.db.MIGRATION_20_21
import de.gematik.ti.erp.app.db.MIGRATION_21_22
import de.gematik.ti.erp.app.db.MIGRATION_22_23
import de.gematik.ti.erp.app.db.MIGRATION_23_24
import de.gematik.ti.erp.app.db.MIGRATION_24_25
import de.gematik.ti.erp.app.db.MIGRATION_25_26
import de.gematik.ti.erp.app.db.MIGRATION_2_3
import de.gematik.ti.erp.app.db.MIGRATION_3_4
import de.gematik.ti.erp.app.db.MIGRATION_4_5
import de.gematik.ti.erp.app.db.MIGRATION_5_6
import de.gematik.ti.erp.app.db.MIGRATION_6_7
import de.gematik.ti.erp.app.db.MIGRATION_7_8
import de.gematik.ti.erp.app.db.MIGRATION_8_9
import de.gematik.ti.erp.app.db.MIGRATION_9_10
import de.gematik.ti.erp.app.db.converter.TruststoreConverter
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.util.UUID
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RoomDatabaseSecureSharedPreferences

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    val migrations = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_8_9,
        MIGRATION_9_10,
        MIGRATION_10_11,
        MIGRATION_11_12,
        MIGRATION_12_13,
        MIGRATION_13_14,
        MIGRATION_14_15,
        MIGRATION_15_16,
        MIGRATION_16_17,
        MIGRATION_17_18,
        MIGRATION_18_19,
        MIGRATION_19_20,
        MIGRATION_20_21,
        MIGRATION_21_22,
        MIGRATION_22_23,
        MIGRATION_23_24,
        MIGRATION_24_25,
        MIGRATION_25_26
    )

    private const val ENCRYPTED_PREFS_FILE_NAME = "ENCRYPTED_PREFS_FILE_NAME"
    private const val ENCRYPTED_PREFS_PASSWORD_KEY = "ENCRYPTED_PREFS_PASSWORD_KEY"
    private const val MASTER_KEY_ALIAS = "ROOM_DB_MASTER_KEY"

    @Singleton
    @Provides
    fun provideRoomDatabase(
        @ApplicationContext context: Context,
        truststoreConverter: TruststoreConverter,
        @RoomDatabaseSecureSharedPreferences securePrefs: SharedPreferences
    ): AppDatabase {
        val passphrase: ByteArray =
            SQLiteDatabase.getBytes(getPassphrase(securePrefs).toCharArray())
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "db"
        )
            .addMigrations(*migrations)
            .addTypeConverter(truststoreConverter)
            .openHelperFactory(factory)
            .build()
    }

    private fun getPassphrase(sharedPreferences: SharedPreferences): String {
        if (getPassword(sharedPreferences).isNullOrEmpty()) {
            val passPhrase = generatePassPhrase()
            storePassPhrase(sharedPreferences, passPhrase)
        }
        return getPassword(sharedPreferences)
            ?: throw IllegalStateException("passphrase should not be empty")
    }

    private fun generatePassPhrase(): String {
        return UUID.randomUUID().toString()
    }

    private fun storePassPhrase(
        @RoomDatabaseSecureSharedPreferences sharedPreferences: SharedPreferences,
        passPhrase: String
    ) {
        sharedPreferences.edit().putString(
            ENCRYPTED_PREFS_PASSWORD_KEY,
            passPhrase
        )
            .apply()
    }

    private fun getPassword(sharedPreferences: SharedPreferences): String? {
        return sharedPreferences.getString(
            ENCRYPTED_PREFS_PASSWORD_KEY,
            null
        )
    }

    @RoomDatabaseSecureSharedPreferences
    @Singleton
    @Provides
    fun providesSecureSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_FILE_NAME,
            MasterKey.Builder(context, MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
