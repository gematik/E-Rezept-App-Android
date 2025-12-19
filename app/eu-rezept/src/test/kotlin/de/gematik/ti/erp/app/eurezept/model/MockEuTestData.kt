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

package de.gematik.ti.erp.app.eurezept.model

import android.location.Location
import androidx.compose.ui.graphics.ImageBitmap
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.eurezept.domain.model.Country
import de.gematik.ti.erp.app.eurezept.domain.model.CountryPhrases
import de.gematik.ti.erp.app.eurezept.domain.model.CountrySpecificLabels
import de.gematik.ti.erp.app.eurezept.domain.model.EuPrescription
import de.gematik.ti.erp.app.eurezept.domain.model.EuPrescriptionType
import de.gematik.ti.erp.app.eurezept.domain.model.EuRedemptionDetails
import de.gematik.ti.erp.app.fhir.FhirConsentErpModelCollection
import de.gematik.ti.erp.app.fhir.FhirCountryErpModel
import de.gematik.ti.erp.app.fhir.FhirCountryErpModelCollection
import de.gematik.ti.erp.app.fhir.consent.model.ConsentCategory
import de.gematik.ti.erp.app.fhir.consent.model.FhirCodeableConceptErp
import de.gematik.ti.erp.app.fhir.consent.model.FhirCodingErp
import de.gematik.ti.erp.app.fhir.consent.model.FhirConsentErpModel
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.localization.CountryCode
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.util.encoders.Base64
import java.util.Locale
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

object MockEuTestData {

    internal const val MOCK_PROFILE_ID = "test-profile-id"
    internal const val MOCK_TASK_ID_01 = "123-001"
    internal const val MESSAGE_TIMESTAMP = "2024-01-01T10:00:00Z"
    internal const val PRECRIPTION_ID_1 = "prescription-1"
    internal const val PRECRIPTION_ID_2 = "prescription-2"
    internal const val MOCK_MEDICATION_NAME_1 = "Ibuprofen 400mg"
    private const val MOCK_PRACTITIONER_NAME = "Dr. John Doe"

    internal val mockValidSsoToken = mockk<IdpData.SingleSignOnToken> {
        every { isValid(any()) } returns true
    }

    internal val mockInvalidSsoToken = mockk<IdpData.SingleSignOnToken> {
        every { isValid(any()) } returns false
    }

    internal val mockValidSsoTokenScope = mockk<IdpData.DefaultToken> {
        every { token } returns mockValidSsoToken
    }

    internal val mockInvalidSsoTokenScope = mockk<IdpData.DefaultToken> {
        every { token } returns mockInvalidSsoToken
    }

    internal val mockProfileWithValidToken = ProfilesUseCaseData.Profile(
        id = MOCK_PROFILE_ID,
        name = "Test Profile",
        insurance = ProfileInsuranceInformation(),
        isActive = true,
        color = ProfilesData.ProfileColorNames.SPRING_GRAY,
        avatar = ProfilesData.Avatar.PersonalizedImage,
        lastAuthenticated = Clock.System.now().minus(1.hours),
        ssoTokenScope = mockValidSsoTokenScope
    )

    internal val mockProfileWithInvalidToken = ProfilesUseCaseData.Profile(
        id = MOCK_PROFILE_ID,
        name = "Test Profile",
        insurance = ProfileInsuranceInformation(),
        isActive = true,
        color = ProfilesData.ProfileColorNames.SPRING_GRAY,
        avatar = ProfilesData.Avatar.PersonalizedImage,
        lastAuthenticated = Clock.System.now().minus(1.hours),
        ssoTokenScope = mockInvalidSsoTokenScope
    )

    internal val mockValidProfile = mockk<ProfilesUseCaseData.Profile> {
        every { id } returns MOCK_PROFILE_ID
        every { isSSOTokenValid(any()) } returns true
        every { ssoTokenScope } returns mockValidSsoTokenScope
        every { isRedemptionAllowed() } returns true
    }

    internal val mockActiveConsent = FhirConsentErpModelCollection(
        consent = listOf(
            FhirConsentErpModel(
                resourceType = "Consent",
                id = "consent-1",
                status = "active",
                category = listOf(
                    FhirCodeableConceptErp(
                        coding = listOf(
                            FhirCodingErp(
                                system = "https://gematik.de/fhir/eurezept/CodeSystem/consent-category",
                                code = ConsentCategory.EUCONSENT.code
                            )
                        )
                    )
                ),
                policyRule = null,
                dateTime = "2024-01-01T10:00:00Z",
                scope = null
            )
        )
    )

    internal val mockInactiveConsent = FhirConsentErpModelCollection(
        consent = listOf(
            FhirConsentErpModel(
                resourceType = "Consent",
                id = "consent-2",
                status = "inactive",
                category = listOf(
                    FhirCodeableConceptErp(
                        coding = listOf(
                            FhirCodingErp(
                                system = "https://gematik.de/fhir/eurezept/CodeSystem/consent-category",
                                code = ConsentCategory.EUCONSENT.code
                            )
                        )
                    )
                ),
                policyRule = null,
                dateTime = "2024-01-01T10:00:00Z",
                scope = null
            )
        )
    )

    internal val mockEuPrescriptions = listOf(
        EuPrescription(
            profileIdentifier = MOCK_PROFILE_ID,
            id = PRECRIPTION_ID_1,
            name = MOCK_MEDICATION_NAME_1,
            type = EuPrescriptionType.EuRedeemable,
            isMarkedAsEuRedeemableByPatientAuthorization = false,
            isMarkedAsError = false,
            isLoading = false,
            expiryDate = Clock.System.now()
        ),
        EuPrescription(
            profileIdentifier = MOCK_PROFILE_ID,
            id = PRECRIPTION_ID_2,
            name = "Aspirin 100mg",
            type = EuPrescriptionType.EuRedeemable,
            isMarkedAsEuRedeemableByPatientAuthorization = true,
            isMarkedAsError = false,
            isLoading = false,
            expiryDate = Clock.System.now()
        ),
        EuPrescription(
            profileIdentifier = MOCK_PROFILE_ID,
            id = "prescription-3",
            name = "Paracetamol 500mg",
            type = EuPrescriptionType.Scanned,
            isMarkedAsEuRedeemableByPatientAuthorization = false,
            isMarkedAsError = false,
            isLoading = false,
            expiryDate = null
        )
    )

    internal val mockSupportedCountries = listOf(
        CountryCode.DE,
        CountryCode.FR,
        CountryCode.IT,
        CountryCode.UK
    )

    internal val mockCountryPhrases = CountryPhrases(
        flagEmoji = "🇩🇪",
        redeemPrescriptionPhrase = "I would like to redeem a prescription",
        thankYouPhrase = "Thank you"
    )

    internal val mockFhirCountryModel = FhirCountryErpModelCollection(
        countries = listOf(
            FhirCountryErpModel(code = "DE", name = "Germany"),
            FhirCountryErpModel(code = "FR", name = "France"),
            FhirCountryErpModel(code = "IT", name = "Italy")
        )
    )

    internal val expectedEuCountries = listOf(
        Country("Germany", "DE", "🇩🇪"),
        Country("France", "FR", "🇫🇷"),
        Country("Italy", "IT", "🇮🇹")
    )

    internal val mockLocation = mockk<Location> {
        every { latitude } returns 10.1234
        every { longitude } returns 10.3210
    }

    internal val mockTtsLocale = Locale.GERMAN
    internal val mockCountrySpecificLabels = CountrySpecificLabels(
        codeLabel = "Einlösecode",
        insuranceNumberLabel = "Versichertennummer"
    )

    internal val mockEuAccessCode = EuAccessCode(
        countryCode = "DE",
        accessCode = "123456789",
        validUntil = Clock.System.now() + 7.days,
        createdAt = Clock.System.now(),
        profileIdentifier = "profile-123"
    )

    internal val mockEuRedemptionDetails = EuRedemptionDetails(
        euAccessCode = mockEuAccessCode,
        insuranceNumber = "A123456789",
        qrCodeBitmap = mockk<ImageBitmap>()
    )

    private val byteArray = Base64.decode(BuildKonfig.DEFAULT_VIRTUAL_HEALTH_CARD_CERTIFICATE)
    private val healthCertificate = X509CertificateHolder(byteArray)

    internal val profileData = ProfilesData.Profile(
        id = "1",
        name = "Max Mustermann",
        color = ProfilesData.ProfileColorNames.BLUE_MOON,
        lastAuthenticated = Clock.System.now(),
        avatar = ProfilesData.Avatar.ManWithPhone,
        insuranceName = "AOK",
        insuranceType = ProfilesData.InsuranceType.GKV,
        isConsentDrawerShown = true,
        active = true,
        singleSignOnTokenScope = IdpData.DefaultToken(
            token = mockValidSsoToken,
            cardAccessNumber = "123123",
            healthCardCertificate = healthCertificate
        )
    )

    private val MOCK_PATIENT = SyncedTaskData.Patient(
        name = "Jane",
        address = SyncedTaskData.Address(
            line1 = "",
            line2 = "",
            postalCode = "",
            city = ""
        ),
        birthdate = null,
        insuranceIdentifier = "ins123"
    )

    private val MOCK_ORGANIZATION = SyncedTaskData.Organization(
        name = "TestOrganization",
        address = SyncedTaskData.Address(
            line1 = "123 Main Street",
            line2 = "Apt 4",
            postalCode = "12345",
            city = "City"
        ),
        uniqueIdentifier = "org123",
        phone = "123-456-7890",
        mail = "info@testorg.com"
    )

    private val MOCK_PRACTITIONER = SyncedTaskData.Practitioner(
        name = MOCK_PRACTITIONER_NAME,
        qualification = "",
        practitionerIdentifier = " "
    )

    internal val MOCK_MEDICATION = SyncedTaskData.Medication(
        category = SyncedTaskData.MedicationCategory.ARZNEI_UND_VERBAND_MITTEL,
        medicationProfile = null,
        vaccine = false,
        text = MOCK_MEDICATION_NAME_1,
        form = "Tablet",
        lotNumber = null,
        expirationDate = null,
        identifier = SyncedTaskData.Identifier(),
        normSizeCode = null,
        amount = null,
        manufacturingInstructions = null,
        packaging = null,
        ingredientMedications = emptyList(),
        ingredients = emptyList()
    )

    internal val MOCK_MEDICATION_REQUEST = SyncedTaskData.MedicationRequest(
        medication = MOCK_MEDICATION,
        authoredOn = null,
        dateOfAccident = null,
        accidentType = SyncedTaskData.AccidentType.None,
        location = null,
        emergencyFee = null,
        substitutionAllowed = false,
        dosageInstruction = null,
        multiplePrescriptionInfo = SyncedTaskData.MultiplePrescriptionInfo(false),
        quantity = 1,
        note = null,
        bvg = null,
        additionalFee = SyncedTaskData.AdditionalFee.None
    )

    internal val MOCK_SYNCED_TASK_DATA_01 = SyncedTaskData.SyncedTask(
        profileId = MOCK_PROFILE_ID,
        taskId = MOCK_TASK_ID_01,
        accessCode = "testAccessCode",
        lastModified = Instant.parse(MESSAGE_TIMESTAMP),
        organization = MOCK_ORGANIZATION,
        practitioner = MOCK_PRACTITIONER,
        patient = MOCK_PATIENT,
        insuranceInformation = SyncedTaskData.InsuranceInformation(
            name = "TestInsurance",
            status = "Active",
            coverageType = SyncedTaskData.CoverageType.GKV
        ),
        expiresOn = Instant.parse(MESSAGE_TIMESTAMP),
        acceptUntil = Instant.parse(MESSAGE_TIMESTAMP),
        authoredOn = Instant.parse(MESSAGE_TIMESTAMP),
        status = SyncedTaskData.TaskStatus.Ready,
        isIncomplete = false,
        pvsIdentifier = "testPvsIdentifier",
        failureToReport = "testFailureToReport",
        medicationRequest = MOCK_MEDICATION_REQUEST,
        lastMedicationDispense = null,
        medicationDispenses = emptyList(),
        communications = emptyList(),
        isEuRedeemable = false,
        isEuRedeemableByPatientAuthorization = false
    )

    internal val MOCK_READY_EU_SYNCED_TASK = MOCK_SYNCED_TASK_DATA_01.copy(
        expiresOn = Clock.System.now() + 30.days,
        acceptUntil = Clock.System.now() + 30.days,
        status = SyncedTaskData.TaskStatus.Ready,
        isEuRedeemable = true
    )
}
