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

package de.gematik.ti.erp.app.orderhealthcard.usecase.model

import androidx.compose.runtime.Immutable
import com.squareup.moshi.JsonClass

object HealthCardOrderUseCaseData {
    @Immutable
    @JsonClass(generateAdapter = true)
    data class HealthInsuranceCompany(
        val name: String,
        val healthCardAndPinPhone: String?,
        val healthCardAndPinMail: String?,
        val healthCardAndPinUrl: String?,
        val pinUrl: String?,
        val subjectCardAndPinMail: String?,
        val bodyCardAndPinMail: String?,
        val subjectPinMail: String?,
        val bodyPinMail: String?,
    ) {
        fun noContactInformation() =
            healthCardAndPinPhone.isNullOrEmpty() &&
                healthCardAndPinMail.isNullOrEmpty() &&
                healthCardAndPinUrl.isNullOrEmpty() &&
                pinUrl.isNullOrEmpty()

        fun hasContactInfoForPin() =
            !pinUrl.isNullOrEmpty()

        fun hasContactInfoForHealthCardAndPin() =
            !healthCardAndPinPhone.isNullOrEmpty() || !healthCardAndPinMail.isNullOrEmpty() || !healthCardAndPinUrl.isNullOrEmpty()

        fun hasMailContentForCardAndPin() = !subjectCardAndPinMail.isNullOrEmpty() && !bodyCardAndPinMail.isNullOrEmpty()

        fun hasMailContentForPin() = !subjectPinMail.isNullOrEmpty() && !bodyPinMail.isNullOrEmpty()
    }
}
