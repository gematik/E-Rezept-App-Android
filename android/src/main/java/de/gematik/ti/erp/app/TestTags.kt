/*
 * Copyright (c) 2022 gematik GmbH
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

// Test tags for debug builds.
//
// Read before modifying!
//
// Developers: Use `@Deprecated(...)` for unused/old tags and always create a new tag with the by delegate `tagName()`.
// Testers: Replace `by tagName()` with an expressive name, e.g. `= "SomeName"`. `@Deprecated` identifiers are not used
//          anymore and should be replaced according their info.
object TestTag {
    object TopNavigation {
        val BackButton by tagName()
        val CloseButton by tagName()
    }

    object Settings {
        val SettingsScreen by tagName()
        val DebugMenuButton by tagName()
        val ProfileButton by tagName()
        val AddProfileButton by tagName()

        object AddProfileDialog {
            val ProfileNameTextField by tagName()
            val ConfirmButton by tagName()
        }
    }

    object DebugMenu {
        val DebugMenuScreen by tagName()
        val DebugMenuContent by tagName()
        val CertificateField by tagName()
        val PrivateKeyField by tagName()
        val SetVirtualHealthCardButton by tagName()
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

        val WelcomeScreen by tagName()
        val FeatureScreen by tagName()
        val ProfileScreen by tagName()

        object Profile {
            val ProfileField by tagName()
        }

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
            val TermsOfUseSwitch by tagName()
            val OpenTermsOfUseButton by tagName()
            val DataProtectionSwitch by tagName()
            val OpenDataProtectionButton by tagName()
        }
        val DataProtectionScreen by tagName()
        val TermsOfUseScreen by tagName()
    }

    object Main {
        val MainScreen by tagName()
        val LoginButton by tagName()
        object Profile {
            val OpenProfileListButton by tagName()
            val ProfileDetailsButton by tagName()
        }
    }

    object Profile {
        val ProfileScreen by tagName()
        val ProfileScreenContent by tagName()
        val OpenTokensScreenButton by tagName()
        val InsuranceId by tagName()
        val LoginButton by tagName()
        val ThreeDotMenuButton by tagName()
        val LogoutButton by tagName()
        val DeleteProfileButton by tagName()
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
        object CAN {
            val CANField by tagName()
        }
        object PIN {
            val PINField by tagName()
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
