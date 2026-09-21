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

package de.gematik.ti.erp.app.idp.repository

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.database.api.UserAuthenticationLocalDataSource
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.userauthentication.model.SingleSignOnTokenErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class IdpPairingRepository(
    private val localDataSource: UserAuthenticationLocalDataSource
) {
    private val decryptedAccessTokenMap: MutableStateFlow<Map<String, AccessToken>> =
        MutableStateFlow(mutableMapOf())
    private val singleSignOnTokenMap: MutableStateFlow<Map<ProfileIdentifier, SingleSignOnTokenErpModel>> =
        MutableStateFlow(mutableMapOf())

    fun decryptedAccessToken(profileId: ProfileIdentifier) =
        decryptedAccessTokenMap.map { it[profileId] }.distinctUntilChanged()

    fun saveDecryptedAccessToken(profileId: ProfileIdentifier, accessToken: AccessToken) {
        decryptedAccessTokenMap.update {
            it + (profileId to accessToken)
        }
    }

    @Requirement(
        "A_21326#1",
        "A_21327#1",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "removing decrypted access token from map" +
            "since we have automatic memory management, we can't delete the token. " +
            "Due to the use of frameworks we have sensitive data as immutable objects and hence " +
            "cannot override it"
    )
    fun invalidateDecryptedAccessToken(profileId: ProfileIdentifier) {
        decryptedAccessTokenMap.update {
            it - profileId
        }
    }

    /**
     * This function fuses the scope of the original prescription token with the token scoped to pairing.
     */
    fun userAuthentication(profileId: ProfileIdentifier) =
        combine(
            localDataSource.getUserAuthenticationForProfile(profileId),
            singleSignOnTokenMap
                .map { it[profileId] }
                .distinctUntilChanged()
        ) { authenticationErpModel, pairingToken ->
            when (authenticationErpModel) {
                is UserAuthenticationErpModel.External ->
                    pairingToken?.let {
                        UserAuthenticationErpModel.External(
                            singleSignOnTokenErpModel = it,
                            externalAuthenticatorId = authenticationErpModel.externalAuthenticatorId,
                            externalAuthenticatorName = authenticationErpModel.externalAuthenticatorName
                        )
                    }
                is UserAuthenticationErpModel.HealthCardWithSavedCredentials ->
                    pairingToken?.let {
                        UserAuthenticationErpModel.HealthCardWithSavedCredentials(
                            singleSignOnTokenErpModel = it,
                            cardAccessNumber = authenticationErpModel.cardAccessNumber,
                            aliasOfSecureElementEntry = authenticationErpModel.aliasOfSecureElementEntry,
                            healthCardCertificate = authenticationErpModel.healthCardCertificate
                        )
                    } ?: authenticationErpModel
                is UserAuthenticationErpModel.HealthCard ->
                    pairingToken?.let {
                        UserAuthenticationErpModel.HealthCard(
                            singleSignOnTokenErpModel = it,
                            cardAccessNumber = authenticationErpModel.cardAccessNumber,
                            healthCardCertificate = authenticationErpModel.healthCardCertificate
                        )
                    }
                is UserAuthenticationErpModel.NotInitialized -> UserAuthenticationErpModel.NotInitialized
            }
        }

    fun saveSingleSignOnToken(profileId: ProfileIdentifier, ssoToken: SingleSignOnTokenErpModel?) {
        ssoToken?.let { nonNullToken ->
            singleSignOnTokenMap.update {
                it + (profileId to nonNullToken)
            }
        }
    }

    fun invalidateSingleSignOnToken(profileId: ProfileIdentifier) {
        singleSignOnTokenMap.update {
            it - profileId
        }
    }
}
