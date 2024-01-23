/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.vau.repository

import de.gematik.ti.erp.app.db.entities.v1.TruststoreEntityV1
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.db.writeOrCopyToRealm
import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import io.realm.kotlin.Realm
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
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
