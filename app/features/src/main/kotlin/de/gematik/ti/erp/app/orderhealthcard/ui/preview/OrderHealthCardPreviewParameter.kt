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

package de.gematik.ti.erp.app.orderhealthcard.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.orderhealthcard.presentation.HealthInsuranceCompany
import de.gematik.ti.erp.app.orderhealthcard.ui.preview.OrderHealthCardPreviewData.healthInsuranceCompanyData
import de.gematik.ti.erp.app.orderhealthcard.ui.preview.OrderHealthCardPreviewData.healthInsuranceCompanyDataWithSearchText

data class HealthInsuranceSearchData(
    val healthInsurance: HealthInsuranceCompany,
    val searchText: String = ""
)
class OrderHealthCardPreviewParameter : PreviewParameterProvider<HealthInsuranceSearchData> {

    override val values: Sequence<HealthInsuranceSearchData>
        get() = sequenceOf(
            healthInsuranceCompanyData,
            healthInsuranceCompanyDataWithSearchText
        )
}

object OrderHealthCardPreviewData {

    val healthInsuranceCompany = HealthInsuranceCompany(
        name = "Insurance Company",
        healthCardAndPinPhone = "+123123",
        healthCardAndPinMail = "",
        healthCardAndPinUrl = "https://www.TestURL.de/",
        pinUrl = "https://www.TestPinURL.de/",
        subjectCardAndPinMail = "testHeader",
        bodyCardAndPinMail = "testBody",
        subjectPinMail = "testHeader",
        bodyPinMail = "testBody"
    )

    val healthInsuranceCompanyData = HealthInsuranceSearchData(
        healthInsuranceCompany

    )

    val healthInsuranceCompanyDataWithSearchText = HealthInsuranceSearchData(
        healthInsuranceCompany,
        "Insurance Company G"
    )
}
