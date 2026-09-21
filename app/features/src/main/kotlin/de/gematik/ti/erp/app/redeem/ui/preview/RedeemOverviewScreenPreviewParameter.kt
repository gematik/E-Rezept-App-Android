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
import de.gematik.ti.erp.app.pharmacy.model.OrderOptionErpModel
import de.gematik.ti.erp.app.pharmacy.model.OrderStateErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyDetailsErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyOpeningHoursErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyServiceErpModel
import de.gematik.ti.erp.app.pharmacy.model.PrescriptionInOrderErpModel
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.redeem.model.RedeemContactValidationState
import de.gematik.ti.erp.app.redeem.ui.preview.RedeemOverviewScreenPreviewParameter.contactPreviewData
import de.gematik.ti.erp.app.redeem.ui.preview.RedeemOverviewScreenPreviewParameter.pharmacyPreviewData
import de.gematik.ti.erp.app.redeem.ui.preview.RedeemOverviewScreenPreviewParameter.prescriptionsForOrdersPreviewData
import de.gematik.ti.erp.app.redeem.ui.preview.RedeemOverviewScreenPreviewParameter.prescriptionsForOrdersPreviewDataTeratogenic
import de.gematik.ti.erp.app.redeem.ui.preview.RedeemOverviewScreenPreviewParameter.profilePreviewData
import de.gematik.ti.erp.app.shippingInfo.model.ShippingInfoErpModel
import de.gematik.ti.erp.app.shippingInfo.model.ShippingInfoErpModel.Companion.EmptyShippingInfoErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Instant

class PrescriptionSelectionSectionParameter : PreviewParameterProvider<List<PrescriptionInOrderErpModel>> {
    override val values = sequenceOf(prescriptionsForOrdersPreviewData, emptyList())
}

data class RedeemOverviewScreenPreviewData(
    val title: String,
    val activeProfile: UiState<ProfileErpModel>,
    val prescriptions: List<PrescriptionInOrderErpModel>,
    val orderOption: OrderOptionErpModel?,
    val markAsSelfPayer: Boolean,
    val pharmacy: PharmacyDetailsErpModel?,
    val contactValidationState: RedeemContactValidationState,
    val contact: ShippingInfoErpModel,
    val isRedeemEnabled: Boolean,
    val isPrescriptionError: Boolean,
    val isPharmacyError: Boolean,
    val isContactError: Boolean,
    val hasTeratogenicPrescriptionError: Boolean
) {
    fun orderState() = OrderStateErpModel(
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
                orderOption = OrderOptionErpModel.Delivery,
                markAsSelfPayer = false,
                contactValidationState = RedeemContactValidationState.NoError,
                pharmacy = pharmacyPreviewData,
                contact = contactPreviewData,
                isRedeemEnabled = true,
                isPrescriptionError = false,
                isPharmacyError = false,
                isContactError = false,
                hasTeratogenicPrescriptionError = false
            ),
            // self payer order
            RedeemOverviewScreenPreviewData(
                title = "self_payer_order",
                activeProfile = profilePreviewData,
                prescriptions = prescriptionsForOrdersPreviewData,
                orderOption = OrderOptionErpModel.Online,
                markAsSelfPayer = true,
                contactValidationState = RedeemContactValidationState.NoError,
                pharmacy = pharmacyPreviewData,
                contact = contactPreviewData,
                isRedeemEnabled = true,
                isPrescriptionError = false,
                isPharmacyError = false,
                isContactError = false,
                hasTeratogenicPrescriptionError = false
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
                contact = contactPreviewData.copy(phone = ""),
                isRedeemEnabled = false,
                isPrescriptionError = false,
                isPharmacyError = true,
                isContactError = true,
                hasTeratogenicPrescriptionError = false
            ),
            // missing contact
            RedeemOverviewScreenPreviewData(
                title = "missing_contact_order",
                activeProfile = profilePreviewData,
                prescriptions = prescriptionsForOrdersPreviewData,
                orderOption = OrderOptionErpModel.Online,
                markAsSelfPayer = false,
                contactValidationState = RedeemContactValidationState.MissingPersonalInfo,
                pharmacy = pharmacyPreviewData,
                contact = EmptyShippingInfoErpModel,
                isRedeemEnabled = false,
                isPrescriptionError = false,
                isPharmacyError = false,
                isContactError = true,
                hasTeratogenicPrescriptionError = false
            ),
            // missing prescriptions
            RedeemOverviewScreenPreviewData(
                title = "missing_prescription_order",
                activeProfile = profilePreviewData,
                prescriptions = emptyList(),
                orderOption = OrderOptionErpModel.Pickup,
                markAsSelfPayer = false,
                contactValidationState = RedeemContactValidationState.NoError,
                pharmacy = pharmacyPreviewData,
                contact = contactPreviewData,
                isRedeemEnabled = false,
                isPrescriptionError = true,
                isPharmacyError = false,
                isContactError = false,
                hasTeratogenicPrescriptionError = false
            ),
            // teratogenic prescription error
            RedeemOverviewScreenPreviewData(
                title = "teratogenic_prescription_error",
                activeProfile = profilePreviewData,
                prescriptions = prescriptionsForOrdersPreviewDataTeratogenic,
                orderOption = OrderOptionErpModel.Online,
                markAsSelfPayer = false,
                contactValidationState = RedeemContactValidationState.NoError,
                pharmacy = pharmacyPreviewData,
                contact = contactPreviewData,
                isRedeemEnabled = false,
                isPrescriptionError = false,
                isPharmacyError = false,
                isContactError = false,
                hasTeratogenicPrescriptionError = true
            )
        )
}

object RedeemOverviewScreenPreviewParameter {

    val profilePreviewData = UiState.Data(
        ProfileErpModel(
            id = "test-profile-1",
            name = "Ada Muster",
            insuranceData = ProfileInsuranceDataErpModel(
                insurantName = "Ada Muster",
                insuranceIdentifier = "123456789",
                insuranceName = "Test Insurance",
                insuranceType = InsuranceType.GKV,
                organizationIdentifier = null
            ),
            active = true,
            isNewlyCreated = false,
            profileImageData = ProfileImageDataErpModel(
                color = ProfileColorNames.BLUE_MOON,
                avatar = Avatar.FemaleDoctor,
                image = null
            ),
            isConsentDrawerShown = false,
            lastAuthenticated = Instant.parse("2024-03-20T10:00:00Z"),
            lastAuditEventSynced = null,
            lastTaskSynced = null,
            userAuthentication = UserAuthenticationErpModel.NotInitialized
        )
    )

    private val prescriptionForOrderPreviewData = PrescriptionInOrderErpModel(
        taskId = "taskId",
        accessCode = "access-code-1",
        title = "Prescription",
        isSelfPayerPrescription = false,
        index = 1,
        timestamp = Instant.parse("2024-08-01T10:00:00Z"),
        substitutionsAllowed = false,
        isScanned = false,
        isTeratogenicPrescription = false
    )

    val contactPreviewData = ShippingInfoErpModel(
        name = "Ubelix Ewiglangername",
        street = "Kantstraße 149",
        addressDetail = "",
        zip = "12099",
        city = "Berlin",
        phone = "01653 387123199",
        mail = "mailaddresse@provider.de",
        deliveryInfo = "Bitte im Vordherhaus abgeben."
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

    val prescriptionsForOrdersPreviewDataTeratogenic = listOf(
        prescriptionForOrderPreviewData,
        prescriptionForOrderPreviewData.copy(
            title = "Other Prescription",
            taskId = "taskId2",
            isTeratogenicPrescription = true
        ),
        prescriptionForOrderPreviewData.copy(
            title = "Unwanted Prescription",
            taskId = "taskId3"
        )
    )

    val pharmacyPreviewData = PharmacyDetailsErpModel(
        id = "PHARMACY_ID",
        name = "PharmacyDetailsErpModel With a Very Long Name",
        address = "PharmacyDetailsErpModel Str,\n12345 PharmacyDetailsErpModel City",
        coordinates = null,
        distance = null,
        contact = ContactInformationErpModel(
            "1234",
            "mail@web.de",
            "https://www.gematik.de"
        ),
        provides = listOf(
            PharmacyServiceErpModel.OnlinePharmacyServiceErpModel(name = "Online"),
            PharmacyServiceErpModel.PickUpPharmacyServiceErpModel(name = "PickUp"),
            PharmacyServiceErpModel.LocalPharmacyServiceErpModel(
                name = "Local",
                openingHours = PharmacyOpeningHoursErpModel(emptyMap())
            ),
            PharmacyServiceErpModel.DeliveryPharmacyServiceErpModel(
                name = "Delivery",
                openingHours = PharmacyOpeningHoursErpModel(emptyMap())
            )
        ),
        openingHours = PharmacyOpeningHoursErpModel(emptyMap()),
        telematikId = "TELEMATIK_ID"
    )
}
