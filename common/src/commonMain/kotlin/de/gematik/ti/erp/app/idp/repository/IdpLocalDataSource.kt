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

package de.gematik.ti.erp.app.idp.repository

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.db.entities.v1.IdpAuthenticationDataEntityV1
import de.gematik.ti.erp.app.db.entities.v1.IdpConfigurationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import de.gematik.ti.erp.app.db.entities.v1.SingleSignOnTokenScopeV1
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.db.toInstant
import de.gematik.ti.erp.app.db.toRealmInstant
import de.gematik.ti.erp.app.db.writeOrCopyToRealm
import de.gematik.ti.erp.app.db.writeToRealm
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.map
import org.bouncycastle.cert.X509CertificateHolder
import java.security.KeyStore

class IdpLocalDataSource constructor(
    private val realm: Realm
) {
    suspend fun saveIdpInfo(config: IdpData.IdpConfiguration) {
        realm.writeOrCopyToRealm(::IdpConfigurationEntityV1) { entity ->
            entity.authorizationEndpoint = config.authorizationEndpoint
            entity.ssoEndpoint = config.ssoEndpoint
            entity.tokenEndpoint = config.tokenEndpoint
            entity.pairingEndpoint = config.pairingEndpoint
            entity.authenticationEndpoint = config.authenticationEndpoint
            entity.pukIdpEncEndpoint = config.pukIdpEncEndpoint
            entity.pukIdpSigEndpoint = config.pukIdpSigEndpoint
            entity.certificateX509 = config.certificate.encoded
            entity.expirationTimestamp = config.expirationTimestamp.toRealmInstant()
            entity.issueTimestamp = config.issueTimestamp.toRealmInstant()
            entity.externalAuthorizationIDsEndpoint = config.externalAuthorizationIDsEndpoint
            entity.thirdPartyAuthorizationEndpoint = config.thirdPartyAuthorizationEndpoint
        }
    }

    fun loadIdpInfo(): IdpData.IdpConfiguration? =
        realm.queryFirst<IdpConfigurationEntityV1>()?.let {
            IdpData.IdpConfiguration(
                authorizationEndpoint = it.authorizationEndpoint,
                ssoEndpoint = it.ssoEndpoint,
                tokenEndpoint = it.tokenEndpoint,
                pairingEndpoint = it.pairingEndpoint,
                authenticationEndpoint = it.authenticationEndpoint,
                pukIdpEncEndpoint = it.pukIdpEncEndpoint,
                pukIdpSigEndpoint = it.pukIdpSigEndpoint,
                certificate = X509CertificateHolder(it.certificateX509),
                expirationTimestamp = it.expirationTimestamp.toInstant(),
                issueTimestamp = it.issueTimestamp.toInstant(),
                externalAuthorizationIDsEndpoint = it.externalAuthorizationIDsEndpoint,
                thirdPartyAuthorizationEndpoint = it.thirdPartyAuthorizationEndpoint
            )
        }

    suspend fun invalidateConfiguration() {
        realm.writeToRealm<IdpConfigurationEntityV1, Unit> { config ->
            delete(config)
        }
    }

    @Requirement(
        "A_21328#2",
        "A_21322",
        "A_21595#4",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Save the SSO token to database."
    )
    @Requirement(
        "O.Tokn_1#1",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Save the SSO token to realm database."
    )
    suspend fun saveSingleSignOnToken(
        profileId: ProfileIdentifier,
        tokenScope: IdpData.SingleSignOnTokenScope
    ) {
        writeToRealm(profileId) { profile ->
            val actualToken = tokenScope.token?.token
            val scope = when (tokenScope) {
                is IdpData.ExternalAuthenticationToken -> SingleSignOnTokenScopeV1.ExternalAuthentication
                is IdpData.AlternateAuthenticationToken -> SingleSignOnTokenScopeV1.AlternateAuthentication
                is IdpData.AlternateAuthenticationWithoutToken -> SingleSignOnTokenScopeV1.AlternateAuthentication
                is IdpData.DefaultToken -> SingleSignOnTokenScopeV1.Default
            }
            val can = when (tokenScope) {
                is IdpData.TokenWithHealthCardScope -> tokenScope.cardAccessNumber
                else -> ""
            }
            val cert = when (tokenScope) {
                is IdpData.TokenWithHealthCardScope -> tokenScope.healthCardCertificate.encoded
                else -> null
            }
            val alias = when (tokenScope) {
                is IdpData.AlternateAuthenticationToken -> tokenScope.aliasOfSecureElementEntry
                is IdpData.AlternateAuthenticationWithoutToken -> tokenScope.aliasOfSecureElementEntry
                else -> null
            }
            val authId = when (tokenScope) {
                is IdpData.ExternalAuthenticationToken -> tokenScope.authenticatorId
                else -> null
            }
            val authName = when (tokenScope) {
                is IdpData.ExternalAuthenticationToken -> tokenScope.authenticatorName
                else -> null
            }

            getOrInsertAuthData(profile)?.apply {
                this.singleSignOnToken = actualToken
                this.singleSignOnTokenScope = scope

                this.cardAccessNumber = can
                this.healthCardCertificate = cert
                this.aliasOfSecureElementEntry = alias

                this.externalAuthenticatorId = authId
                this.externalAuthenticatorName = authName
            }
        }
    }

    suspend fun invalidateSingleSignOnTokenRetainingScope(profileId: ProfileIdentifier) {
        writeToRealm(profileId) { profile ->
            getOrInsertAuthData(profile)?.apply {
                this.singleSignOnToken = null
            }
        }
    }

    @Requirement(
        "O.Tokn_6#5",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "invalidate authentication data from keystore "
    )
    suspend fun invalidateAuthenticationData(profileId: ProfileIdentifier) {
        writeToRealm(profileId) { profile ->
            getOrInsertAuthData(profile)?.apply {
                try {
                    this.aliasOfSecureElementEntry?.also {
                        KeyStore.getInstance("AndroidKeyStore")
                            .apply { load(null) }
                            .deleteEntry(it.decodeToString())
                    }
                } catch (e: Exception) {
                    // silent fail; expected
                }

                delete(this)
            }
        }
    }

    fun authenticationData(profileId: ProfileIdentifier) =
        realm.query<ProfileEntityV1>("id = $0", profileId)
            .first()
            .asFlow()
            .map { profile ->
                IdpData.AuthenticationData(
                    singleSignOnTokenScope = profile.obj?.idpAuthenticationData?.toSingleSignOnTokenScope()
                )
            }

    private suspend fun writeToRealm(profileId: ProfileIdentifier, block: MutableRealm.(ProfileEntityV1) -> Unit) {
        realm.writeToRealm<ProfileEntityV1, Unit>("id == $0", profileId) {
            block(it)
        }
    }

    private fun MutableRealm.getOrInsertAuthData(profile: ProfileEntityV1) =
        if (profile.idpAuthenticationData == null) {
            copyToRealm(IdpAuthenticationDataEntityV1()).also {
                profile.idpAuthenticationData = it
            }
        } else {
            profile.idpAuthenticationData
        }
}

fun IdpAuthenticationDataEntityV1.toSingleSignOnTokenScope(): IdpData.SingleSignOnTokenScope? =
    try {
        when (this.singleSignOnTokenScope) {
            SingleSignOnTokenScopeV1.Default ->
                IdpData.DefaultToken(
                    token = this.singleSignOnToken?.let { token -> IdpData.SingleSignOnToken(token) },
                    cardAccessNumber = this.cardAccessNumber,
                    healthCardCertificate = requireNotNull(this.healthCardCertificate)
                )
            SingleSignOnTokenScopeV1.AlternateAuthentication ->
                this.singleSignOnToken?.let { token ->
                    IdpData.AlternateAuthenticationToken(
                        token = IdpData.SingleSignOnToken(token),
                        cardAccessNumber = this.cardAccessNumber,
                        healthCardCertificate = requireNotNull(this.healthCardCertificate),
                        aliasOfSecureElementEntry = requireNotNull(this.aliasOfSecureElementEntry)
                    )
                } ?: IdpData.AlternateAuthenticationWithoutToken(
                    cardAccessNumber = this.cardAccessNumber,
                    aliasOfSecureElementEntry = requireNotNull(this.aliasOfSecureElementEntry),
                    healthCardCertificate = requireNotNull(this.healthCardCertificate)
                )
            SingleSignOnTokenScopeV1.ExternalAuthentication ->
                IdpData.ExternalAuthenticationToken(
                    token = this.singleSignOnToken?.let { token -> IdpData.SingleSignOnToken(token) },
                    authenticatorId = requireNotNull(this.externalAuthenticatorId),
                    authenticatorName = requireNotNull(this.externalAuthenticatorName)
                )
        }
    } catch (e: IllegalArgumentException) {
        Napier.e("IDP auth data is in a inconsistent state", e)
        null
    }
