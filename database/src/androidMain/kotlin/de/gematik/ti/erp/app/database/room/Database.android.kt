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
import de.gematik.ti.erp.app.database.room.security.RoomEncryptionConfig
import de.gematik.ti.erp.app.database.BuildConfig as ModuleBuildConfig

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbName = "room.db"

    // In non-debug builds, ensure the old plaintext DB (if any) is removed to avoid SQLCipher open errors.
    if (!ModuleBuildConfig.DEBUG) {
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

    // Apply SQLCipher openHelperFactory only in non-debug builds
    RoomEncryptionConfig.getOpenHelperFactoryIfNeeded(appContext)?.let { factory ->
        builder.openHelperFactory(factory)
    }

    return builder
}
