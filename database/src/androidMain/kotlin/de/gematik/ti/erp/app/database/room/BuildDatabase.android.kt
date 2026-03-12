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

import de.gematik.ti.erp.app.database.room.v2.settings.SettingsRoomEntity
import de.gematik.ti.erp.app.database.settings.appContext
import kotlinx.coroutines.runBlocking

/**
 * Android actual implementation that builds the Room database using the
 * platform builder and the shared getRoomDatabase() helper.
 * Additionally, it seeds the Settings table with an example row
 * the first time the DB is created/used, if none exists yet.
 */
actual fun buildAppDatabase(): AppDatabase {
    val builder = getDatabaseBuilder(appContext)
    val db = getRoomDatabase(builder)

    // Seed default settings if missing
    runBlocking {
        val existing = db.settingsDao().get()
        if (existing == null) {
            db.settingsDao().upsert(SettingsRoomEntity.example())
        }
    }

    return db
}
