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

package de.gematik.ti.erp.app.idp.repository

import androidx.room.withTransaction
import de.gematik.ti.erp.app.db.AppDatabase
import de.gematik.ti.erp.app.db.entities.IdpAuthenticationDataEntity
import de.gematik.ti.erp.app.db.entities.IdpConfiguration
import javax.inject.Inject

class IdpLocalDataSource @Inject constructor(
    private val db: AppDatabase
) {
    suspend fun saveIdpInfo(idpConfiguration: IdpConfiguration) {
        db.idpInfoDao().insertIdpConfiguration(idpConfiguration)
    }

    suspend fun loadIdpInfo(): IdpConfiguration? {
        return db.idpInfoDao().getIdpConfiguration()
    }

    suspend fun clearIdpInfo() {
        db.idpInfoDao().clearIdpConfigurationTable()
    }

    suspend fun saveSingleSignOnToken(token: String?, scope: SingleSignOnToken.Scope?) {
        db.idpAuthDataDao().updateToken(token, scope)
    }

    suspend fun saveSingleSignOnToken(token: String?) {
        db.idpAuthDataDao().updateTokenWithoutScope(token)
    }

    suspend fun saveHealthCardCertificate(cert: ByteArray) {
        db.idpAuthDataDao().updateHealthCardCert(cert)
    }

    suspend fun saveSecureElementAlias(alias: ByteArray) {
        db.idpAuthDataDao().updateAliasOfSecureElement(alias)
    }

    suspend fun loadIdpAuthData(): IdpAuthenticationDataEntity = db.withTransaction {
        db.idpAuthDataDao().get() ?: run {
            IdpAuthenticationDataEntity().also {
                db.idpAuthDataDao().insert(IdpAuthenticationDataEntity())
            }
        }
    }

    suspend fun clearIdpAuthData() {
        db.idpAuthDataDao().clear()
    }
}
