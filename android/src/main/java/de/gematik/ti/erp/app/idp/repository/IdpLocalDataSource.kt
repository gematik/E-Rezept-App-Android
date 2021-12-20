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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import java.time.Instant
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

    suspend fun saveSingleSignOnToken(
        profileName: String,
        token: String?,
        scope: SingleSignOnToken.Scope?,
        validOn: Instant?,
        expiresOn: Instant?
    ) {
        db.idpAuthDataDao().updateToken(
            profileName = profileName,
            token = token,
            scope = scope,
            validOn = validOn,
            expiresOn = expiresOn
        )
    }

    suspend fun saveSingleSignOnToken(
        profileName: String,
        token: String?,
        validOn: Instant?,
        expiresOn: Instant?
    ) {
        db.idpAuthDataDao().updateTokenWithoutScope(
            profileName = profileName,
            token = token,
            validOn = validOn,
            expiresOn = expiresOn
        )
    }

    suspend fun saveHealthCardCertificate(profileName: String, cert: ByteArray) {
        db.idpAuthDataDao().updateHealthCardCert(profileName, cert)
    }

    suspend fun saveSecureElementAlias(profileName: String, alias: ByteArray) {
        db.idpAuthDataDao().updateAliasOfSecureElement(profileName, alias)
    }

    suspend fun loadIdpAuthData(profileName: String): Flow<IdpAuthenticationDataEntity> {
        db.withTransaction {
            if (db.profileDao().countProfilesWithName(profileName) == 1) {
                db.idpAuthDataDao().insert(IdpAuthenticationDataEntity(profileName))
            }
        }

        return db.idpAuthDataDao().getIdpAuthenticationEntity(profileName).filterNotNull()
    }

    suspend fun clearIdpAuthData(profileName: String) {
        db.idpAuthDataDao().clear(profileName)
    }

    suspend fun setCardAccessNumber(profileName: String, can: String?) {
        db.idpAuthDataDao().updateCardAccessNumber(profileName, can)
    }

    fun cardAccessNumber(profileName: String) =
        db.idpAuthDataDao().cardAccessNumber(profileName)

    suspend fun updateLastAuthenticated(lastAuthenticated: Instant, profileName: String) {
        db.profileDao().updateLastAuthenticated(lastAuthenticated, profileName)
    }
}
