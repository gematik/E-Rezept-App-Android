/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app

import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import kotlin.properties.ReadOnlyProperty

/**
 * Returns the qualified name of the property without the package and `TestTag` prefix.
 * Example:
 * ```
 * object TestTag {
 *     object Onboarding {
 *         val WelcomePage by tagName()
 *         ...
 * ```
 * will be `Onboarding.WelcomePage`.
 */
fun tagName(): ReadOnlyProperty<Any?, String> =
    ReadOnlyProperty { thisRef, property ->
        "${thisRef!!::class.qualifiedName!!.removePrefix("de.gematik.ti.erp.app.TestTag.")}.${property.name}"
    }

fun <T> testDataPropertyKey(name: String) =
    SemanticsPropertyKey<T?>(
        name = name,
        mergePolicy = { parentValue, _ ->
            parentValue
        }
    )

val PrescriptionId = testDataPropertyKey<String>(name = "PrescriptionId")
var SemanticsPropertyReceiver.prescriptionId by PrescriptionId

val PrescriptionIds = testDataPropertyKey<List<String>>(name = "PrescriptionIds")
var SemanticsPropertyReceiver.prescriptionIds by PrescriptionIds

val PharmacyId = testDataPropertyKey<String>(name = "PharmacyId")
var SemanticsPropertyReceiver.pharmacyId by PharmacyId

val InsuranceState = testDataPropertyKey<String>(name = "InsuranceState")
var SemanticsPropertyReceiver.insuranceState by InsuranceState

val SubstitutionAllowed = testDataPropertyKey<Boolean>(name = "SubstitutionAllowed")
var SemanticsPropertyReceiver.substitutionAllowed by SubstitutionAllowed

val SupplyForm = testDataPropertyKey<String>(name = "SupplyForm")
var SemanticsPropertyReceiver.supplyForm by SupplyForm

val MedicationCategory = testDataPropertyKey<String>(name = "MedicationCategory")
var SemanticsPropertyReceiver.medicationCategory by MedicationCategory

// Test tags for debug builds.
//
// Read before modifying!
//
// Developers: Use `@Deprecated(...)` for unused/old tags and always create a new tag with the by delegate `tagName()`.
// Testers: Replace `by tagName()` with an expressive name, e.g. `= "SomeName"`. `@Deprecated` identifiers are not used
//          anymore and should be replaced according their info.
object TestTag {
    // ...Screen = Scaffold
    // ...Content = LazyColumn

    object TopNavigation {
        val BackButton by tagName()
        val CloseButton by tagName()
    }

    object Prescriptions {
        val Content by tagName()
        val FullDetailPrescription by tagName()
        val FullDetailPrescriptionName by tagName()

        val PrescriptionRedeemable by tagName()
        val PrescriptionWaitForResponse by tagName()
        val PrescriptionInProgress by tagName()
        val PrescriptionRedeemed by tagName()

        val ArchiveButton by tagName()

        object Archive {
            val Content by tagName()
        }

        object Details {
            val Content by tagName()
            val Screen by tagName()

            val MoreButton by tagName()
            val DeleteButton by tagName()

            val MedicationButton by tagName()
            val PrescriberButton by tagName()
            val PatientButton by tagName()
            val OrganizationButton by tagName()
            val TechnicalInformationButton by tagName()

            object TechnicalInformation {
                val Content by tagName()
                val Screen by tagName()

                val AccessCode by tagName()
                val TaskId by tagName()
            }

            object Patient {
                val Content by tagName()
                val Screen by tagName()

                val BirthDate by tagName()
                val Name by tagName()
                val KVNR by tagName()
                val Address by tagName()
                val InsuranceName by tagName()
                val InsuranceState by tagName()
            }

            object Practitioner {
                val Content by tagName()
                val Screen by tagName()

                val Name by tagName()
                val Type by tagName()
                val LANR by tagName()
            }

            object Organization {
                val Content by tagName()
                val Screen by tagName()

                val Name by tagName()
                val Address by tagName()
                val BSNR by tagName()
                val Phone by tagName()
                val EMail by tagName()
            }

            object Medication {
                val Content by tagName()
                val Screen by tagName()

                val Name by tagName()
                val Amount by tagName()
                val PZN by tagName()
                val SupplyForm by tagName()
                val Category by tagName()
                val DosageInstruction by tagName()
                val Quantity by tagName()
                val FreeText by tagName()
                val BVG by tagName()

                val StandardSize by tagName()
                val SubstitutionAllowed by tagName()
                val Type by tagName()
            }
        }
    }

    object PharmacySearch {
        val OverviewContent by tagName()
        val OverviewScreen by tagName()
        val ResultContent by tagName()
        val ResultScreen by tagName()

        val TextSearchButton by tagName()
        val TextSearchField by tagName()
        val PharmacyListEntry by tagName()

        object OrderOptions {
            val Content by tagName()

            val PickUpOptionButton by tagName()
            val CourierDeliveryOptionButton by tagName()
            val MailDeliveryOptionButton by tagName()

            val ComposeToast by tagName()
        }

        object OrderPrescriptionSelection {
            val Content by tagName()
            val Screen by tagName()
        }

        object OrderSummary {
            val Content by tagName()
            val Screen by tagName()

            val PrescriptionSelectionButton by tagName()

            val SendOrderButton by tagName()
        }
    }

    object Orders {
        val Content by tagName()

        val OrderListItem by tagName()

        object Details {
            val Content by tagName()
            val Screen by tagName()

            val PrescriptionListItem by tagName()
            val MessageListItem by tagName()
        }

        object Messages {
            val Content by tagName()
            val Link by tagName()
            val LinkButton by tagName()
            val Text by tagName()
            val Code by tagName()
            val CodeLabelContent by tagName()
            val Empty by tagName()
        }
    }

    object Settings {
        val SettingsScreen by tagName()
        val DebugMenuButton by tagName()
        val ProfileButton by tagName()

        @Deprecated("add profile removed from settings screen")
        val AddProfileButton by tagName()

        val OrderNewCardButton by tagName()

        object AddProfileDialog {
            val Modal by tagName()
            val ProfileNameTextField by tagName()
            val ConfirmButton by tagName()
            val CancelButton by tagName()
        }

        object OrderEgk {
            val OrderEgkScreen by tagName()
            val OrderEgkContent by tagName()
            val SelectOrderOptionScreen by tagName()
            val SelectOrderOptionContent by tagName()
            val HealthCardOrderContactScreen by tagName()
            val HealthCardOrderContactScreenContent by tagName()
            val NFCExplanationPageLink by tagName()
            val ChooseInsuranceButton by tagName()
        }

        object ContactInsuranceCompany {
            val OrderEgkAndPinButton by tagName()
            val OrderPinButton by tagName()
            val TelephoneButton by tagName()
            val WebsiteButton by tagName()
            val MailToButton by tagName()
            val NoContactInfoTextBox by tagName()
        }

        object InsuranceCompanyList {
            val InsuranceSelectionScreen by tagName()
            val InsuranceSelectionContent by tagName()
            val ListOfInsuranceButtons by tagName()
        }
    }

    object AlertDialog {
        val Modal by tagName()
        val ConfirmButton by tagName()
        val CancelButton by tagName()
    }

    object DebugMenu {
        val FakeNFCCapabilities by tagName()
        val DebugMenuScreen by tagName()
        val DebugMenuContent by tagName()
        val CertificateField by tagName()
        val PrivateKeyField by tagName()
        val SetVirtualHealthCardButton by tagName()
        val FakeAppUpdate by tagName()
    }

    object BottomNavigation {
        val PrescriptionButton by tagName()
        val OrdersButton by tagName()
        val PharmaciesButton by tagName()
        val SettingsButton by tagName()
    }

    object Onboarding {
        val Pager by tagName()
        val SkipOnboardingButton by tagName()
        val NextButton by tagName()
        val ScreenContent by tagName()

        val WelcomeScreen by tagName()

        val CredentialsScreen by tagName()

        object Credentials {
            val BiometricTab by tagName()
            val PasswordTab by tagName()
            val PasswordFieldA by tagName()
            val PasswordFieldB by tagName()
            val PasswordStrengthCheck by tagName()
        }

        val AnalyticsScreen by tagName()
        val DataTermsScreen by tagName()

        object DataTerms {
            val AcceptDataTermsSwitch by tagName()
            val OpenTermsOfUseButton by tagName()
            val OpenDataProtectionButton by tagName()
        }

        val DataProtectionScreen by tagName()
        val TermsOfUseScreen by tagName()

        val AnalyticsSwitch by tagName()

        object Analytics {
            val ScreenContent by tagName()
            val AcceptAnalyticsButton by tagName()
        }
    }

    object Main {
        val MainScreen by tagName()
        val LoginButton by tagName()
        val CenterScreenMessageField by tagName()

        val AddProfileButton by tagName()
        object MainScreenBottomSheet {
            val Modal by tagName()
            val ProfileNameField by tagName()
            val ConnectLaterButton by tagName()
            val LoginButton by tagName()
            val GetConsentButton by tagName()
            val SaveProfileNameButton by tagName()
        }

        @Deprecated("Profile list selector removed from Mainscreen")
        object Profile {
            val OpenProfileListButton by tagName()
            val ProfileDetailsButton by tagName()
        }

        object OrderSuccessDialog {
            val Modal by tagName()
            val DismissButton by tagName()
        }
    }

    object Profile {
        val ProfileScreen by tagName()
        val ProfileScreenContent by tagName()
        val InvoicesScreen by tagName()
        val InvoicesDetailScreen by tagName()
        val InvoicesScreenContent by tagName()
        val OpenTokensScreenButton by tagName()
        val InsuranceId by tagName()
        val LoginButton by tagName()
        val ThreeDotMenuButton by tagName()
        val LogoutButton by tagName()
        val DeleteProfileButton by tagName()
        val EditProfileNameButton by tagName()
        val EditProfileImageButton by tagName()
        val NewProfileNameField by tagName()

        object EditProfileIcon {
            val ColorSelectorSpringGrayButton by tagName()
            val ColorSelectorSunDewButton by tagName()
            val ColorSelectorPinkButton by tagName()
            val ColorSelectorTreeButton by tagName()
            val ColorSelectorBlueMoonButton by tagName()
        }

        val OpenAuditEventsScreenButton by tagName()

        object TokenList {
            val TokenScreen by tagName()
            val AccessToken by tagName()
            val SSOToken by tagName()
            val NoTokenHeader by tagName()
            val NoTokenInfo by tagName()
        }

        object AuditEvents {
            val AuditEventsScreen by tagName()
            val NoAuditEventHeader by tagName()
            val NoAuditEventInfo by tagName()
            val AuditEvent by tagName()
        }
    }

    object CardWall {
        val ContinueButton by tagName()

        object Login {
            val LoginScreen by tagName()
        }

        object Intro {
            val IntroScreen by tagName()
            val OrderEgkButton by tagName()
        }

        object CAN {
            val CANScreen by tagName()
            val CANField by tagName()
            val OrderEgkButton by tagName()
        }

        object PIN {
            val PinScreen by tagName()
            val PINField by tagName()
            val OrderEgkButton by tagName()
        }

        object StoreCredentials {
            val Save by tagName()
            val DontSave by tagName()
        }

        object SecurityAcceptance {
            val AcceptButton by tagName()
        }

        object Nfc {
            val NfcScreen by tagName()
            val CardReadingDialog by tagName()
        }
    }
}
