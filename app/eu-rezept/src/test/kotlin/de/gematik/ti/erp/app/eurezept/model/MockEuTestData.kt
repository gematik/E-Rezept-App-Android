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
import de.gematik.ti.erp.app.localization.CountryCode
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.task.model.AccidentType
import de.gematik.ti.erp.app.task.model.AdditionalFeeErpModel
import de.gematik.ti.erp.app.task.model.AddressErpModel
import de.gematik.ti.erp.app.task.model.Identifier
import de.gematik.ti.erp.app.task.model.InsuranceErpModel
import de.gematik.ti.erp.app.task.model.InsuranceErpModelCoverageType
import de.gematik.ti.erp.app.task.model.MedicationCategory
import de.gematik.ti.erp.app.task.model.MedicationErpModel
import de.gematik.ti.erp.app.task.model.MedicationRequestErpModel
import de.gematik.ti.erp.app.task.model.MultiplePrescriptionInfo
import de.gematik.ti.erp.app.task.model.OrganizationErpModel
import de.gematik.ti.erp.app.task.model.PatientErpModel
import de.gematik.ti.erp.app.task.model.PractitionerErpModel
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import de.gematik.ti.erp.app.userauthentication.model.SingleSignOnTokenErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
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

    internal val mockValidSsoToken = mockk<SingleSignOnTokenErpModel> {
        every { isValid(any()) } returns true
        every { token } returns "mock-token"
    }

    internal val mockInvalidSsoToken = mockk<SingleSignOnTokenErpModel> {
        every { isValid(any()) } returns false
        every { token } returns "mock-token"
        every { validOn } returns Instant.DISTANT_PAST
    }

    internal val mockValidUserAuthentication = mockk<UserAuthenticationErpModel.HealthCard> {
        every { singleSignOnTokenErpModel } returns mockValidSsoToken
        every { cardAccessNumber } returns "123123"
    }

    internal val mockInvalidUserAuthentication = mockk<UserAuthenticationErpModel.HealthCard> {
        every { singleSignOnTokenErpModel } returns mockInvalidSsoToken
        every { cardAccessNumber } returns "123123"
    }

    internal val mockProfileWithValidToken = ProfileErpModel(
        id = MOCK_PROFILE_ID,
        name = "Test Profile",
        active = true,
        isNewlyCreated = false,
        profileImageData = ProfileImageDataErpModel(
            color = ProfileColorNames.SPRING_GRAY,
            avatar = Avatar.PersonalizedImage,
            image = null
        ),
        insuranceData = ProfileInsuranceDataErpModel(
            insurantName = null,
            insuranceIdentifier = null,
            insuranceName = null,
            insuranceType = InsuranceType.NONE,
            organizationIdentifier = null
        ),
        isConsentDrawerShown = false,
        lastAuthenticated = Clock.System.now().minus(1.hours),
        lastAuditEventSynced = null,
        lastTaskSynced = null,
        userAuthentication = mockValidUserAuthentication
    )

    internal val mockProfileWithInvalidToken = ProfileErpModel(
        id = MOCK_PROFILE_ID,
        name = "Test Profile",
        active = true,
        isNewlyCreated = false,
        profileImageData = ProfileImageDataErpModel(
            color = ProfileColorNames.SPRING_GRAY,
            avatar = Avatar.PersonalizedImage,
            image = null
        ),
        insuranceData = ProfileInsuranceDataErpModel(
            insurantName = null,
            insuranceIdentifier = null,
            insuranceName = null,
            insuranceType = InsuranceType.NONE,
            organizationIdentifier = null
        ),
        isConsentDrawerShown = false,
        lastAuthenticated = Clock.System.now().minus(1.hours),
        lastAuditEventSynced = null,
        lastTaskSynced = null,
        userAuthentication = mockInvalidUserAuthentication
    )

    internal val mockValidProfileMock = mockk<ProfileErpModel> {
        every { id } returns MOCK_PROFILE_ID
        every { isSSOTokenValid(any()) } returns true
        every { userAuthentication } returns mockValidUserAuthentication
        every { isRedemptionAllowed() } returns true
        every { active } returns true
        every { lastAuthenticated } returns null
        every { profileImageData } returns ProfileImageDataErpModel(
            color = ProfileColorNames.SPRING_GRAY,
            avatar = Avatar.PersonalizedImage,
            image = null
        )
        every { insuranceData } returns ProfileInsuranceDataErpModel(
            insurantName = null,
            insuranceIdentifier = "X123456789",
            insuranceName = null,
            insuranceType = InsuranceType.NONE,
            organizationIdentifier = null
        )
        every { name } returns "Test Profile"
    }

    internal val mockInvalidProfileMock = mockk<ProfileErpModel> {
        every { id } returns MOCK_PROFILE_ID
        every { isSSOTokenValid(any()) } returns false
        every { userAuthentication } returns mockInvalidUserAuthentication
        every { isRedemptionAllowed() } returns false
        every { active } returns true
        every { lastAuthenticated } returns null
        every { profileImageData } returns ProfileImageDataErpModel(
            color = ProfileColorNames.SPRING_GRAY,
            avatar = Avatar.PersonalizedImage,
            image = null
        )
        every { insuranceData } returns ProfileInsuranceDataErpModel(
            insurantName = null,
            insuranceIdentifier = null,
            insuranceName = null,
            insuranceType = InsuranceType.NONE,
            organizationIdentifier = null
        )
        every { name } returns "Test Profile"
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

    internal val mockEuAccessCode = EuAccessCodeErpModel(
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

    internal val profileData = ProfileErpModel(
        id = "1",
        name = "Max Mustermann",
        active = true,
        isNewlyCreated = false,
        profileImageData = ProfileImageDataErpModel(
            color = ProfileColorNames.BLUE_MOON,
            avatar = Avatar.ManWithPhone,
            image = null
        ),
        insuranceData = ProfileInsuranceDataErpModel(
            insurantName = "Max Mustermann",
            insuranceIdentifier = null,
            insuranceName = "AOK",
            insuranceType = InsuranceType.GKV,
            organizationIdentifier = null
        ),
        isConsentDrawerShown = true,
        lastAuthenticated = Clock.System.now(),
        lastAuditEventSynced = null,
        lastTaskSynced = null,
        userAuthentication = UserAuthenticationErpModel.HealthCard(
            singleSignOnTokenErpModel = mockValidSsoToken,
            cardAccessNumber = "123123",
            healthCardCertificate = healthCertificate.encoded
        )
    )

    private val MOCK_PATIENT = PatientErpModel(
        name = "Jane",
        address = AddressErpModel(
            line1 = "",
            line2 = "",
            postalCode = "",
            city = ""
        ),
        dateOfBirth = null,
        insuranceIdentifier = "ins123"
    )

    private val MOCK_ORGANIZATION = OrganizationErpModel(
        name = "TestOrganization",
        address = AddressErpModel(
            line1 = "123 Main Street",
            line2 = "Apt 4",
            postalCode = "12345",
            city = "City"
        ),
        uniqueIdentifier = "org123",
        phone = "123-456-7890",
        mail = "info@testorg.com"
    )

    private val MOCK_PRACTITIONER = PractitionerErpModel(
        name = MOCK_PRACTITIONER_NAME,
        qualification = "",
        practitionerIdentifier = " ",
        dentistIdentifier = null,
        telematikId = null
    )

    internal val MOCK_MEDICATION = MedicationErpModel(
        category = MedicationCategory.ARZNEI_UND_VERBAND_MITTEL,
        medicationProfile = null,
        isVaccine = false,
        text = MOCK_MEDICATION_NAME_1,
        form = "Tablet",
        lotNumber = null,
        expirationDate = null,
        identifier = Identifier(),
        normSizeCode = null,
        amount = null,
        manufacturingInstructions = null,
        packaging = null,
        ingredientMedications = emptyList(),
        ingredients = emptyList()
    )

    internal val MOCK_MEDICATION_REQUEST = MedicationRequestErpModel(
        medication = MOCK_MEDICATION,
        authoredOn = null,
        dateOfAccident = null,
        accidentType = AccidentType.None,
        location = null,
        emergencyFee = null,
        substitutionAllowed = false,
        dosageInstruction = null,
        multiplePrescriptionInfo = MultiplePrescriptionInfo(false),
        quantity = 1,
        note = null,
        bvg = null,
        additionalFee = AdditionalFeeErpModel.None
    )

    internal val MOCK_SYNCED_TASK_DATA_01 = TaskErpModel.Synced.Prescription(
        profileId = MOCK_PROFILE_ID,
        name = MOCK_MEDICATION_NAME_1,
        taskId = MOCK_TASK_ID_01,
        accessCode = "testAccessCode",
        isEuRedeemable = false,
        lastModified = Instant.parse(MESSAGE_TIMESTAMP),
        isEuRedeemableByPatientAuthorization = false,
        organization = MOCK_ORGANIZATION,
        practitioner = MOCK_PRACTITIONER,
        patient = MOCK_PATIENT,
        insuranceInformation = InsuranceErpModel(
            name = "TestInsurance",
            status = "Active",
            coverageType = InsuranceErpModelCoverageType.GKV
        ),
        expiresOn = Instant.parse(MESSAGE_TIMESTAMP),
        acceptUntil = Instant.parse(MESSAGE_TIMESTAMP),
        authoredOn = Instant.parse(MESSAGE_TIMESTAMP),
        status = TaskStatusEnum.Ready,
        isIncomplete = false,
        pvsIdentifier = "testPvsIdentifier",
        failureToReport = "testFailureToReport",
        medicationRequest = MOCK_MEDICATION_REQUEST
    )

    internal val MOCK_READY_EU_SYNCED_TASK = MOCK_SYNCED_TASK_DATA_01.copy(
        expiresOn = Clock.System.now() + 30.days,
        acceptUntil = Clock.System.now() + 30.days,
        status = TaskStatusEnum.Ready,
        isEuRedeemable = true
    )

    val mockProfile = ProfileErpModel(
        id = "profile-id-1",
        name = "Test Profile",
        active = true,
        isNewlyCreated = false,
        profileImageData = ProfileImageDataErpModel(
            color = ProfileColorNames.PINK,
            avatar = Avatar.PersonalizedImage,
            image = null
        ),
        insuranceData = ProfileInsuranceDataErpModel(
            insurantName = "Test User",
            insuranceIdentifier = "X123456789",
            insuranceName = "Test Insurance",
            insuranceType = InsuranceType.GKV,
            organizationIdentifier = null
        ),
        isConsentDrawerShown = false,
        lastAuthenticated = null,
        lastAuditEventSynced = null,
        lastTaskSynced = null,
        userAuthentication = UserAuthenticationErpModel.NotInitialized
    )

    val mockProfile2 = mockProfile.copy(
        id = "profile-id-2",
        name = "Second Profile",
        profileImageData = ProfileImageDataErpModel(
            color = ProfileColorNames.BLUE_MOON,
            avatar = Avatar.PersonalizedImage,
            image = null
        )
    )
}
