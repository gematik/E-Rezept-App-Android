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

package de.gematik.ti.erp.app.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.gematik.ti.erp.app.db.entities.IdpAuthenticationDataEntity
import de.gematik.ti.erp.app.db.entities.IdpConfiguration
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface IdpConfigurationDao {

    @Query("SELECT * FROM idpConfiguration")
    suspend fun getIdpConfiguration(): IdpConfiguration?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdpConfiguration(idpConfiguration: IdpConfiguration)

    @Query("DELETE FROM idpConfiguration")
    suspend fun clearIdpConfigurationTable()
}

@Dao
interface IdpAuthenticationDataDao {

    @Query("SELECT * FROM idpAuthenticationDataEntity WHERE profileName = :activeProfileName")
    fun getIdpAuthenticationEntity(activeProfileName: String): Flow<IdpAuthenticationDataEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(data: IdpAuthenticationDataEntity)

    @Query("UPDATE idpAuthenticationDataEntity SET singleSignOnToken = null, singleSignOnTokenScope = null, singleSignOnTokenValidOn = null, singleSignOnTokenExpiresON = null, cardAccessNumber = null, healthCardCertificate = null, aliasOfSecureElementEntry = null WHERE profileName = :profileName")
    suspend fun clear(profileName: String)

    @Query("UPDATE idpAuthenticationDataEntity SET singleSignOnToken = :token, singleSignOnTokenScope = :scope, singleSignOnTokenValidOn = :validOn, singleSignOnTokenExpiresON = :expiresOn WHERE profileName = :profileName")
    suspend fun updateToken(profileName: String, token: String?, scope: IdpAuthenticationDataEntity.SingleSignOnTokenScope?, validOn: Instant?, expiresOn: Instant?)

    @Query("UPDATE idpAuthenticationDataEntity SET singleSignOnToken = :token, singleSignOnTokenValidOn = :validOn, singleSignOnTokenExpiresON = :expiresOn WHERE profileName = :profileName")
    suspend fun updateTokenWithoutScope(profileName: String, token: String?, validOn: Instant?, expiresOn: Instant?)

    @Query("UPDATE idpAuthenticationDataEntity SET cardAccessNumber = :can WHERE profileName = :profileName")
    suspend fun updateCardAccessNumber(profileName: String, can: String?)

    @Query("SELECT cardAccessNumber FROM idpAuthenticationDataEntity WHERE profileName = :profileName")
    fun cardAccessNumber(profileName: String): Flow<String?>

    @Query("UPDATE idpAuthenticationDataEntity SET healthCardCertificate = :cert WHERE profileName = :profileName")
    suspend fun updateHealthCardCert(profileName: String, cert: ByteArray?)

    @Query("UPDATE idpAuthenticationDataEntity SET aliasOfSecureElementEntry = :alias WHERE profileName = :profileName")
    suspend fun updateAliasOfSecureElement(profileName: String, alias: ByteArray?)
}
