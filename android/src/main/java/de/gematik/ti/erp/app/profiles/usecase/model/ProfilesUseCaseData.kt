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
    data class ProfileInsuranceInformation(
        val insurantName: String = "",
        val insuranceIdentifier: String = "",
        val insuranceName: String = "",
        val insuranceType: InsuranceType = InsuranceType.NONE
    )

    @Immutable
    data class Profile(
        val id: ProfileIdentifier,
        val name: String,
        val insuranceInformation: ProfileInsuranceInformation,
        val active: Boolean,
        val color: ProfilesData.ProfileColorNames,
        val avatarFigure: ProfilesData.AvatarFigure,
        val personalizedImage: ByteArray? = null,
        val lastAuthenticated: Instant? = null,
        val ssoTokenScope: IdpData.SingleSignOnTokenScope?
    ) {
        fun ssoTokenValid(now: Instant = Clock.System.now()) = ssoTokenScope?.token?.isValid(now) ?: false
        fun hasNoImageSelected() = this.avatarFigure == ProfilesData.AvatarFigure.PersonalizedImage &&
            this.personalizedImage == null

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Profile

            if (id != other.id) return false
            if (name != other.name) return false
            if (insuranceInformation != other.insuranceInformation) return false
            if (active != other.active) return false
            if (color != other.color) return false
            if (avatarFigure != other.avatarFigure) return false
            if (personalizedImage != null) {
                if (other.personalizedImage == null) return false
                if (!personalizedImage.contentEquals(other.personalizedImage)) return false
            } else if (other.personalizedImage != null) return false
            if (lastAuthenticated != other.lastAuthenticated) return false
            if (ssoTokenScope != other.ssoTokenScope) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + insuranceInformation.hashCode()
            result = 31 * result + active.hashCode()
            result = 31 * result + color.hashCode()
            result = 31 * result + avatarFigure.hashCode()
            result = 31 * result + (personalizedImage?.contentHashCode() ?: 0)
            result = 31 * result + (lastAuthenticated?.hashCode() ?: 0)
            result = 31 * result + (ssoTokenScope?.hashCode() ?: 0)
            return result
        }
    }

    @Immutable
    data class PairedDevice(
        val name: String,
        val alias: String,
        val connectedOn: Instant
    ) {
        @Stable
        fun isOurDevice(alias: String) = this.alias == alias
    }

    @Immutable
    data class PairedDevices(
        val devices: List<PairedDevice>
    )
}
