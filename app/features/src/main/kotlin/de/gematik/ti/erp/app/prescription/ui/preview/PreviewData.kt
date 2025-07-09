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
import de.gematik.ti.erp.app.fhir.model.DigaStatus
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.bouncycastle.util.encoders.Base64
import java.util.UUID
import kotlin.time.Duration.Companion.days

private val validSingleSignOnToken = IdpData.SingleSignOnToken(
    token = UUID.randomUUID().toString(),
    expiresOn = Clock.System.now().plus(200.days),
    validOn = Clock.System.now().minus(20.days)
)

private val invalidSingleSignOnToken = IdpData.SingleSignOnToken(
    token = UUID.randomUUID().toString(),
    expiresOn = Clock.System.now().plus(200.days),
    validOn = Clock.System.now().plus(20.days)
)

private const val CAN = "123123"
private val healthCardCertificate = Base64.decode(BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE)

val PREVIEW_ACTIVE_PROFILE = ProfilesUseCaseData.Profile(
    id = "1",
    name = "Max Mustermann",
    insurance = ProfileInsuranceInformation(
        insuranceType = ProfilesUseCaseData.InsuranceType.GKV
    ),
    isActive = true,
    color = ProfilesData.ProfileColorNames.SPRING_GRAY,
    lastAuthenticated = Instant.parse("2024-08-01T10:00:00Z"),
    ssoTokenScope = IdpData.DefaultToken(
        token = validSingleSignOnToken,
        cardAccessNumber = CAN,
        healthCardCertificate = healthCardCertificate
    ),
    avatar = ProfilesData.Avatar.ManWithPhone,
    image = null
)

val PREVIEW_INVALID_PROFILE = PREVIEW_ACTIVE_PROFILE.copy(
    ssoTokenScope = IdpData.DefaultToken(
        token = invalidSingleSignOnToken,
        cardAccessNumber = CAN,
        healthCardCertificate = healthCardCertificate
    )
)

val MOCK_MODEL_PROFILE = ProfilesUseCaseData.Profile(
    id = "id-1",
    name = "first profile",
    insurance = ProfileInsuranceInformation(
        insurantName = "insurantName",
        insuranceIdentifier = "insuranceIdentifier",
        insuranceName = "insuranceName",
        insuranceType = ProfilesUseCaseData.InsuranceType.GKV
    ),
    isActive = true,
    color = ProfilesData.ProfileColorNames.PINK,
    avatar = ProfilesData.Avatar.Baby,
    image = byteArrayOf(0x00, 0x01, 0x02),
    lastAuthenticated = Instant.parse("2024-08-01T10:00:00Z"),
    ssoTokenScope = null
)

val MOCK_MODEL_PROFILE_LOGGED_IN = ProfilesUseCaseData.Profile(
    id = "id-1",
    name = "logged-in",
    insurance = ProfileInsuranceInformation(
        insurantName = "insurantName",
        insuranceIdentifier = "insuranceIdentifier",
        insuranceName = "insuranceName",
        insuranceType = ProfilesUseCaseData.InsuranceType.GKV
    ),
    isActive = true,
    color = ProfilesData.ProfileColorNames.SUN_DEW,
    avatar = ProfilesData.Avatar.FemaleDoctor,
    image = byteArrayOf(0x00, 0x01, 0x02),
    lastAuthenticated = Instant.parse("2024-08-01T10:00:00Z"),
    ssoTokenScope = IdpData.ExternalAuthenticationToken(
        token = IdpData.SingleSignOnToken(
            token = "token",
            expiresOn = Instant.parse("3024-08-01T10:00:00Z"),
            validOn = Instant.parse("2023-08-01T10:00:00Z")
        ),
        authenticatorName = "authenticatorName",
        authenticatorId = "authenticatorId"
    )
)

val MOCK_MODEL_PROFILE_LOGGED_INVALID = ProfilesUseCaseData.Profile(
    id = "id-invalid",
    name = "token-null",
    insurance = ProfileInsuranceInformation(
        insurantName = "insurantName",
        insuranceIdentifier = "insuranceIdentifier",
        insuranceName = "insuranceName",
        insuranceType = ProfilesUseCaseData.InsuranceType.GKV
    ),
    isActive = true,
    color = ProfilesData.ProfileColorNames.SUN_DEW,
    avatar = ProfilesData.Avatar.WomanWithPhone,
    image = byteArrayOf(0x00, 0x01, 0x02),
    lastAuthenticated = Instant.parse("2024-08-01T10:00:00Z"),
    ssoTokenScope = IdpData.ExternalAuthenticationToken(
        token = null,
        authenticatorName = "authenticatorName",
        authenticatorId = "authenticatorId"
    )
)

val MOCK_MODEL_PROFILE_2 = ProfilesUseCaseData.Profile(
    id = "id-2",
    name = "second profile",
    insurance = ProfileInsuranceInformation(
        insurantName = "insurantName",
        insuranceIdentifier = "insuranceIdentifier",
        insuranceName = "insuranceName",
        insuranceType = ProfilesUseCaseData.InsuranceType.GKV
    ),
    isActive = true,
    color = ProfilesData.ProfileColorNames.PINK,
    avatar = ProfilesData.Avatar.FemaleDoctor,
    image = byteArrayOf(0x00, 0x01, 0x02),
    lastAuthenticated = Instant.parse("2024-08-01T10:00:00Z"),
    ssoTokenScope = null
)

val MOCK_PRESCRIPTION_SELF_PAYER = Prescription.SyncedPrescription(
    taskId = "Amlodipine",
    name = "Amlodipine",
    redeemedOn = null,
    expiresOn = Instant.parse("3024-08-01T10:00:00Z"),
    state = SyncedTaskData.SyncedTask.Ready(
        expiresOn = Instant.parse("2023-08-01T10:00:00Z"),
        acceptUntil = Instant.parse("2024-08-01T10:00:00Z")
    ),
    isIncomplete = false,
    organization = "MOCK_PRACTITIONER_NAME",
    authoredOn = Instant.parse("2024-08-01T10:00:00Z"),
    acceptUntil = Instant.parse("2024-08-01T10:00:00Z"), // decides self-payment
    isDirectAssignment = false,
    deviceRequestState = DigaStatus.Ready,
    lastModified = Instant.fromEpochSeconds(123456),
    prescriptionChipInformation = Prescription.PrescriptionChipInformation(
        isPartOfMultiplePrescription = false,
        numerator = null,
        denominator = null,
        start = Instant.parse("2024-08-01T10:00:00Z")
    )
)

val MOCK_PRESCRIPTION_DIRECT_ASSIGNMENT = MOCK_PRESCRIPTION_SELF_PAYER.copy(
    taskId = "Atorvastatin",
    name = "Atorvastatin",
    isDirectAssignment = true
)

val MOCK_PRESCRIPTION_EXPIRED = Prescription.SyncedPrescription(
    taskId = "Cetirizine",
    name = "Cetirizine",
    redeemedOn = null,
    expiresOn = Instant.parse("3024-08-01T10:00:00Z"),
    state = SyncedTaskData.SyncedTask.Expired(expiredOn = Instant.parse("2024-08-01T10:00:00Z")),
    isIncomplete = false,
    organization = "MOCK_PRACTITIONER_NAME",
    authoredOn = Instant.parse("2024-08-01T10:00:00Z"),
    acceptUntil = Instant.parse("3024-08-01T10:00:00Z"),
    isDirectAssignment = false,
    deviceRequestState = DigaStatus.Ready,
    lastModified = Instant.fromEpochSeconds(123456),
    prescriptionChipInformation = Prescription.PrescriptionChipInformation(
        isPartOfMultiplePrescription = false,
        numerator = null,
        denominator = null,
        start = Instant.parse("2024-08-01T10:00:00Z")
    )
)

val MOCK_PRESCRIPTION_DELETED = Prescription.SyncedPrescription(
    taskId = "Glipizide",
    name = "Glipizide",
    redeemedOn = null,
    expiresOn = Instant.parse("3024-08-01T10:00:00Z"),
    state = SyncedTaskData.SyncedTask.Deleted(lastModified = Instant.parse("2024-08-01T10:00:00Z")),
    isIncomplete = false,
    organization = "MOCK_PRACTITIONER_NAME",
    authoredOn = Instant.parse("2024-08-01T10:00:00Z"),
    acceptUntil = Instant.parse("3024-08-01T10:00:00Z"),
    isDirectAssignment = false,
    deviceRequestState = DigaStatus.Ready,
    lastModified = Instant.fromEpochSeconds(123456),
    prescriptionChipInformation = Prescription.PrescriptionChipInformation(
        isPartOfMultiplePrescription = false,
        numerator = null,
        denominator = null,
        start = Instant.parse("2024-08-01T10:00:00Z")
    )
)

val MOCK_PRESCRIPTION_PENDING = Prescription.SyncedPrescription(
    taskId = "Metformin",
    name = "Metformin",
    redeemedOn = null,
    expiresOn = Instant.parse("3024-08-01T10:00:00Z"),
    state = SyncedTaskData.SyncedTask.Pending(
        sentOn = Instant.parse("2024-08-01T10:00:00Z"),
        toTelematikId = "toTelematikId"
    ),
    isIncomplete = false,
    organization = "MOCK_PRACTITIONER_NAME",
    authoredOn = Instant.parse("2024-08-01T10:00:00Z"),
    acceptUntil = Instant.parse("3024-08-01T10:00:00Z"),
    isDirectAssignment = false,
    deviceRequestState = DigaStatus.Ready,
    lastModified = Instant.fromEpochSeconds(123456),
    prescriptionChipInformation = Prescription.PrescriptionChipInformation(
        isPartOfMultiplePrescription = false,
        numerator = null,
        denominator = null,
        start = Instant.parse("2024-08-01T10:00:00Z")
    )
)

val MOCK_PRESCRIPTION_IN_PROGRESS = Prescription.SyncedPrescription(
    taskId = "Montelukast",
    name = "Montelukast",
    redeemedOn = null,
    expiresOn = Instant.parse("3024-08-01T10:00:00Z"),
    state = SyncedTaskData.SyncedTask.InProgress(
        lastModified = Instant.parse("2024-08-01T10:00:00Z")
    ),
    isIncomplete = false,
    organization = "MOCK_PRACTITIONER_NAME",
    authoredOn = Instant.parse("2024-08-01T10:00:00Z"),
    acceptUntil = Instant.parse("3024-08-01T10:00:00Z"),
    isDirectAssignment = false,
    deviceRequestState = DigaStatus.Ready,
    lastModified = Instant.fromEpochSeconds(123456),
    prescriptionChipInformation = Prescription.PrescriptionChipInformation(
        isPartOfMultiplePrescription = false,
        numerator = null,
        denominator = null,
        start = Instant.parse("2024-08-01T10:00:00Z")
    )
)

val MOCK_PRESCRIPTION_LATER_REDEEMABLE = Prescription.SyncedPrescription(
    taskId = "Zolpidem",
    name = "Zolpidem",
    redeemedOn = null,
    expiresOn = Instant.parse("3024-08-01T10:00:00Z"),
    state = SyncedTaskData.SyncedTask.LaterRedeemable(
        redeemableOn = Instant.parse("2024-08-01T10:00:00Z")
    ),
    isIncomplete = false,
    organization = "MOCK_PRACTITIONER_NAME",
    authoredOn = Instant.parse("2024-08-01T10:00:00Z"),
    acceptUntil = Instant.parse("3024-08-01T10:00:00Z"),
    isDirectAssignment = false,
    deviceRequestState = DigaStatus.Ready,
    lastModified = Instant.fromEpochSeconds(123456),
    prescriptionChipInformation = Prescription.PrescriptionChipInformation(
        isPartOfMultiplePrescription = false,
        numerator = null,
        denominator = null,
        start = Instant.parse("2024-08-01T10:00:00Z")
    )
)

val MOCK_PRESCRIPTION_OTHER = Prescription.SyncedPrescription(
    taskId = "Fluoxetine",
    name = "Fluoxetine",
    redeemedOn = null,
    expiresOn = Instant.parse("3024-08-01T10:00:00Z"),
    state = SyncedTaskData.SyncedTask.Other(
        state = SyncedTaskData.TaskStatus.Failed,
        lastModified = Instant.parse("2024-08-01T10:00:00Z")
    ),
    isIncomplete = false,
    organization = "MOCK_PRACTITIONER_NAME",
    authoredOn = Instant.parse("2024-08-01T10:00:00Z"),
    acceptUntil = Instant.parse("3024-08-01T10:00:00Z"),
    isDirectAssignment = false,
    deviceRequestState = DigaStatus.Ready,
    lastModified = Instant.fromEpochSeconds(123456),
    prescriptionChipInformation = Prescription.PrescriptionChipInformation(
        isPartOfMultiplePrescription = false,
        numerator = null,
        denominator = null,
        start = Instant.parse("2024-08-01T10:00:00Z")
    )
)

val MOCK_PRESCRIPTION_READY = Prescription.SyncedPrescription(
    taskId = "Fluoxetine",
    name = "Fluoxetine",
    redeemedOn = null,
    expiresOn = Instant.parse("3024-08-01T10:00:00Z"),
    state = SyncedTaskData.SyncedTask.Ready(
        expiresOn = Instant.parse("3024-08-01T10:00:00Z"),
        acceptUntil = Instant.parse("3024-08-01T10:00:00Z")
    ),
    isIncomplete = false,
    organization = "MOCK_PRACTITIONER_NAME",
    authoredOn = Instant.parse("2024-08-01T10:00:00Z"),
    acceptUntil = Instant.parse("3024-08-01T10:00:00Z"),
    isDirectAssignment = false,
    deviceRequestState = DigaStatus.Ready,
    lastModified = Instant.fromEpochSeconds(123456),
    prescriptionChipInformation = Prescription.PrescriptionChipInformation(
        isPartOfMultiplePrescription = false,
        numerator = null,
        denominator = null,
        start = Instant.parse("2024-08-01T10:00:00Z")
    )
)
