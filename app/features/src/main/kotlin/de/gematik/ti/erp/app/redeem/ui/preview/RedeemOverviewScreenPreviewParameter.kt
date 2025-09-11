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
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.OrderState
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Pharmacy
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PharmacyContact
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PharmacyService
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.ShippingContact
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.redeem.model.RedeemContactValidationState
import de.gematik.ti.erp.app.redeem.ui.preview.RedeemOverviewScreenPreviewParameter.contactPreviewData
import de.gematik.ti.erp.app.redeem.ui.preview.RedeemOverviewScreenPreviewParameter.pharmacyPreviewData
import de.gematik.ti.erp.app.redeem.ui.preview.RedeemOverviewScreenPreviewParameter.prescriptionsForOrdersPreviewData
import de.gematik.ti.erp.app.redeem.ui.preview.RedeemOverviewScreenPreviewParameter.profilePreviewData
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Instant

class PrescriptionSelectionSectionParameter : PreviewParameterProvider<List<PharmacyUseCaseData.PrescriptionInOrder>> {
    override val values = sequenceOf(prescriptionsForOrdersPreviewData, emptyList())
}

data class RedeemOverviewScreenPreviewData(
    val title: String,
    val activeProfile: UiState<ProfilesUseCaseData.Profile>,
    val prescriptions: List<PharmacyUseCaseData.PrescriptionInOrder>,
    val orderOption: PharmacyScreenData.OrderOption?,
    val markAsSelfPayer: Boolean,
    val pharmacy: Pharmacy?,
    val contactValidationState: RedeemContactValidationState,
    val contact: ShippingContact,
    val isRedeemEnabled: Boolean,
    val isPrescriptionError: Boolean,
    val isPharmacyError: Boolean,
    val isContactError: Boolean
) {
    fun orderState() = OrderState(
        prescriptionsInOrder = prescriptions,
        selfPayerPrescriptionIds = if (markAsSelfPayer) prescriptions.map { it.taskId } else emptyList(),
        contact = contact
    )
}

class RedeemOverviewScreenParameter : PreviewParameterProvider<RedeemOverviewScreenPreviewData> {
    override val values: Sequence<RedeemOverviewScreenPreviewData>
        get() = sequenceOf(
            // normal order
            RedeemOverviewScreenPreviewData(
                title = "normal_order",
                activeProfile = profilePreviewData,
                prescriptions = prescriptionsForOrdersPreviewData,
                orderOption = PharmacyScreenData.OrderOption.Delivery,
                markAsSelfPayer = false,
                contactValidationState = RedeemContactValidationState.NoError,
                pharmacy = pharmacyPreviewData,
                contact = contactPreviewData,
                isRedeemEnabled = true,
                isPrescriptionError = false,
                isPharmacyError = false,
                isContactError = false
            ),
            // self payer order
            RedeemOverviewScreenPreviewData(
                title = "self_payer_order",
                activeProfile = profilePreviewData,
                prescriptions = prescriptionsForOrdersPreviewData,
                orderOption = PharmacyScreenData.OrderOption.Online,
                markAsSelfPayer = true,
                contactValidationState = RedeemContactValidationState.NoError,
                pharmacy = pharmacyPreviewData,
                contact = contactPreviewData,
                isRedeemEnabled = true,
                isPrescriptionError = false,
                isPharmacyError = false,
                isContactError = false
            ),
            // missing pharmacy
            RedeemOverviewScreenPreviewData(
                title = "missing_pharmacy_order",
                activeProfile = profilePreviewData,
                prescriptions = prescriptionsForOrdersPreviewData,
                orderOption = null,
                markAsSelfPayer = false,
                contactValidationState = RedeemContactValidationState.MissingPhone,
                pharmacy = null,
                contact = contactPreviewData.copy(telephoneNumber = ""),
                isRedeemEnabled = false,
                isPrescriptionError = false,
                isPharmacyError = true,
                isContactError = true
            ),
            // missing contact
            RedeemOverviewScreenPreviewData(
                title = "missing_contact_order",
                activeProfile = profilePreviewData,
                prescriptions = prescriptionsForOrdersPreviewData,
                orderOption = PharmacyScreenData.OrderOption.Online,
                markAsSelfPayer = false,
                contactValidationState = RedeemContactValidationState.MissingPersonalInfo,
                pharmacy = pharmacyPreviewData,
                contact = ShippingContact.EmptyShippingContact,
                isRedeemEnabled = false,
                isPrescriptionError = false,
                isPharmacyError = false,
                isContactError = true
            ),
            // missing prescriptions
            RedeemOverviewScreenPreviewData(
                title = "missing_prescription_order",
                activeProfile = profilePreviewData,
                prescriptions = emptyList(),
                orderOption = PharmacyScreenData.OrderOption.Pickup,
                markAsSelfPayer = false,
                contactValidationState = RedeemContactValidationState.NoError,
                pharmacy = pharmacyPreviewData,
                contact = contactPreviewData,
                isRedeemEnabled = false,
                isPrescriptionError = true,
                isPharmacyError = false,
                isContactError = false
            )
        )
}

object RedeemOverviewScreenPreviewParameter {

    val profilePreviewData = UiState.Data(
        ProfilesUseCaseData.Profile(
            id = "test-profile-1",
            name = "Ada Muster",
            insurance = ProfileInsuranceInformation(
                insuranceIdentifier = "123456789",
                insuranceName = "Test Insurance",
                insuranceType = ProfilesUseCaseData.InsuranceType.GKV
            ),
            isActive = true,
            color = ProfilesData.ProfileColorNames.BLUE_MOON,
            avatar = ProfilesData.Avatar.FemaleDoctor,
            image = null,
            lastAuthenticated = Instant.parse("2024-03-20T10:00:00Z"),
            ssoTokenScope = null
        )
    )

    private val prescriptionForOrderPreviewData = PharmacyUseCaseData.PrescriptionInOrder(
        taskId = "taskId",
        accessCode = "access-code-1",
        title = "Prescription",
        isSelfPayerPrescription = false,
        index = 1,
        timestamp = Instant.parse("2024-08-01T10:00:00Z"),
        substitutionsAllowed = false,
        isScanned = false
    )

    val contactPreviewData = ShippingContact(
        name = "Ubelix Ewiglangername",
        line1 = "Kantstraße 149",
        line2 = "",
        postalCode = "12099",
        city = "Berlin",
        telephoneNumber = "01653 387123199",
        mail = "mailaddresse@provider.de",
        deliveryInformation = "Bitte im Vordherhaus abgeben."
    )
    val prescriptionsForOrdersPreviewData = listOf(
        prescriptionForOrderPreviewData,
        prescriptionForOrderPreviewData.copy(
            title = "Other Prescription",
            taskId = "taskId2"
        ),
        prescriptionForOrderPreviewData.copy(
            title = "Unwanted Prescription",
            taskId = "taskId3"
        )
    )

    val pharmacyPreviewData = Pharmacy(
        id = "PHARMACY_ID",
        name = "Pharmacy With a Very Long Name",
        address = "Pharmacy Str,\n12345 Pharmacy City",
        coordinates = null,
        distance = null,
        contact = PharmacyContact(
            "1234",
            "mail@web.de",
            "https://www.gematik.de"
        ),
        provides = listOf(
            PharmacyService.OnlinePharmacyService(name = "Online"),
            PharmacyService.PickUpPharmacyService(name = "PickUp"),
            PharmacyService.LocalPharmacyService(
                name = "Local",
                openingHours = PharmacyUseCaseData.OpeningHours(emptyMap())
            ),
            PharmacyService.DeliveryPharmacyService(
                name = "Delivery",
                openingHours = PharmacyUseCaseData.OpeningHours(emptyMap())
            )
        ),
        openingHours = PharmacyUseCaseData.OpeningHours(emptyMap()),
        telematikId = "TELEMATIK_ID"
    )
}
