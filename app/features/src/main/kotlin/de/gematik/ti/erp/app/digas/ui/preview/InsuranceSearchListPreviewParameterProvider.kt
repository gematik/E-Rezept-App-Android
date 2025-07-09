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

package de.gematik.ti.erp.app.digas.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.digas.ui.model.InsuranceUiModel
import de.gematik.ti.erp.app.digas.util.InsuranceDrawableUtil
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.utils.uistate.UiState

/**
 * Preview parameter provider for the InsuranceSearchListScreen.
 * Provides different states for the insurance search list screen.
 */
class InsuranceSearchListPreviewParameterProvider : PreviewParameterProvider<InsuranceSearchListPreviewData> {
    override val values = sequenceOf(
        InsuranceSearchListPreviewData(
            name = "Loading state",
            uiState = UiState.Loading()
        ),
        InsuranceSearchListPreviewData(
            name = "Empty state",
            uiState = UiState.Empty()
        ),
        InsuranceSearchListPreviewData(
            name = "Error state",
            uiState = UiState.Error(Exception("Failed to load insurance list"))
        ),
        InsuranceSearchListPreviewData(
            name = "Data state",
            uiState = UiState.Data(mockInsuranceUiModelList)
        ),
        InsuranceSearchListPreviewData(
            name = "Filtered state",
            uiState = UiState.Data(mockInsuranceUiModelList.filter { it.name.contains("AOK", ignoreCase = true) }),
            searchTerm = "AOK"
        )
    )
}

data class InsuranceSearchListPreviewData(
    val name: String,
    val uiState: UiState<List<InsuranceUiModel>>,
    val searchTerm: String = ""
)

// Convert PharmacyUseCaseData.Pharmacy objects to InsuranceUiModel objects
val mockInsuranceUiModelList = listOf(
    InsuranceUiModel(
        pharmacy = PharmacyUseCaseData.Pharmacy(
            id = "1",
            name = "AOK Nordost - Die Gesundheitskasse",
            address = null,
            coordinates = null,
            distance = null,
            contact = PharmacyUseCaseData.PharmacyContact(
                phone = "0800 2650800",
                mail = "service@nordost.aok.de",
                url = "https://www.aok.de/pk/nordost/",
                pickUpUrl = "",
                deliveryUrl = "",
                onlineServiceUrl = ""
            ),
            provides = listOf(
                PharmacyUseCaseData.PharmacyService.OnlinePharmacyService(
                    name = "Online Service"
                )
            ),
            openingHours = null,
            telematikId = "107519005"
        ),
        drawableResourceId = InsuranceDrawableUtil.getDrawableResourceForTelematikId("ic_8010000000015")
    ),
    InsuranceUiModel(
        pharmacy = PharmacyUseCaseData.Pharmacy(
            id = "2",
            name = "BARMER",
            address = null,
            coordinates = null,
            distance = null,
            contact = PharmacyUseCaseData.PharmacyContact(
                phone = "0800 3331010",
                mail = "service@barmer.de",
                url = "https://www.barmer.de",
                pickUpUrl = "",
                deliveryUrl = "",
                onlineServiceUrl = ""
            ),
            provides = listOf(
                PharmacyUseCaseData.PharmacyService.OnlinePharmacyService(
                    name = "Online Service"
                )
            ),
            openingHours = null,
            telematikId = "104204002"
        ),
        drawableResourceId = InsuranceDrawableUtil.getDrawableResourceForTelematikId("ic_8010000000019")
    ),
    InsuranceUiModel(
        pharmacy = PharmacyUseCaseData.Pharmacy(
            id = "3",
            name = "Techniker Krankenkasse",
            address = null,
            coordinates = null,
            distance = null,
            contact = PharmacyUseCaseData.PharmacyContact(
                phone = "0800 2858585",
                mail = "service@tk.de",
                url = "https://www.tk.de",
                pickUpUrl = "",
                deliveryUrl = "",
                onlineServiceUrl = ""
            ),
            provides = listOf(
                PharmacyUseCaseData.PharmacyService.OnlinePharmacyService(
                    name = "Online Service"
                )
            ),
            openingHours = null,
            telematikId = "101575519"
        ),
        drawableResourceId = InsuranceDrawableUtil.getDrawableResourceForTelematikId("ic_8010000000001")
    ),
    InsuranceUiModel(
        pharmacy = PharmacyUseCaseData.Pharmacy(
            id = "4",
            name = "DAK-Gesundheit",
            address = null,
            coordinates = null,
            distance = null,
            contact = PharmacyUseCaseData.PharmacyContact(
                phone = "040 325325555",
                mail = "service@dak.de",
                url = "https://www.dak.de",
                pickUpUrl = "",
                deliveryUrl = "",
                onlineServiceUrl = ""
            ),
            provides = listOf(
                PharmacyUseCaseData.PharmacyService.OnlinePharmacyService(
                    name = "Online Service"
                )
            ),
            openingHours = null,
            telematikId = "101884516"
        ),
        drawableResourceId = InsuranceDrawableUtil.getDrawableResourceForTelematikId("ic_8010000000024")
    ),
    InsuranceUiModel(
        pharmacy = PharmacyUseCaseData.Pharmacy(
            id = "5",
            name = "IKK classic",
            address = null,
            coordinates = null,
            distance = null,
            contact = PharmacyUseCaseData.PharmacyContact(
                phone = "0800 4551111",
                mail = "service@ikk-classic.de",
                url = "https://www.ikk-classic.de",
                pickUpUrl = "",
                deliveryUrl = "",
                onlineServiceUrl = ""
            ),
            provides = listOf(
                PharmacyUseCaseData.PharmacyService.OnlinePharmacyService(
                    name = "Online Service"
                )
            ),
            openingHours = null,
            telematikId = "101084567"
        ),
        drawableResourceId = InsuranceDrawableUtil.getDrawableResourceForTelematikId("101084567")
    )
)
