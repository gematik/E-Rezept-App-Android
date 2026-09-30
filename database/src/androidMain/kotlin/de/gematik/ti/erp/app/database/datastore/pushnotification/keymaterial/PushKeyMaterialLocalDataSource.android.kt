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

/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package de.gematik.ti.erp.app.database.datastore.pushnotification.keymaterial

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import de.gematik.ti.erp.app.database.datastore.AndroidDataStoreCryptography

val Context.pushKeyMaterialDataStore: DataStore<PushKeyMaterialEntitySchema> by dataStore(
    fileName = PUSH_KEY_MATERIAL_DATA_STORE_FILE,
    serializer = PushKeyMaterialEntitySerializer(AndroidDataStoreCryptography),
    corruptionHandler = ReplaceFileCorruptionHandler { PushKeyMaterialEntitySchema() }
)

fun pushKeyMaterialLocalDataSource(context: Context) =
    PushKeyMaterialLocalDataSource(context.pushKeyMaterialDataStore)
