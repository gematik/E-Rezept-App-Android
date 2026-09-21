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

package de.gematik.ti.erp.app.database.room.v2.idp

import de.gematik.ti.erp.app.idp.IdpConfigurationErpModel
import kotlinx.datetime.Instant

internal fun IdpConfigurationErpModel.toIdpConfigurationEntity() = IdpConfigurationEntity(
    authorizationEndpoint = authorizationEndpoint,
    ssoEndpoint = ssoEndpoint,
    tokenEndpoint = tokenEndpoint,
    pairingEndpoint = pairingEndpoint,
    authenticationEndpoint = authenticationEndpoint,
    pukIdpEncEndpoint = pukIdpEncEndpoint,
    pukIdpSigEndpoint = pukIdpSigEndpoint,
    certificateX509 = certificate,
    expirationTimestamp = expirationTimestamp,
    issueTimestamp = issueTimestamp,
    externalAuthorizationIDsEndpoint = externalAuthorizationIDsEndpoint,
    federationAuthorizationIDsEndpoint = federationAuthorizationIDsEndpoint,
    federationAuthorizationEndpoint = federationAuthorizationEndpoint,
    thirdPartyAuthorizationEndpoint = thirdPartyAuthorizationEndpoint
)

internal fun IdpConfigurationEntity.toIdpConfigurationErpModel() = IdpConfigurationErpModel(
    authorizationEndpoint = authorizationEndpoint ?: "",
    ssoEndpoint = ssoEndpoint ?: "",
    tokenEndpoint = tokenEndpoint ?: "",
    pairingEndpoint = pairingEndpoint ?: "",
    authenticationEndpoint = authenticationEndpoint ?: "",
    pukIdpEncEndpoint = pukIdpEncEndpoint ?: "",
    pukIdpSigEndpoint = pukIdpSigEndpoint ?: "",
    certificate = certificateX509,
    expirationTimestamp = expirationTimestamp ?: Instant.DISTANT_PAST,
    issueTimestamp = issueTimestamp ?: Instant.DISTANT_PAST,
    externalAuthorizationIDsEndpoint = externalAuthorizationIDsEndpoint,
    federationAuthorizationIDsEndpoint = federationAuthorizationIDsEndpoint,
    federationAuthorizationEndpoint = federationAuthorizationEndpoint,
    thirdPartyAuthorizationEndpoint = thirdPartyAuthorizationEndpoint
)
