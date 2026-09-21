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

package de.gematik.ti.erp.app.eurezept.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.eurezept.domain.model.EuPrescription
import de.gematik.ti.erp.app.eurezept.domain.model.EuPrescriptionType
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import kotlinx.datetime.Instant

data class EuPrescriptionSelectionPreviewData(
    val prescriptions: List<EuPrescription>,
    val selectedPrescriptionIds: Set<String>,
    val profileData: ProfileErpModel? = null
)

class EuPrescriptionSelectionPreviewParameterProvider : PreviewParameterProvider<EuPrescriptionSelectionPreviewData> {

    private val fullErrorCasePrescriptions = listOf(
        EuPrescription(
            profileIdentifier = "profile1",
            id = "1",
            name = "🇪🇺 EU Medikament Prescription",
            type = EuPrescriptionType.EuRedeemable,
            expiryDate = Instant.parse("2024-07-01T10:00:00Z"),
            isMarkedAsEuRedeemableByPatientAuthorization = true
        ),
        EuPrescription(
            profileIdentifier = "profile1",
            id = "2",
            name = "Acaimoum",
            type = EuPrescriptionType.Unknown,
            isMarkedAsEuRedeemableByPatientAuthorization = false
        ),
        EuPrescription(
            profileIdentifier = "profile1",
            id = "3",
            name = "MeinMedikament",
            type = EuPrescriptionType.FreeText,
            isMarkedAsEuRedeemableByPatientAuthorization = false
        ),
        EuPrescription(
            profileIdentifier = "profile1",
            id = "4",
            name = "Wirkstoff Ibu",
            type = EuPrescriptionType.Ingredient,
            isMarkedAsEuRedeemableByPatientAuthorization = false
        ),
        EuPrescription(
            profileIdentifier = "profile1",
            id = "5",
            name = "Gescanntes Medikament 3",
            type = EuPrescriptionType.Scanned,
            isMarkedAsEuRedeemableByPatientAuthorization = false
        ),
        EuPrescription(
            profileIdentifier = "profile1",
            id = "6",
            name = "Benzos",
            type = EuPrescriptionType.BTM,
            isMarkedAsEuRedeemableByPatientAuthorization = false
        )
    )

    val profile = ProfileErpModel(
        id = "123456",
        name = "Preview User",
        profileImageData = ProfileImageDataErpModel(
            avatar = Avatar.BoyWithHealthCard,
            color = ProfileColorNames.SUN_DEW,
            image = null
        ),
        insuranceData = ProfileInsuranceDataErpModel(
            insuranceIdentifier = "123456789",
            insuranceType = InsuranceType.NONE,
            insuranceName = "Example Insurance",
            insurantName = "Example Insurant",
            organizationIdentifier = null
        ),
        active = true,
        lastAuthenticated = null,
        userAuthentication = UserAuthenticationErpModel.NotInitialized,
        isNewlyCreated = false,
        isConsentDrawerShown = true,
        lastAuditEventSynced = null,
        lastTaskSynced = null
    )

    override val values: Sequence<EuPrescriptionSelectionPreviewData>
        get() = sequenceOf(
            // Full error cases - no selection
            EuPrescriptionSelectionPreviewData(
                prescriptions = fullErrorCasePrescriptions,
                selectedPrescriptionIds = emptySet(),
                profileData = profile
            ),

            // Full error cases - only available selected
            EuPrescriptionSelectionPreviewData(
                prescriptions = fullErrorCasePrescriptions,
                selectedPrescriptionIds = setOf("1"),
                profileData = profile
            )
        )
}
