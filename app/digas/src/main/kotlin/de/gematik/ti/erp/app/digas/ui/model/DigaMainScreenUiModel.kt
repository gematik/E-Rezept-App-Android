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

package de.gematik.ti.erp.app.digas.ui.model

import androidx.compose.runtime.Composable
import de.gematik.ti.erp.app.datetime.timeStateParser
import de.gematik.ti.erp.app.fhir.model.DigaStatus
import de.gematik.ti.erp.app.timestate.TimeState
import de.gematik.ti.erp.app.timestate.getTimeState
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class DigaMainScreenUiModel(
    val name: String? = null,
    val canBeRedeemedAgain: Boolean,
    val languages: List<String>? = null,
    val supportedPlatforms: List<String>? = null,
    val medicalServicesRequired: Boolean = false,
    val additionalDevices: String? = null,
    val fee: String? = null,
    val cost: String? = null,
    val insuredPerson: String? = null,
    val prescribingPerson: String? = null,
    val institution: String? = null,
    val declineNote: String? = null,
    val code: String? = null,
    val deepLink: String? = null,
    val status: DigaStatus,
    val isArchived: Boolean = false,
    val logoUrl: String? = null,
    val lifeCycleTimestamps: DigaTimestamps,
    // Bfarm information
    val url: String? = "",
    val title: String? = null,
    val description: String? = null
)

/**
 * Represents important timestamps associated with a DiGA process.
 *
 * @property issuedOn The creation timestamp when the prescription was issued.
 * @property sentOn The timestamp when the communication (e.g., redeem code) was sent.
 * @property modifiedOn The timestamp when the dispense event occurred (e.g., redemption recorded).
 * @property expiresOn The expiration timestamp after which the redeem code or prescription is no longer valid.
 */
data class DigaTimestamps(
    val issuedOn: Instant? = null,
    val sentOn: Instant? = null,
    val modifiedOn: Instant? = null,
    val expiresOn: Instant? = null,
    val now: Instant = Clock.System.now()
) {
    /**
     * Indicates whether the DiGA has expired, based on the [expiresOn] timestamp.
     */
    val isExpired: Boolean
        get() = expiresOn?.let { it < now } ?: false

    /**
     * Calculates the [TimeState] based on [issuedOn], representing how much time has passed since issuance.
     */
    val issuedOnTimeState: String
        @Composable
        get() = issuedOn?.let { getTimeState(it) }?.let { timeStateParser(timeState = it) } ?: ""

    /**
     * Calculates the [TimeState] based on [sentOn], representing how much time has passed since the communication was sent.
     */
    val sentOnTimeState: String
        @Composable
        get() = sentOn?.let { getTimeState(it) }?.let { timeStateParser(timeState = it) } ?: ""

    /**
     * Calculates the [TimeState] based on [expiresOn], representing how much time is left until expiration (or time since expiration).
     */
    val expiresOnTimeState: String
        @Composable
        get() = expiresOn?.let { getTimeState(it) }?.let { timeStateParser(timeState = it) } ?: ""
}
