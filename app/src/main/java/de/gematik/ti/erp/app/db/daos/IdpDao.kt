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

package de.gematik.ti.erp.app.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.gematik.ti.erp.app.db.entities.IdpAuthenticationDataEntity
import de.gematik.ti.erp.app.db.entities.IdpConfiguration
import de.gematik.ti.erp.app.idp.repository.SingleSignOnToken

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

    @Query("SELECT * FROM idpAuthenticationDataEntity")
    suspend fun get(): IdpAuthenticationDataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: IdpAuthenticationDataEntity)

    @Query("UPDATE idpAuthenticationDataEntity SET singleSignOnToken = :token, singleSignOnTokenScope = :scope")
    suspend fun updateToken(token: String?, scope: SingleSignOnToken.Scope?)

    @Query("UPDATE idpAuthenticationDataEntity SET singleSignOnToken = :token")
    suspend fun updateTokenWithoutScope(token: String?)

    @Query("UPDATE idpAuthenticationDataEntity SET healthCardCertificate = :cert")
    suspend fun updateHealthCardCert(cert: ByteArray?)

    @Query("UPDATE idpAuthenticationDataEntity SET aliasOfSecureElementEntry = :alias")
    suspend fun updateAliasOfSecureElement(alias: ByteArray?)

    @Query("DELETE FROM idpAuthenticationDataEntity")
    suspend fun clear()
}
