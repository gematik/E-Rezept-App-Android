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

package de.gematik.ti.erp.app.eurezept.domain.model

import androidx.compose.ui.graphics.ImageBitmap
import de.gematik.ti.erp.app.eurezept.model.EuAccessCode
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

enum class PrescriptionFilter {
    ALL,
    EU_REDEEMABLE_ONLY
}

enum class EuPrescriptionType {
    Scanned,
    FreeText,
    BTM,
    Ingredient,
    EuRedeemable,
    Unknown
}

data class EuPrescription(
    val profileIdentifier: ProfileIdentifier,
    val id: String,
    val name: String,
    val type: EuPrescriptionType,
    val isMarkedAsEuRedeemableByPatientAuthorization: Boolean,
    val isMarkedAsError: Boolean = false,
    val isLoading: Boolean = false,
    val expiryDate: Instant? = null
)

data class Country(
    val name: String,
    val code: String,
    val flagEmoji: String = "🇪🇺"
)

data class CountryPhrases(
    val flagEmoji: String,
    val redeemPrescriptionPhrase: String,
    val thankYouPhrase: String
)

data class CountrySpecificLabels(
    val codeLabel: String,
    val insuranceNumberLabel: String
)

data class EuRedeemError(
    val euPrescription: EuPrescription,
    val error: Throwable
)

data class EuRedemptionDetails(
    val euAccessCode: EuAccessCode,
    val insuranceNumber: String,
    val qrCodeBitmap: ImageBitmap?,
    val isExpired: Boolean = euAccessCode.validUntil < Clock.System.now()
) {
    fun calculateIsExpired(): Boolean = euAccessCode.validUntil < Clock.System.now()
}
