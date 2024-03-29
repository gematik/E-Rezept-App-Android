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

package de.gematik.ti.erp.app.profiles.usecase.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.idp.model.IdpData
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
        val active: Boolean,
        val color: ProfilesData.ProfileColorNames,
        val avatar: ProfilesData.Avatar,
        val image: ByteArray? = null,
        val lastAuthenticated: Instant? = null,
        val ssoTokenScope: IdpData.SingleSignOnTokenScope?
    ) {
        fun isBiometricPairing() = ssoTokenScope !is IdpData.ExternalAuthenticationToken

        fun isSSOTokenValid(now: Instant = Clock.System.now()) = ssoTokenScope?.token?.isValid(now) ?: false

        fun hasNoImageSelected() = this.avatar == ProfilesData.Avatar.PersonalizedImage &&
            this.image == null

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Profile

            if (id != other.id) return false
            if (name != other.name) return false
            if (insurance != other.insurance) return false
            if (active != other.active) return false
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
            result = 31 * result + active.hashCode()
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

            // old state: ssoTokenScope == null && lastAuthenticated == null
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
            fun List<Profile>.activeProfile() = first { it.active }
            fun List<Profile>.profileById(id: ProfileIdentifier?) = firstOrNull { it.id == id }
            fun List<Profile>.containsProfileWithName(name: String) = any { it.name == name.trim() }

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
        }
    }

    @Immutable
    data class PairedDevices(
        val devices: List<PairedDevice>
    )
}
