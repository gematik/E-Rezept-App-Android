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

package de.gematik.ti.erp.app.vau.repository

import de.gematik.ti.erp.app.db.AppDatabase
import de.gematik.ti.erp.app.db.entities.TruststoreEntity
import de.gematik.ti.erp.app.vau.api.model.UntrustedCertList
import de.gematik.ti.erp.app.vau.api.model.UntrustedOCSPList
import javax.inject.Inject

class VauLocalDataSource @Inject constructor(
    private val db: AppDatabase
) {

    suspend fun saveLists(certList: UntrustedCertList, ocspList: UntrustedOCSPList) {
        db.truststoreDao().insert(TruststoreEntity(certList, ocspList))
    }

    suspend fun loadUntrusted(): Pair<UntrustedCertList, UntrustedOCSPList>? {
        return db.truststoreDao().getUntrusted()?.let {
            Pair(it.certList, it.ocspList)
        }
    }

    suspend fun deleteAll() {
        db.truststoreDao().deleteAll()
    }
}
