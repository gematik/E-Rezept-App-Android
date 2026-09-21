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

package de.gematik.ti.erp.app.profile.model

import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.time.Duration.Companion.minutes

private val SYNC_TIMESTAMP_GRACE_PERIOD = 1.minutes

@Serializable
data class ProfileErpModel(
    val id: ProfileIdentifier,
    val active: Boolean,
    val isNewlyCreated: Boolean,

    val name: String,
    val profileImageData: ProfileImageDataErpModel,

    val insuranceData: ProfileInsuranceDataErpModel,
    val isConsentDrawerShown: Boolean,

    // Timestamps
    val lastAuthenticated: Instant?,
    val lastAuditEventSynced: Instant?,
    val lastTaskSynced: Instant?,

    val userAuthentication: UserAuthenticationErpModel
) {
    fun isPkv() = insuranceData.isPkv()

    fun isPkvOrBund() = insuranceData.isPkvOrBund()

    fun hasNoImageSelected() = profileImageData.hasNoImageSelected()

    fun isSSOTokenValid(now: Instant = Clock.System.now()) = userAuthentication.singleSignOnTokenErpModel?.isValid(now) ?: false

    fun isRedemptionAllowed() = isSSOTokenValid()

    fun hasChanged(other: ProfileErpModel): Boolean =
        id != other.id ||
            active != other.active ||
            isNewlyCreated != other.isNewlyCreated ||
            name != other.name ||
            profileImageData.color != other.profileImageData.color ||
            profileImageData.avatar != other.profileImageData.avatar ||
            !profileImageData.image.contentEquals(other.profileImageData.image) ||
            insuranceData != other.insuranceData ||
            isConsentDrawerShown != other.isConsentDrawerShown ||
            !lastAuthenticated.isWithinGracePeriod(other.lastAuthenticated) ||
            !lastTaskSynced.isWithinGracePeriod(other.lastTaskSynced) ||
            !lastAuditEventSynced.isWithinGracePeriod(other.lastAuditEventSynced) ||
            userAuthentication != other.userAuthentication

    companion object {
        enum class ProfileConnectionState {
            LoggedIn,
            LoggedOutWithoutTokenBiometrics,
            LoggedOutWithoutToken,
            LoggedOut,
            NeverConnected
        }

        fun ProfileErpModel.connectionState(): ProfileConnectionState? =
            when {
                neverConnected() -> ProfileConnectionState.NeverConnected

                ssoTokenWithoutScope() -> ProfileConnectionState.LoggedOutWithoutTokenBiometrics

                ssoTokenNotSet() -> ProfileConnectionState.LoggedOutWithoutToken

                ssoTokenSetAndConnected() -> ProfileConnectionState.LoggedIn

                ssoTokenSetAndDisconnected() -> ProfileConnectionState.LoggedOut

                else -> null
            }

        private fun ProfileErpModel.neverConnected() = lastAuthenticated == null

        private fun ProfileErpModel.ssoTokenSetAndConnected() =
            userAuthentication.singleSignOnTokenErpModel != null && userAuthentication.singleSignOnTokenErpModel?.isValid() == true

        private fun ProfileErpModel.ssoTokenSetAndDisconnected() =
            userAuthentication.singleSignOnTokenErpModel != null && userAuthentication.singleSignOnTokenErpModel?.isValid() == false ||
                lastAuthenticated != null

        private fun ProfileErpModel.ssoTokenNotSet() = userAuthentication.singleSignOnTokenErpModel == null

        private fun ProfileErpModel.ssoTokenWithoutScope() =
            userAuthentication is UserAuthenticationErpModel.HealthCardWithSavedCredentials &&
                userAuthentication.singleSignOnTokenErpModel == null

        fun ProfileErpModel.validateRequirementForLastAuthUpdateRequired(
            block: (ProfileIdentifier, Instant) -> Unit
        ): ProfileErpModel {
            when {
                !(
                    userAuthentication is UserAuthenticationErpModel.HealthCardWithSavedCredentials &&
                        userAuthentication.singleSignOnTokenErpModel == null
                    ) && lastAuthenticated == null -> {
                    userAuthentication.singleSignOnTokenErpModel?.let { token ->
                        block(id, token.validOn)
                        this@Companion
                    }
                }
            }
            return this
        }

        fun List<ProfileErpModel>.activeProfile() = first { it.active }

        fun List<ProfileErpModel>.profileById(id: ProfileIdentifier?) = firstOrNull { it.id == id }

        fun List<ProfileErpModel>.containsProfileWithName(name: String) = any { it.name == name.trim() }

        fun ProfileErpModel.toJson(): String = SafeJson.value.encodeToString(this)
    }
}

private fun Instant?.isWithinGracePeriod(other: Instant?): Boolean {
    if (this == null && other == null) return true
    if (this == null || other == null) return false
    return abs((this - other).inWholeMilliseconds) <= SYNC_TIMESTAMP_GRACE_PERIOD.inWholeMilliseconds
}
