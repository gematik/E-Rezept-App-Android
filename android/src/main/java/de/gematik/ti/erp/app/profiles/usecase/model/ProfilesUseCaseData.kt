/*
 * Copyright (c) 2022 gematik GmbH
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
import de.gematik.ti.erp.app.db.entities.ProfileColorNames
import de.gematik.ti.erp.app.idp.repository.SingleSignOnToken
import java.time.Instant
import java.time.OffsetDateTime

object ProfilesUseCaseData {

    data class ProfileInsuranceInformation(
        val insurantName: String? = null,
        val insuranceIdentifier: String? = null,
        val insuranceName: String? = null,
    )

    @Immutable
    data class Profile(
        val id: Int,
        val name: String,
        val insuranceInformation: ProfileInsuranceInformation,
        val active: Boolean,
        val color: ProfileColorNames,
        val lastAuthenticated: Instant? = null,
        val ssoToken: SingleSignOnToken? = null,
        val accessToken: String? = null
    ) {
        fun ssoTokenValid(now: Instant = Instant.now()) = ssoToken?.isValid(now) ?: false
        fun connected(): Boolean =
            insuranceInformation.insurantName != null &&
                insuranceInformation.insuranceIdentifier != null &&
                insuranceInformation.insuranceName != null
    }

    @Immutable
    data class AuditEvent(
        val text: String,
        val medicationText: String?,
        val timeStamp: OffsetDateTime,
    )
}
