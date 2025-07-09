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
import de.gematik.ti.erp.app.pharmacy.usecase.ShippingContactState
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData

data class ShippingContactPreviewData(
    val name: String,
    val validShippingContactState: ShippingContactState.ValidShippingContactState? = null,
    val invalidShippingContactState: ShippingContactState.InvalidShippingContactState? = null,
    val errorShippingContactState: ShippingContactState.InvalidShippingContactState? = null,
    val shippingContact: PharmacyUseCaseData.ShippingContact
)

class RedeemEditShippingPreviewParameter : PreviewParameterProvider<ShippingContactPreviewData> {

    override val values: Sequence<ShippingContactPreviewData>
        get() = sequenceOf(
            ShippingContactPreviewData(
                name = "ValidShippingState",
                validShippingContactState = ShippingContactState.ValidShippingContactState.OK,
                shippingContact = PharmacyUseCaseData.ShippingContact(
                    name = "John Doe",
                    line1 = "123 Main St",
                    line2 = "Apt 4B",
                    postalCode = "12345",
                    city = "Metropolis",
                    telephoneNumber = "555-1234",
                    mail = "john.doe@example.com",
                    deliveryInformation = "Leave at the front door"
                )
            ),
            ShippingContactPreviewData(
                name = "InvalidShippingState",
                invalidShippingContactState = ShippingContactState.InvalidShippingContactState(
                    errorList = listOf(
                        ShippingContactState.ShippingContactError.InvalidName,
                        ShippingContactState.ShippingContactError.InvalidLine1,
                        ShippingContactState.ShippingContactError.InvalidLine2,
                        ShippingContactState.ShippingContactError.InvalidPostalCode,
                        ShippingContactState.ShippingContactError.InvalidCity,
                        ShippingContactState.ShippingContactError.InvalidPhoneNumber,
                        ShippingContactState.ShippingContactError.InvalidMail,
                        ShippingContactState.ShippingContactError.InvalidDeliveryInformation
                    )
                ),
                shippingContact = PharmacyUseCaseData.ShippingContact(
                    name = "!@#$%^&*()",
                    line1 = "@#@#",
                    line2 = "#$#$#",
                    postalCode = "1",
                    city = "@",
                    telephoneNumber = "#$#$",
                    mail = "",
                    deliveryInformation = "!!#@!@#"
                )
            ),
            ShippingContactPreviewData(
                name = "ErrorShippingState",
                errorShippingContactState = ShippingContactState.InvalidShippingContactState(
                    errorList = listOf(
                        ShippingContactState.ShippingContactError.EmptyName,
                        ShippingContactState.ShippingContactError.EmptyLine1,
                        ShippingContactState.ShippingContactError.EmptyPostalCode,
                        ShippingContactState.ShippingContactError.EmptyCity,
                        ShippingContactState.ShippingContactError.EmptyPhoneNumber,
                        ShippingContactState.ShippingContactError.EmptyMail
                    )
                ),
                shippingContact = PharmacyUseCaseData.ShippingContact(
                    name = "",
                    line1 = "",
                    line2 = "",
                    postalCode = "",
                    city = "",
                    telephoneNumber = "",
                    mail = "",
                    deliveryInformation = ""
                )
            )
        )
}
