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

package de.gematik.ti.erp.app.database.realm.v1.idp

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.database.api.IdpConfigurationLocalDataSource
import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.utils.writeOrCopyToRealm
import de.gematik.ti.erp.app.database.realm.utils.writeToRealm
import de.gematik.ti.erp.app.idp.IdpConfigurationErpModel
import io.realm.kotlin.Realm

@Requirement(
    "O.Data_4#2",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "The encrypted realm database is used to store data."
)
class IdpConfigurationLocalDataSourceV1(private val realm: Realm) : IdpConfigurationLocalDataSource {
    override suspend fun getIdpConfiguration(): IdpConfigurationErpModel? =
        realm.queryFirst<IdpConfigurationEntityV1>()?.let {
            IdpConfigurationErpModel(
                authorizationEndpoint = it.authorizationEndpoint,
                ssoEndpoint = it.ssoEndpoint,
                tokenEndpoint = it.tokenEndpoint,
                pairingEndpoint = it.pairingEndpoint,
                authenticationEndpoint = it.authenticationEndpoint,
                pukIdpEncEndpoint = it.pukIdpEncEndpoint,
                pukIdpSigEndpoint = it.pukIdpSigEndpoint,
                certificate = it.certificateX509,
                expirationTimestamp = it.expirationTimestamp.toInstant(),
                issueTimestamp = it.issueTimestamp.toInstant(),
                externalAuthorizationIDsEndpoint = it.externalAuthorizationIDsEndpoint,
                thirdPartyAuthorizationEndpoint = it.thirdPartyAuthorizationEndpoint,
                federationAuthorizationIDsEndpoint = it.federationAuthorizationIDsEndpoint,
                federationAuthorizationEndpoint = it.federationAuthorizationEndpoint
            )
        }

    override suspend fun saveIdpConfiguration(idpConfiguration: IdpConfigurationErpModel) {
        realm.writeOrCopyToRealm(::IdpConfigurationEntityV1) { entity ->
            entity.authorizationEndpoint = idpConfiguration.authorizationEndpoint
            entity.ssoEndpoint = idpConfiguration.ssoEndpoint
            entity.tokenEndpoint = idpConfiguration.tokenEndpoint
            entity.pairingEndpoint = idpConfiguration.pairingEndpoint
            entity.authenticationEndpoint = idpConfiguration.authenticationEndpoint
            entity.pukIdpEncEndpoint = idpConfiguration.pukIdpEncEndpoint
            entity.pukIdpSigEndpoint = idpConfiguration.pukIdpSigEndpoint
            entity.certificateX509 = idpConfiguration.certificate!!
            entity.expirationTimestamp = idpConfiguration.expirationTimestamp.toRealmInstant()
            entity.issueTimestamp = idpConfiguration.issueTimestamp.toRealmInstant()
            entity.externalAuthorizationIDsEndpoint = idpConfiguration.externalAuthorizationIDsEndpoint
            entity.thirdPartyAuthorizationEndpoint = idpConfiguration.thirdPartyAuthorizationEndpoint
            entity.federationAuthorizationIDsEndpoint = idpConfiguration.federationAuthorizationIDsEndpoint
            entity.federationAuthorizationEndpoint = idpConfiguration.federationAuthorizationEndpoint
        }
    }

    override suspend fun invalidateIdpConfiguration() {
        realm.writeToRealm<IdpConfigurationEntityV1, Unit> { config ->
            delete(config)
        }
    }
}
