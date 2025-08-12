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

package de.gematik.ti.erp.app.vau.repository

import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.writeOrCopyToRealm
import de.gematik.ti.erp.app.database.realm.v1.TruststoreEntityV1
import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import io.realm.kotlin.Realm
import kotlinx.serialization.json.Json

class VauLocalDataSource(
    private val realm: Realm
) {

    suspend fun saveLists(certList: UntrustedCertList, ocspList: UntrustedOCSPList) {
        realm.writeOrCopyToRealm(::TruststoreEntityV1) {
            it.certListJson = Json.encodeToString(certList)
            it.ocspListJson = Json.encodeToString(ocspList)
        }
    }

    fun loadUntrusted(): Pair<UntrustedCertList, UntrustedOCSPList>? =
        realm.queryFirst<TruststoreEntityV1>()?.let {
            Pair(Json.decodeFromString(it.certListJson), Json.decodeFromString(it.ocspListJson))
        }

    suspend fun deleteAll() {
        realm.writeOrCopyToRealm(::TruststoreEntityV1) {
            delete(it)
        }
    }
}
