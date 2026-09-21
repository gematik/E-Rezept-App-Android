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

package de.gematik.ti.erp.app.redeem.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.pharmacy.model.ContactInformationErpModel
import de.gematik.ti.erp.app.pharmacy.model.OrderStateErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyDetailsErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyOpeningHoursErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyServiceErpModel
import de.gematik.ti.erp.app.pharmacy.model.PrescriptionInOrderErpModel
import de.gematik.ti.erp.app.shippingInfo.model.ShippingInfoErpModel.Companion.EmptyShippingInfoErpModel
import kotlinx.datetime.Instant

data class PrescriptionSelectionPreview(
    val name: String,
    val hasTeratogenicError: Boolean,
    val pharmacy: PharmacyDetailsErpModel?,
    val orders: List<PrescriptionInOrderErpModel>,
    val selectedOrders: OrderStateErpModel,
    val onCheckedChange: (PrescriptionInOrderErpModel, Boolean) -> Unit
)

class PrescriptionSelectionPreviewParameter : PreviewParameterProvider<PrescriptionSelectionPreview> {
    val time = Instant.fromEpochSeconds(1732060800)

    override val values: Sequence<PrescriptionSelectionPreview>
        get() = sequenceOf(
            PrescriptionSelectionPreview(
                name = "NoPrescriptionsSelected",
                orders = listOf(
                    PrescriptionInOrderErpModel(
                        taskId = "1",
                        accessCode = "ABC123",
                        title = "Prescription for Cold Medicine",
                        isSelfPayerPrescription = false,
                        index = 1,
                        timestamp = time,
                        substitutionsAllowed = true,
                        isScanned = false,
                        isTeratogenicPrescription = true
                    ),
                    PrescriptionInOrderErpModel(
                        taskId = "2",
                        accessCode = "DEF456",
                        title = "Prescription for Pain Relief",
                        isSelfPayerPrescription = true,
                        index = 2,
                        timestamp = time,
                        substitutionsAllowed = false,
                        isScanned = true,
                        isTeratogenicPrescription = false
                    )
                ),
                selectedOrders = OrderStateErpModel(
                    prescriptionsInOrder = emptyList(),
                    selfPayerPrescriptionIds = emptyList(),
                    contact = EmptyShippingInfoErpModel
                ),
                onCheckedChange = { _, _ -> },
                hasTeratogenicError = true,
                pharmacy = PharmacyDetailsErpModel(
                    id = "pharmacy-1",
                    name = "Muster Apotheke",
                    address = "Musterstraße 1\n12345 Berlin",
                    coordinates = null,
                    distance = null,
                    contact = ContactInformationErpModel(phone = "030123456", mail = "", url = ""),
                    provides = listOf(
                        PharmacyServiceErpModel.OnlinePharmacyServiceErpModel(
                            name = "Muster Apotheke"
                        )
                    ),
                    openingHours = PharmacyOpeningHoursErpModel(emptyMap()),
                    telematikId = "1234567890"
                )
            ),
            PrescriptionSelectionPreview(
                name = "OnePrescriptionSelected",
                orders = listOf(
                    PrescriptionInOrderErpModel(
                        taskId = "1",
                        accessCode = "ABC123",
                        title = "Prescription for Cold Medicine",
                        isSelfPayerPrescription = false,
                        index = 1,
                        timestamp = time,
                        substitutionsAllowed = true,
                        isScanned = false,
                        isTeratogenicPrescription = true
                    ),
                    PrescriptionInOrderErpModel(
                        taskId = "2",
                        accessCode = "DEF456",
                        title = "Prescription for Pain Relief",
                        isSelfPayerPrescription = true,
                        index = 2,
                        timestamp = time,
                        substitutionsAllowed = false,
                        isScanned = true,
                        isTeratogenicPrescription = false
                    )
                ),
                selectedOrders = OrderStateErpModel(
                    prescriptionsInOrder = listOf(
                        PrescriptionInOrderErpModel(
                            taskId = "1",
                            accessCode = "ABC123",
                            title = "Prescription for Cold Medicine",
                            isSelfPayerPrescription = false,
                            index = 1,
                            timestamp = time,
                            substitutionsAllowed = true,
                            isScanned = false,
                            isTeratogenicPrescription = false
                        )
                    ),
                    selfPayerPrescriptionIds = emptyList(),
                    contact = EmptyShippingInfoErpModel
                ),
                onCheckedChange = { _, _ -> },
                hasTeratogenicError = false,
                pharmacy = PharmacyDetailsErpModel(
                    id = "pharmacy-1",
                    name = "Muster Apotheke",
                    address = "Musterstraße 1\n12345 Berlin",
                    coordinates = null,
                    distance = null,
                    contact = ContactInformationErpModel(phone = "030123456", mail = "", url = ""),
                    provides = listOf(
                        PharmacyServiceErpModel.OnlinePharmacyServiceErpModel(
                            name = "Muster Apotheke"
                        )
                    ),
                    openingHours = PharmacyOpeningHoursErpModel(emptyMap()),
                    telematikId = "1234567890"
                )
            )
        )
}
