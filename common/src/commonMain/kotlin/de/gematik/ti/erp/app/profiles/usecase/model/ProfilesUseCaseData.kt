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

package de.gematik.ti.erp.app.profiles.usecase.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.model.IdpData.AlternateAuthenticationWithoutToken
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

object ProfilesUseCaseData {

    enum class InsuranceType {
        GKV,
        PKV,
        NONE
    }

    @Immutable
    data class Profile(
        val id: ProfileIdentifier,
        val name: String,
        val insurance: ProfileInsuranceInformation,
        val isActive: Boolean,
        val color: ProfilesData.ProfileColorNames,
        val avatar: ProfilesData.Avatar,
        val image: ByteArray? = null,
        val lastAuthenticated: Instant? = null,
        val ssoTokenScope: IdpData.SingleSignOnTokenScope?
    ) {
        //region Validations required before starting processes that require tokens
        fun isNotGid() = ssoTokenScope !is IdpData.ExternalAuthenticationToken

        fun isPkv() = insurance.insuranceType == InsuranceType.PKV

        fun isSSOTokenValid(now: Instant = Clock.System.now()) = ssoTokenScope?.token?.isValid(now) ?: false

        val isDirectRedeemEnabled: Boolean
            get() = lastAuthenticated == null

        fun isRedemptionAllowed() = isSSOTokenValid() || isDirectRedeemEnabled
        //endregion

        fun hasNoImageSelected() = this.avatar == ProfilesData.Avatar.PersonalizedImage &&
            this.image == null

        @Suppress("CyclomaticComplexMethod")
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Profile

            if (id != other.id) return false
            if (name != other.name) return false
            if (insurance != other.insurance) return false
            if (isActive != other.isActive) return false
            if (color != other.color) return false
            if (avatar != other.avatar) return false
            if (image != null) {
                if (other.image == null) return false
                if (!image.contentEquals(other.image)) return false
            } else if (other.image != null) return false
            if (lastAuthenticated != other.lastAuthenticated) return false
            if (ssoTokenScope != other.ssoTokenScope) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + insurance.hashCode()
            result = 31 * result + isActive.hashCode()
            result = 31 * result + color.hashCode()
            result = 31 * result + avatar.hashCode()
            result = 31 * result + (image?.contentHashCode() ?: 0)
            result = 31 * result + (lastAuthenticated?.hashCode() ?: 0)
            result = 31 * result + (ssoTokenScope?.hashCode() ?: 0)
            return result
        }

        companion object {

            enum class ProfileConnectionState {
                LoggedIn,
                LoggedOutWithoutTokenBiometrics,
                LoggedOutWithoutToken,
                LoggedOut,
                NeverConnected
            }

            private fun Profile.neverConnected() = lastAuthenticated == null

            private fun Profile.ssoTokenSetAndConnected() =
                ssoTokenScope?.token != null && ssoTokenScope.token?.isValid() == true

            private fun Profile.ssoTokenSetAndDisconnected() =
                ssoTokenScope != null && ssoTokenScope.token?.isValid() == false ||
                    lastAuthenticated != null

            private fun Profile.ssoTokenNotSet() =
                when (ssoTokenScope) {
                    is IdpData.ExternalAuthenticationToken,
                    is IdpData.AlternateAuthenticationToken,
                    is IdpData.AlternateAuthenticationWithoutToken,
                    is IdpData.DefaultToken -> ssoTokenScope.token == null

                    null -> true
                }

            private fun Profile.ssoTokenWithoutScope() =
                when (ssoTokenScope) {
                    is IdpData.AlternateAuthenticationWithoutToken -> true
                    else -> false
                }

            fun List<Profile>.activeProfile() = first { it.isActive }

            fun List<Profile>.profileById(id: ProfileIdentifier?) = firstOrNull { it.id == id }

            fun List<Profile>.containsProfileWithName(name: String) = any { it.name == name.trim() }

            // for pharmacies that do not support direct redemption this condition needs to be fulfilled
            fun Profile.isSsoTokenValidAndDirectRedeemEnabled() = isSSOTokenValid() && isDirectRedeemEnabled

            @Stable
            fun Profile.connectionState(): ProfileConnectionState? =
                when {
                    this.neverConnected() -> ProfileConnectionState.NeverConnected

                    this.ssoTokenWithoutScope() -> ProfileConnectionState.LoggedOutWithoutTokenBiometrics

                    this.ssoTokenNotSet() -> ProfileConnectionState.LoggedOutWithoutToken

                    this.ssoTokenSetAndConnected() -> ProfileConnectionState.LoggedIn

                    this.ssoTokenSetAndDisconnected() -> ProfileConnectionState.LoggedOut

                    else -> null
                }

            fun Profile.validateRequirementForLastAuthUpdateRequired(
                block: (ProfileIdentifier, Instant) -> Unit
            ): Profile {
                when {
                    ssoTokenScope != null && ssoTokenScope !is AlternateAuthenticationWithoutToken &&
                        lastAuthenticated == null -> {
                        ssoTokenScope.token?.let { token ->
                            block(id, token.validOn)
                            this@Companion
                        }
                    }
                }
                return this
            }
        }
    }

    @Immutable
    data class PairedDevices(
        val devices: List<PairedDevice>
    )
}
