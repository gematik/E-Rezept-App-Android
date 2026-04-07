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

package de.gematik.ti.erp.app.pharmacy.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.pharmacy.model.PharmacyAddressErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyErpModel
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import kotlinx.datetime.Instant

data class PharmacyStartScreenPreviewData(
    val isModalFlow: Boolean,
    val favouritePharmacies: List<PharmacyErpModel>,
    val previewCoordinates: PharmacyUseCaseData.Coordinates,
    val isGooglePlayServicesAvailable: Boolean
)

class PharmacyStartScreenPreviewParameterProvider : PreviewParameterProvider<PharmacyStartScreenPreviewData> {
    override val values = sequenceOf(
        PharmacyStartScreenPreviewData(
            isModalFlow = false,
            favouritePharmacies = listOf(
                pharmacy(
                    telematikId = "123456789",
                    name = "Berlin Apotheke",
                    lineAddress = "Berliner Strasse 123",
                    city = "Berlin",
                    zip = "12345",
                    isFavorite = true
                ),
                pharmacy(
                    telematikId = "123456788",
                    name = "Stuttgart Apotheke",
                    lineAddress = "StuttgartStr 12345",
                    city = "Stuttgart",
                    zip = "12345",
                    isFavorite = true
                )
            ),
            previewCoordinates = PharmacyUseCaseData.Coordinates(52.51947562977698, 13.404335795642881),
            isGooglePlayServicesAvailable = true
        ),
        PharmacyStartScreenPreviewData(
            isModalFlow = true,
            favouritePharmacies = emptyList(),
            previewCoordinates = PharmacyUseCaseData.Coordinates(48.137154, 11.576124),
            isGooglePlayServicesAvailable = false
        )
    )

    private fun pharmacy(
        telematikId: String,
        name: String,
        lineAddress: String,
        city: String,
        zip: String,
        isFavorite: Boolean
    ) = PharmacyErpModel(
        lastUsed = Instant.parse("2022-01-01T00:00:00Z"),
        isFavorite = isFavorite,
        usageCount = 1,
        telematikId = telematikId,
        name = name,
        address = PharmacyAddressErpModel(
            lineAddress = lineAddress,
            city = city,
            zip = zip
        ),
        contact = null
    )
}
