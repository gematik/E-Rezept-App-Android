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

package de.gematik.ti.erp.app.database.realm.v1.userauthentication

import de.gematik.ti.erp.app.userauthentication.model.SingleSignOnTokenErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty

fun UserAuthenticationErpModel.toIdpAuthenticationDataEntityV1(): IdpAuthenticationDataEntityV1 =
    when (this) {
        is UserAuthenticationErpModel.HealthCard -> IdpAuthenticationDataEntityV1().apply {
            singleSignOnToken = singleSignOnTokenErpModel?.token
            singleSignOnTokenScope = SingleSignOnTokenScopeV1.Default
            cardAccessNumber = this@toIdpAuthenticationDataEntityV1.cardAccessNumber
            healthCardCertificate = this@toIdpAuthenticationDataEntityV1.healthCardCertificate
            aliasOfSecureElementEntry = null
            externalAuthenticatorId = null
            externalAuthenticatorName = null
        }

        is UserAuthenticationErpModel.HealthCardWithSavedCredentials -> IdpAuthenticationDataEntityV1().apply {
            singleSignOnToken = singleSignOnTokenErpModel?.token
            singleSignOnTokenScope = SingleSignOnTokenScopeV1.AlternateAuthentication
            cardAccessNumber = this@toIdpAuthenticationDataEntityV1.cardAccessNumber
            healthCardCertificate = this@toIdpAuthenticationDataEntityV1.healthCardCertificate
            aliasOfSecureElementEntry = this@toIdpAuthenticationDataEntityV1.aliasOfSecureElementEntry
            externalAuthenticatorId = null
            externalAuthenticatorName = null
        }

        is UserAuthenticationErpModel.External -> IdpAuthenticationDataEntityV1().apply {
            singleSignOnToken = singleSignOnTokenErpModel?.token
            singleSignOnTokenScope = SingleSignOnTokenScopeV1.ExternalAuthentication
            cardAccessNumber = ""
            healthCardCertificate = null
            aliasOfSecureElementEntry = null
            externalAuthenticatorId = this@toIdpAuthenticationDataEntityV1.externalAuthenticatorId
            externalAuthenticatorName = this@toIdpAuthenticationDataEntityV1.externalAuthenticatorName
        }

        is UserAuthenticationErpModel.NotInitialized -> IdpAuthenticationDataEntityV1().apply {
            singleSignOnToken = null
            singleSignOnTokenScope = SingleSignOnTokenScopeV1.Default
            cardAccessNumber = ""
            healthCardCertificate = null
            aliasOfSecureElementEntry = null
            externalAuthenticatorId = null
            externalAuthenticatorName = null
        }
    }

fun IdpAuthenticationDataEntityV1.toUserAuthenticationErpModel(): UserAuthenticationErpModel = when {
    externalAuthenticatorId.isNotNullOrEmpty() && externalAuthenticatorName.isNotNullOrEmpty() ->
        UserAuthenticationErpModel.External(
            singleSignOnTokenErpModel = singleSignOnToken?.let { SingleSignOnTokenErpModel(it) },
            externalAuthenticatorId = externalAuthenticatorId.orEmpty(),
            externalAuthenticatorName = externalAuthenticatorName.orEmpty()
        )
    aliasOfSecureElementEntry != null &&
        healthCardCertificate != null &&
        cardAccessNumber.isNotNullOrEmpty() ->
        UserAuthenticationErpModel.HealthCardWithSavedCredentials(
            singleSignOnTokenErpModel = singleSignOnToken?.let { SingleSignOnTokenErpModel(it) },
            cardAccessNumber = cardAccessNumber,
            healthCardCertificate = healthCardCertificate!!,
            aliasOfSecureElementEntry = aliasOfSecureElementEntry!!
        )
    healthCardCertificate != null && cardAccessNumber.isNotNullOrEmpty() ->
        UserAuthenticationErpModel.HealthCard(
            singleSignOnTokenErpModel = singleSignOnToken?.let { SingleSignOnTokenErpModel(it) },
            cardAccessNumber = cardAccessNumber,
            healthCardCertificate = healthCardCertificate!!
        )
    else -> UserAuthenticationErpModel.NotInitialized
}
