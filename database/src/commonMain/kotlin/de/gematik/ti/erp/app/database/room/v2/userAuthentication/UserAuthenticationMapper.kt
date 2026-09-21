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

package de.gematik.ti.erp.app.database.room.v2.userAuthentication

import de.gematik.ti.erp.app.userauthentication.model.SingleSignOnTokenErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty

fun UserAuthenticationErpModel.toUserAuthenticationEntity(
    profileId: ProfileIdentifier
): UserAuthenticationEntity {
    return when (this) {
        is UserAuthenticationErpModel.HealthCard -> UserAuthenticationEntity(
            profileId = profileId,
            singleSignOnToken = singleSignOnTokenErpModel?.token,
            healthCardAuthenticationEntity = HealthCardAuthenticationEntity(
                healthCardCertificate = healthCardCertificate,
                cardAccessNumber = cardAccessNumber,
                aliasOfSecureElementEntry = null
            ),
            externalAuthenticationEntity = ExternalAuthenticationEntity(
                externalAuthenticatorId = null,
                externalAuthenticatorName = null
            )
        )
        is UserAuthenticationErpModel.HealthCardWithSavedCredentials -> UserAuthenticationEntity(
            profileId = profileId,
            singleSignOnToken = singleSignOnTokenErpModel?.token,
            healthCardAuthenticationEntity = HealthCardAuthenticationEntity(
                healthCardCertificate = healthCardCertificate,
                cardAccessNumber = cardAccessNumber,
                aliasOfSecureElementEntry = aliasOfSecureElementEntry
            ),
            externalAuthenticationEntity = ExternalAuthenticationEntity(
                externalAuthenticatorId = null,
                externalAuthenticatorName = null
            )
        )
        is UserAuthenticationErpModel.External -> UserAuthenticationEntity(
            profileId = profileId,
            singleSignOnToken = singleSignOnTokenErpModel?.token,
            healthCardAuthenticationEntity = HealthCardAuthenticationEntity(
                healthCardCertificate = null,
                cardAccessNumber = null,
                aliasOfSecureElementEntry = null
            ),
            externalAuthenticationEntity = ExternalAuthenticationEntity(
                externalAuthenticatorId = externalAuthenticatorId,
                externalAuthenticatorName = externalAuthenticatorName
            )
        )
        is UserAuthenticationErpModel.NotInitialized -> UserAuthenticationEntity(
            profileId = profileId,
            singleSignOnToken = null,
            healthCardAuthenticationEntity = HealthCardAuthenticationEntity(
                healthCardCertificate = null,
                cardAccessNumber = null,
                aliasOfSecureElementEntry = null
            ),
            externalAuthenticationEntity = ExternalAuthenticationEntity(
                externalAuthenticatorId = null,
                externalAuthenticatorName = null
            )
        )
    }
}

fun UserAuthenticationEntity.toUserAuthenticationErpModel(): UserAuthenticationErpModel {
    return when {
        externalAuthenticationEntity.externalAuthenticatorId.isNotNullOrEmpty() &&
            externalAuthenticationEntity.externalAuthenticatorName.isNotNullOrEmpty()
        -> UserAuthenticationErpModel.External(
            singleSignOnTokenErpModel = singleSignOnToken?.let { SingleSignOnTokenErpModel(token = it) },
            externalAuthenticatorId = externalAuthenticationEntity.externalAuthenticatorId.orEmpty(),
            externalAuthenticatorName = externalAuthenticationEntity.externalAuthenticatorName.orEmpty()
        )
        healthCardAuthenticationEntity.aliasOfSecureElementEntry != null &&
            healthCardAuthenticationEntity.healthCardCertificate != null &&
            healthCardAuthenticationEntity.cardAccessNumber.isNotNullOrEmpty() ->
            UserAuthenticationErpModel.HealthCardWithSavedCredentials(
                singleSignOnTokenErpModel = singleSignOnToken?.let { SingleSignOnTokenErpModel(token = it) },
                cardAccessNumber = healthCardAuthenticationEntity.cardAccessNumber.orEmpty(),
                healthCardCertificate = healthCardAuthenticationEntity.healthCardCertificate,
                aliasOfSecureElementEntry = healthCardAuthenticationEntity.aliasOfSecureElementEntry
            )
        healthCardAuthenticationEntity.healthCardCertificate != null &&
            healthCardAuthenticationEntity.cardAccessNumber.isNotNullOrEmpty() ->
            UserAuthenticationErpModel.HealthCard(
                singleSignOnTokenErpModel = singleSignOnToken?.let { SingleSignOnTokenErpModel(token = it) },
                cardAccessNumber = healthCardAuthenticationEntity.cardAccessNumber.orEmpty(),
                healthCardCertificate = healthCardAuthenticationEntity.healthCardCertificate
            )
        else -> UserAuthenticationErpModel.NotInitialized
    }
}
