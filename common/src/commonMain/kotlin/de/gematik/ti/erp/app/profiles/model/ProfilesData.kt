/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.profiles.model

import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

object ProfilesData {

    enum class Avatar {
        PersonalizedImage,
        FemaleDoctor,
        WomanWithHeadScarf,
        Grandfather,
        BoyWithHealthCard,
        OldManOfColor,
        WomanWithPhone,
        Grandmother,
        ManWithPhone,
        WheelchairUser,
        Baby,
        MaleDoctorWithPhone,
        FemaleDoctorWithPhone,
        FemaleDeveloper;

        companion object {
            val lastIndex = entries.size - 1
            const val firstIndex = 0
        }
    }

    data class Profile(
        val id: ProfileIdentifier,
        val color: ProfileColorNames,
        val avatar: Avatar,
        val image: ByteArray? = null,
        val name: String,
        val insurantName: String? = null,
        val insuranceIdentifier: String? = null,
        val insuranceName: String? = null,
        val insuranceType: InsuranceType,
        val insuranceOrganizationIdentifier: String? = null,
        val isConsentDrawerShown: Boolean,
        val lastAuthenticated: Instant? = null,
        val lastAuditEventSynced: Instant? = null,
        val lastTaskSynced: Instant? = null,
        val active: Boolean = false,
        val singleSignOnTokenScope: IdpData.SingleSignOnTokenScope?
    ) {
        //region Code block required due to using ByteArray in data class
        @Suppress("ktlint:max-line-length")
        override fun toString(): String {
            return "Profile(id='$id', color=$color, name='$name', insurantName=$insurantName, insuranceIdentifier=$insuranceIdentifier, insuranceName=$insuranceName, lastAuthenticated=$lastAuthenticated, lastAuditEventSynced=$lastAuditEventSynced, lastTaskSynced=$lastTaskSynced, active=$active, singleSignOnTokenScope=$singleSignOnTokenScope)"
        }

        @Suppress("CyclomaticComplexMethod", "LongMethod")
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Profile

            if (id != other.id) return false
            if (avatar != other.avatar) return false
            if (image != null) {
                if (other.image == null) return false
                if (!image.contentEquals(other.image)) return false
            } else if (other.image != null) return false
            if (name != other.name) return false
            if (insurantName != other.insurantName) return false
            if (insuranceIdentifier != other.insuranceIdentifier) return false
            if (insuranceName != other.insuranceName) return false
            if (lastAuthenticated != other.lastAuthenticated) return false
            if (lastAuditEventSynced != other.lastAuditEventSynced) return false
            if (lastTaskSynced != other.lastTaskSynced) return false
            if (singleSignOnTokenScope != other.singleSignOnTokenScope) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + color.hashCode()
            result = 31 * result + avatar.hashCode()
            result = 31 * result + (image?.contentHashCode() ?: 0)
            result = 31 * result + name.hashCode()
            result = 31 * result + (insurantName?.hashCode() ?: 0)
            result = 31 * result + (insuranceIdentifier?.hashCode() ?: 0)
            result = 31 * result + (insuranceName?.hashCode() ?: 0)
            result = 31 * result + (lastAuthenticated?.hashCode() ?: 0)
            result = 31 * result + (lastAuditEventSynced?.hashCode() ?: 0)
            result = 31 * result + (lastTaskSynced?.hashCode() ?: 0)
            result = 31 * result + active.hashCode()
            result = 31 * result + (singleSignOnTokenScope?.hashCode() ?: 0)
            return result
        }

        fun isSSOTokenValid(now: Instant = Clock.System.now()) = singleSignOnTokenScope?.token?.isValid(now) ?: false
        //endregion
    }

    enum class ProfileColorNames {
        SPRING_GRAY,
        SUN_DEW,
        PINK,
        TREE,
        BLUE_MOON
    }

    enum class InsuranceType {
        GKV,
        PKV,
        None
    }
}
