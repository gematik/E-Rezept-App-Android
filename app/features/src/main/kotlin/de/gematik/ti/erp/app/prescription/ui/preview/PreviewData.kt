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

package de.gematik.ti.erp.app.prescription.ui.preview

import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.task.model.MedicationRequestErpModel
import de.gematik.ti.erp.app.task.model.MultiplePrescriptionInfo
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import de.gematik.ti.erp.app.userauthentication.model.SingleSignOnTokenErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.bouncycastle.util.encoders.Base64
import java.util.UUID
import kotlin.time.Duration.Companion.days

private val validSingleSignOnToken = SingleSignOnTokenErpModel(
    token = UUID.randomUUID().toString(),
    expiresOn = Clock.System.now().plus(200.days),
    validOn = Clock.System.now().minus(20.days)
)

private val invalidSingleSignOnToken = SingleSignOnTokenErpModel(
    token = UUID.randomUUID().toString(),
    expiresOn = Clock.System.now().plus(200.days),
    validOn = Clock.System.now().plus(20.days)
)

private const val CAN = "123123"
private val healthCardCertificate = Base64.decode(BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE)

val PREVIEW_ACTIVE_PROFILE = ProfileErpModel(
    id = "1",
    name = "Max Mustermann",
    profileImageData = ProfileImageDataErpModel(
        avatar = Avatar.ManWithPhone,
        image = null,
        color = ProfileColorNames.SPRING_GRAY
    ),
    insuranceData = ProfileInsuranceDataErpModel(
        insurantName = "Max Mustermann",
        insuranceIdentifier = "123456789",
        insuranceName = "AOK",
        insuranceType = InsuranceType.GKV,
        organizationIdentifier = "DE123456789"
    ),
    active = true,
    lastAuthenticated = Instant.parse("2024-08-01T10:00:00Z"),
    userAuthentication = UserAuthenticationErpModel.HealthCard(
        singleSignOnTokenErpModel = validSingleSignOnToken,
        cardAccessNumber = CAN,
        healthCardCertificate = healthCardCertificate
    ),
    lastTaskSynced = Instant.parse("2024-08-01T10:00:00Z"),
    lastAuditEventSynced = Instant.parse("2024-08-01T10:00:00Z"),
    isNewlyCreated = false,
    isConsentDrawerShown = true
)

val PREVIEW_INVALID_PROFILE = PREVIEW_ACTIVE_PROFILE.copy(
    userAuthentication = UserAuthenticationErpModel.HealthCard(
        singleSignOnTokenErpModel = invalidSingleSignOnToken,
        cardAccessNumber = CAN,
        healthCardCertificate = healthCardCertificate
    )
)

val MOCK_MODEL_PROFILE = ProfileErpModel(
    id = "id-1",
    name = "first profile",
    insuranceData = ProfileInsuranceDataErpModel(
        insurantName = "insurantName",
        insuranceIdentifier = "insuranceIdentifier",
        insuranceName = "insuranceName",
        insuranceType = InsuranceType.GKV,
        organizationIdentifier = "organizationIdentifier"
    ),
    active = true,
    profileImageData = ProfileImageDataErpModel(
        color = ProfileColorNames.PINK,
        avatar = Avatar.Baby,
        image = byteArrayOf(0x00, 0x01, 0x02)
    ),
    userAuthentication = UserAuthenticationErpModel.NotInitialized,
    lastAuthenticated = null,
    lastTaskSynced = null,
    lastAuditEventSynced = null,
    isNewlyCreated = true,
    isConsentDrawerShown = true
)

val MOCK_MODEL_PROFILE_LOGGED_IN = ProfileErpModel(
    id = "id-1",
    name = "logged-in",
    insuranceData = ProfileInsuranceDataErpModel(
        insurantName = "insurantName",
        insuranceIdentifier = "insuranceIdentifier",
        insuranceName = "insuranceName",
        insuranceType = InsuranceType.GKV,
        organizationIdentifier = "organizationIdentifier"
    ),
    active = true,
    profileImageData = ProfileImageDataErpModel(
        color = ProfileColorNames.SUN_DEW,
        avatar = Avatar.FemaleDoctor,
        image = byteArrayOf(0x00, 0x01, 0x02)
    ),
    lastAuthenticated = Instant.parse("2024-08-01T10:00:00Z"),
    userAuthentication = UserAuthenticationErpModel.External(
        singleSignOnTokenErpModel = SingleSignOnTokenErpModel(
            token = "token",
            expiresOn = Instant.parse("3024-08-01T10:00:00Z"),
            validOn = Instant.parse("2023-08-01T10:00:00Z")
        ),
        externalAuthenticatorName = "authenticatorName",
        externalAuthenticatorId = "authenticatorId"
    ),
    lastTaskSynced = null,
    lastAuditEventSynced = null,
    isNewlyCreated = true,
    isConsentDrawerShown = true
)

val MOCK_MODEL_PROFILE_LOGGED_INVALID = ProfileErpModel(
    id = "id-invalid",
    name = "token-null",
    insuranceData = ProfileInsuranceDataErpModel(
        insurantName = "insurantName",
        insuranceIdentifier = "insuranceIdentifier",
        insuranceName = "insuranceName",
        insuranceType = InsuranceType.GKV,
        organizationIdentifier = "organizationIdentifier"
    ),
    active = true,
    profileImageData = ProfileImageDataErpModel(
        color = ProfileColorNames.SUN_DEW,
        avatar = Avatar.WomanWithPhone,
        image = byteArrayOf(0x00, 0x01, 0x02)
    ),
    lastAuthenticated = Instant.parse("2024-08-01T10:00:00Z"),
    userAuthentication = UserAuthenticationErpModel.External(
        singleSignOnTokenErpModel = null,
        externalAuthenticatorName = "authenticatorName",
        externalAuthenticatorId = "authenticatorId"
    ),
    lastTaskSynced = null,
    lastAuditEventSynced = null,
    isNewlyCreated = true,
    isConsentDrawerShown = true
)

val MOCK_MODEL_PROFILE_2 = ProfileErpModel(
    id = "id-2",
    name = "second profile",
    insuranceData = ProfileInsuranceDataErpModel(
        insurantName = "insurantName",
        insuranceIdentifier = "insuranceIdentifier",
        insuranceName = "insuranceName",
        insuranceType = InsuranceType.GKV,
        organizationIdentifier = "organizationIdentifier"
    ),
    active = true,
    profileImageData = ProfileImageDataErpModel(
        color = ProfileColorNames.SUN_DEW,
        avatar = Avatar.FemaleDoctor,
        image = byteArrayOf(0x00, 0x01, 0x02)
    ),
    lastAuthenticated = Instant.parse("2024-08-01T10:00:00Z"),
    userAuthentication = UserAuthenticationErpModel.NotInitialized,
    lastTaskSynced = null,
    lastAuditEventSynced = null,
    isNewlyCreated = true,
    isConsentDrawerShown = true
)

private fun buildPrescription(
    taskId: String,
    name: String,
    status: TaskStatusEnum,
    expiresOn: Instant = Instant.parse("3024-08-01T10:00:00Z"),
    acceptUntil: Instant = Instant.parse("3024-08-01T10:00:00Z"),
    authoredOn: Instant = Instant.parse("2024-08-01T10:00:00Z"),
    isIncomplete: Boolean = false,
    medicationRequest: MedicationRequestErpModel? = null
) = TaskErpModel.Synced.Prescription(
    profileId = "preview-profile",
    taskId = taskId,
    name = name,
    accessCode = "",
    isEuRedeemable = false,
    isEuRedeemableByPatientAuthorization = false,
    lastModified = Instant.fromEpochSeconds(123456),
    organization = null,
    practitioner = null,
    patient = null,
    insuranceInformation = null,
    expiresOn = expiresOn,
    acceptUntil = acceptUntil,
    authoredOn = authoredOn,
    status = status,
    isIncomplete = isIncomplete,
    pvsIdentifier = "",
    failureToReport = "",
    medicationRequest = medicationRequest,
    medicationDispenses = emptyList()
)

val MOCK_PRESCRIPTION_SELF_PAYER = buildPrescription(
    taskId = "Amlodipine",
    name = "Amlodipine",
    status = TaskStatusEnum.Ready,
    expiresOn = Instant.parse("3024-08-01T10:00:00Z"),
    acceptUntil = Instant.parse("3024-08-01T10:00:00Z"),
    authoredOn = Instant.parse("2024-08-01T10:00:00Z")
)

val MOCK_PRESCRIPTION_DIRECT_ASSIGNMENT = buildPrescription(
    taskId = "169.Atorvastatin",
    name = "Atorvastatin",
    status = TaskStatusEnum.Ready,
    expiresOn = Instant.parse("3024-08-01T10:00:00Z"),
    acceptUntil = Instant.parse("3024-08-01T10:00:00Z"),
    authoredOn = Instant.parse("2024-08-01T10:00:00Z")
)

val MOCK_PRESCRIPTION_EXPIRED = buildPrescription(
    taskId = "Cetirizine",
    name = "Cetirizine",
    status = TaskStatusEnum.Ready,
    expiresOn = Instant.parse("2024-08-01T10:00:00Z"),
    acceptUntil = Instant.parse("3024-08-01T10:00:00Z"),
    authoredOn = Instant.parse("2024-08-01T10:00:00Z")
)

val MOCK_PRESCRIPTION_DELETED = buildPrescription(
    taskId = "Glipizide",
    name = "Glipizide",
    status = TaskStatusEnum.Canceled,
    expiresOn = Instant.parse("3024-08-01T10:00:00Z"),
    acceptUntil = Instant.parse("3024-08-01T10:00:00Z"),
    authoredOn = Instant.parse("2024-08-01T10:00:00Z")
)

val MOCK_PRESCRIPTION_PENDING = buildPrescription(
    taskId = "Metformin",
    name = "Metformin",
    status = TaskStatusEnum.Ready,
    expiresOn = Instant.parse("3024-08-01T10:00:00Z"),
    acceptUntil = Instant.parse("3024-08-01T10:00:00Z"),
    authoredOn = Instant.parse("2024-08-01T10:00:00Z")
)

val MOCK_PRESCRIPTION_IN_PROGRESS = buildPrescription(
    taskId = "Montelukast",
    name = "Montelukast",
    status = TaskStatusEnum.InProgress,
    expiresOn = Instant.parse("3024-08-01T10:00:00Z"),
    acceptUntil = Instant.parse("3024-08-01T10:00:00Z"),
    authoredOn = Instant.parse("2024-08-01T10:00:00Z")
)

val MOCK_PRESCRIPTION_LATER_REDEEMABLE = buildPrescription(
    taskId = "Zolpidem",
    name = "Zolpidem",
    status = TaskStatusEnum.Ready,
    expiresOn = Instant.parse("3024-08-01T10:00:00Z"),
    acceptUntil = Instant.parse("3024-08-01T10:00:00Z"),
    authoredOn = Instant.parse("2024-08-01T10:00:00Z"),
    medicationRequest = MedicationRequestErpModel(
        substitutionAllowed = false,
        multiplePrescriptionInfo = MultiplePrescriptionInfo(
            indicator = true,
            start = Instant.parse("3024-08-01T10:00:00Z")
        ),
        note = null
    )
)

val MOCK_PRESCRIPTION_OTHER = buildPrescription(
    taskId = "Fluoxetine",
    name = "Fluoxetine",
    status = TaskStatusEnum.Failed,
    expiresOn = Instant.parse("3024-08-01T10:00:00Z"),
    acceptUntil = Instant.parse("3024-08-01T10:00:00Z"),
    authoredOn = Instant.parse("2024-08-01T10:00:00Z")
)

val MOCK_PRESCRIPTION_READY = buildPrescription(
    taskId = "FluoxetineReady",
    name = "Fluoxetine",
    status = TaskStatusEnum.Ready,
    expiresOn = Instant.parse("3024-08-01T10:00:00Z"),
    acceptUntil = Instant.parse("3024-08-01T10:00:00Z"),
    authoredOn = Instant.parse("2024-08-01T10:00:00Z")
)
