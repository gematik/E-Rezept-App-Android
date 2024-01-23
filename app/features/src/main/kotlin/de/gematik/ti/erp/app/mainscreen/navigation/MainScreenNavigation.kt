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

package de.gematik.ti.erp.app.mainscreen.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import de.gematik.ti.erp.app.LegalNoticeWithScaffold
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.analytics.trackNavigationChanges
import de.gematik.ti.erp.app.analytics.trackPopUps
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.cardunlock.ui.UnlockEgKScreen
import de.gematik.ti.erp.app.cardwall.ui.CardWallScreen
import de.gematik.ti.erp.app.core.LocalAnalytics
import de.gematik.ti.erp.app.debug.ui.DebugScreenWrapper
import de.gematik.ti.erp.app.features.BuildConfig
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.license.ui.LicenseScreen
import de.gematik.ti.erp.app.mainscreen.presentation.MainScreenController
import de.gematik.ti.erp.app.mainscreen.ui.InsecureDeviceScreen
import de.gematik.ti.erp.app.mainscreen.ui.MainScreenScaffoldContainer
import de.gematik.ti.erp.app.onboarding.ui.OnboardingNavigationScreens
import de.gematik.ti.erp.app.onboarding.ui.OnboardingScreen
import de.gematik.ti.erp.app.orderhealthcard.ui.HealthCardContactOrderScreen
import de.gematik.ti.erp.app.orders.ui.MessageScreen
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyNavigation
import de.gematik.ti.erp.app.prescription.detail.ui.PrescriptionDetailsScreen
import de.gematik.ti.erp.app.prescription.ui.ArchiveScreen
import de.gematik.ti.erp.app.prescription.ui.MlKitInformationScreen
import de.gematik.ti.erp.app.prescription.ui.MlKitIntroScreen
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceState
import de.gematik.ti.erp.app.prescription.ui.RefreshedState
import de.gematik.ti.erp.app.prescription.ui.ScanScreen
import de.gematik.ti.erp.app.prescription.ui.rememberPrescriptionsController
import de.gematik.ti.erp.app.profiles.presentation.rememberProfilesController
import de.gematik.ti.erp.app.profiles.ui.EditProfileScreen
import de.gematik.ti.erp.app.profiles.ui.ProfileImageCropper
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile.Companion.profileById
import de.gematik.ti.erp.app.redeem.ui.RedeemNavigation
import de.gematik.ti.erp.app.settings.ui.AllowAnalyticsScreen
import de.gematik.ti.erp.app.settings.ui.PharmacyLicenseScreen
import de.gematik.ti.erp.app.settings.ui.SecureAppWithPassword
import de.gematik.ti.erp.app.settings.ui.SettingsController
import de.gematik.ti.erp.app.settings.ui.SettingsScreen
import de.gematik.ti.erp.app.settings.ui.rememberSettingsController
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import de.gematik.ti.erp.app.webview.URI_DATA_TERMS
import de.gematik.ti.erp.app.webview.URI_TERMS_OF_USE
import de.gematik.ti.erp.app.webview.WebViewScreen

@RequiresApi(Build.VERSION_CODES.O)
@Requirement(
    "A_19178",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "see documentation-internal/security/mstg.adoc"
)
@Requirement(
    "A_19983",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "All used services except our analytics framework are permitted and attested by the Gematik and " +
        "under the TI monitoring. The usage of our analytics framework is not under our control, but we " +
        "exclusively send data to it and receive none."
)
@Requirement(
    "A_19979",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "We use external services: The Apothekenverzeichnis and our analytics framework. " +
        "During the communication with the pharmacy, there will be data shared via a prescription code. " +
        "The requirement to this feature is described in gemSpec_eRp_FdV section 5.2.3.10 and 5.2.3.11." +
        "Our analytics framework does not use medical personal data, see DSFA section 5.6 " +
        "Verarbeitungsvorgang 4: Rezepte einlösen"
)
@Requirement(
    "A_19182",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "In order to minimize the risk of unknown vulnerabilities in dependencies, we use different measures:" +
        "- We develop according to Security by Design Principles (see E-Rezept-App - SSDLC.pdf - Section " +
        "Richtlinien, Vorgaben und Best Practices)" +
        "- We train our engineers focussing on secure design and coding best practices " +
        "(see Sicherheitsschulungen.pdf)" +
        "- We publish our Code on Github and use a bug bounty program (https://www.gematik.de/datensicherheit -> " +
        "Coordinated Vulnerability Disclosure Program)"
)
@Requirement(
    "GS-A_5526",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "Renegotiation is disabled by default in BoringSSL:\n" +
        "'https://boringssl.googlesource.com/boringssl/+/9f69f139ed1088daabb6525f0c9c34d1e89688f7/PORTING.md" +
        "#:~:text=TLS%20renegotiation&text=Renegotiation%20is%20an%20extremely%20problematic," +
        "rejects%20peer%20renegotiations%20by%20default.'"
)
@Requirement(
    "O.Arch_1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "We implement an SSDLC"
)
@Requirement(
    "O.Arch_3",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "All cryptography is specified by gemSpec_Krypt in corporation with BSI"
)
@Requirement(
    "O.Arch_5",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Authentication and Authorization are not handled by the App itself. It rather takes place in the " +
        "IDP respectively the Relying Party. All communication is done with trustworhty backends, which is ensured" +
        "by certificate pinning."
)
@Requirement(
    "O.Arch_8",
    "O.Plat_11",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Webviews only display local content that is delivered together with the application. " +
        "Javascript is disabled, linking and loading other content is disabled. " +
        "All website open the system browser."
)
@Requirement(
    "O.Source_5",
    "O.Source_7",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Secure deletion in memory is not possible with the technologies used, since automatic memory " +
        "management is used. If there are errors in establishing the connection, the request is aborted. " +
        "Access to sensitive data in the backend is only possible after successful authentication."
)
@Requirement(
    "O.Source_6",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Automatic memory management is used in which the application cannot influence write and read access."
)
@Requirement(
    "O.TrdP_1",
    "O.TrdP_2",
    "O.TrdP_3",
    "O.TrdP_4",
    "O.TrdP_7",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "We use a dependency check that is evaluated cyclically. All libraries are source code dependencies" +
        "The dependencies are checked for vulnerabilities as part of the SAST/SCA checks. " +
        "We are currently in the evaluation phase for a new tool due to the discontinuation of " +
        "Microfocus Fortify. We also use the Owasp dependency check in the build pipeline."
)
@Requirement(
    "O.TrdP_5",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "We do not share sensitive data with third parties. Se data usages within `Purp_8` and `O.Arch_2"
)
@Requirement(
    "O.TrdP_6",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "See `O.Source_1` and `O.Arch_6."
)
@Requirement(
    "O.Cryp_1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Private keys for encryption are either created and stored within the key store or stored " +
        "within the eGK. As encryption for Server communication is done ephemeral via ECDH-ES, no static " +
        "private keys on client side are necessary."
)
@Requirement(
    "O.Cryp_2",
    "O.Cryp_3",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "All cryptographics are defined within `gemSpec_Krypt`. The document was created together with BSI."
)
@Requirement(
    "O.Cryp_5",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "We use Brainpool256R1 and AES256, see usages in O.Cryp_1 to O.Cryp_4"
)
@Requirement(
    "O.Auth_1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Our authentication concept is described in the following repository: https://github.com/gematik/" +
        "api-erp/blob/master/docs/authentisieren.adoc We have more detailed diagrams regarding the context " +
        "of the authentication in the SIS: Authentication on the central IDP: " +
        "E-Rezept-App-Authentifizierungskonzept.pdf" +
        "Authentication via Fast Track: E-Rezept-App-Authentifizierungskonzept_Fast_Track.pdf"
)
@Requirement(
    "O.Auth_2",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "There is no client side separation of authentication and authorization. " +
        "The authentication takes place at the identity provider, the authorization is realized by the Fachdienst"
)
@Requirement(
    "O.Auth_3",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "One way to connect to the FD ist to login by using the eGK. To login with the eGK, the card, " +
        "a CAN and a PIN is needed. The other way is to use a insurance company provided app. " +
        "These apps must implement a second factor as well. " +
        "See all `CardWall` prefixed files for all implementation details."
)
@Requirement(
    "O.Auth_4",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "There is no step up from always using 2nd factor authentication. Tokens have short lifetimes " +
        "of 12h (server defined) SSO-Tokens and 5min Access-Tokens."
)
@Requirement(
    "O.Auth_5",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "A Audit Log for every FD access is available in each user profile."
)
@Requirement(
    "O.Auth_6",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "There are not passwords to guess for server login within our application. " +
        "Only eGK login is directly available within the application. The users application password is not " +
        "delayed, as any user with PIN access would have access to the unencrypted file system as well."
)
@Requirement(
    "O.Auth_9",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Token invalidation happens after 12 hours. If a user is still active, a reauthentication via eGK, " +
        "Biometrics or Insurance App is necessary. " +
        "Each meaning the posession and or knowledge of the needed user input."
)
@Requirement(
    "O.Auth_10",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Authentication via eGK cannot be altered, as the physical card cannot be modified without " +
        "authentication (e.g. PIN change). See gemSpec_COS for details. Adding a authentication key that " +
        "is secured via biometrics (labeld as \"save login\" within the cardwall) enforces a new " +
        "authentication via eGK on server side."
)
@Requirement(
    "O.Auth_12",
    "O.Data_19",
    "O.Plat_4",
    "O.Plat_5",
    "O.Plat_8",
    "O.Plat_9",
    "O.Resi_7",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "unused"
)
@Requirement(
    "O.Plat_16",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Since we implement the \"security-by-default\" principle,  we don't have additional security " +
        "measures which the user can use to increase the security level. All measures are built-in."
)
@Requirement(
    "O.Pass_4",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "There is no protocol for the application password within the application. "
)
@Requirement(
    "O.Sess_1", "O.Sess_2", "O.Sess_3", "O.Sess_4", "O.Sess_5", "O.Sess_6", "O.Sess_7",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "not applicable"
)
@Requirement(
    "O.Paid_1", "O.Paid_2", "O.Paid_3", "O.Paid_4", "O.Paid_5", "O.Paid_6", "O.Paid_7",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "The app does not offer any purchases."
)
@Requirement(
    "O.Tokn_1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "For long-lived tokens, the token is stored in the secured realm database. Ephemeral tokens, " +
        "such as those used in the biometric pairing process, do not persist and only remain in memory."
)
@Requirement(
    "O.Tokn_2",
    "O.Tokn_3",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "The token is created by the backend, we have no means of manipulating the content."
)
@Requirement(
    "O.Tokn_4",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "The token is created by the IDP and signed there. We have not valid signing identity " +
        "within the application to sign the token."
)
@Requirement(
    "O.Tokn_5",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "A Section within User Profiles contains all tokens for that profile on the device."
)
@Requirement(
    "O.Data_5",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Secure deletion in memory is not possible with the technologies used, since automatic memory " +
        "management is used. Ephemeral private keys are released as soon as they are no longer needed"
)
@Requirement(
    "O.Purp_2",
    "O.Data_6",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Collected data is sparse and use case related as required."
)
@Requirement(
    "O.Purp_8",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "The purpose of data usage is described in section 4 and 5 of the Data Terms. " +
        "Interfaces to external services can be found in the following package: " +
        "common/src/commonMain/kotlin/de/gematik/ti/erp/app/api/*"
)
@Requirement(
    "O.Purp_9",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "The purposes are described in the data terms in sections 4 and 5. " +
        "All data presented are necessary besides the display of the tokens by the IDP. " +
        "However, these are presented in the app due to O.Token_5."
)
@Requirement(
    "O.Data_7",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Private data is not initially created by the application. Only additional information, such as " +
        "redeeming or deleting prescriptions create data. The created data is kept on the FD."
)
@Requirement(
    "O.Data_12",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "There is no API that allows extraction of biometric data."
)
@Requirement(
    "O.Data_15",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "All user data is stored in the private data area of the app and thus, cannot be accessed by another " +
        "app. Furthermore, we have only encrypted data stored on the device. This excludes the health data from the " +
        "backend, which can be accessed with legitimate authentication from other devices as well."
)
@Requirement(
    "O.Data_16",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Storing application data is not possible."
)
@Requirement(
    "O.Data_17",
    "O.Data_18",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Uninstalling the app will delete all data stored by the e-prescription app on the device. By " +
        "default, files that created on internal storage are accessible only to the app. Android implements " +
        "this protection by default. The user may choose to manually logout or delete profiles before app deletion."
)
@Requirement(
    "O.Ntwk_4",
    "O.Ntwk_7",
    "O.Resi_6",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "See network_security_config and  AndroidManifest.xml for settings and pinned domains," +
        " no exceptions are made"
)
@Requirement(
    "O.Ntwk_5",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "By using OkHttp-Client handles that within the application."
)
@Requirement(
    "O.Ntwk_6",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "The server uses extended validation certificates to ensure maximum authenticity."
)
@Requirement(
    "O.Ntwk_8",
    "O.Ntwk_9",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "see results of backend system assessment."
)
@Requirement(
    "O.Plat_1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "We test for device security at startup and show a dialog if the device is not secured."
)
@Requirement(
    "O.Plat_2",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "See AndroidManifest.xml for all accessed entitlements."
)
@Requirement(
    "O.Plat_3",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "We use the platform dialoges for this."
)
@Requirement(
    "O.Plat_6",
    "O.Plat_7",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Implemented by the OS Sandboxing."
)
@Requirement(
    "O.Plat_10",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Interprocesscommunication is implemented using Universal Linking. Current use cases are limited " +
        "to login with insurance company apps. No sensitive data is transferred, the actual payload is decided " +
        "by the server."
)
@Requirement(
    "O.Plat_12",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "We use the platform dialoges for this."
)
@Requirement(
    "O.Resi_1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "The application guides the user through the onboarding process and suggests the user the most " +
        "secure mechanisms available on this device."
)
@Requirement(
    "O.Resi_8",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Does not make sense. This Project is an open source project."
)
@Requirement(
    "O.Resi_9",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "We use API keys that are assigned to fixed app versions at the backend service. " +
        "We also have minimum requirements for the supported platform versions. " +
        "There are minimum supported API levels, currently >= 24"
)
@Requirement(
    "O.Resi_10",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "There are persistent data in the app, which means that the app is temporarily resilient to the " +
        "unavailability of the backend service. As soon as data is persisted on the device, it can be " +
        "accessed again even after local disruptions."
)
@Suppress("LongMethod")
@Composable
fun MainScreenNavigation(
    navController: NavHostController
) {
    val settingsController = rememberSettingsController()
    val startDestination = checkFirstAppStart(settingsController)
    LaunchedEffect(startDestination) {
        // `gemSpec_eRp_FdV A_20203` default settings are not allow screenshots
        // (on debug builds should be allowed for testing)
        if (BuildConfig.DEBUG && startDestination == "onboarding") {
            settingsController.onAllowScreenshots()
        }
    }
    val analytics = LocalAnalytics.current
    val analyticsState by analytics.screenState
    trackPopUps(analytics, analyticsState)
    var previousNavEntry by remember { mutableStateOf("main") }
    trackNavigationChanges(navController, previousNavEntry, onNavEntryChange = { previousNavEntry = it })
    val navigationMode by navController.navigationModeState(OnboardingNavigationScreens.Onboarding.route)
    NavHost(
        navController,
        startDestination = startDestination
    ) {
        composable(MainNavigationScreens.Onboarding.route) {
            OnboardingScreen(
                mainNavController = navController,
                settingsController = settingsController
            )
        }
        composable(MainNavigationScreens.DataProtection.route) {
            NavigationAnimation(mode = navigationMode) {
                @Requirement(
                    "O.Arch_8#2",
                    "O.Plat_11#2",
                    sourceSpecification = "BSI-eRp-ePA",
                    rationale = "Webview containing local html without javascript"
                )
                WebViewScreen(
                    title = stringResource(R.string.onb_data_consent),
                    onBack = { navController.popBackStack() },
                    url = URI_DATA_TERMS
                )
            }
        }
        composable(
            MainNavigationScreens.Settings.route,
            MainNavigationScreens.Settings.arguments
        ) {
            SettingsScreen(
                mainNavController = navController,
                settingsController = settingsController
            )
        }
        composable(MainNavigationScreens.Camera.route) {
            ScanScreen(mainNavController = navController)
        }
        composable(MainNavigationScreens.Prescriptions.route) {
            @Requirement(
                "O.Plat_1#2",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "Check for insecure Devices on MainScreen."
            )
            MainScreenScaffoldContainer(
                mainNavController = navController,
                onDeviceIsInsecure = {
                    navController.navigate(MainNavigationScreens.IntegrityNotOkScreen.path())
                }
            )
        }
        composable(
            MainNavigationScreens.PrescriptionDetail.route,
            MainNavigationScreens.PrescriptionDetail.arguments
        ) {
            val taskId = remember { requireNotNull(it.arguments?.getString("taskId")) }
            PrescriptionDetailsScreen(taskId = taskId, mainNavController = navController)
        }
        composable(
            MainNavigationScreens.Pharmacies.route,
            MainNavigationScreens.Pharmacies.arguments
        ) {
            PharmacyNavigation(
                onBack = {
                    navController.popBackStack()
                },
                onFinish = {
                    navController.navigate(MainNavigationScreens.Prescriptions.route)
                }
            )
        }
        composable(MainNavigationScreens.InsecureDeviceScreen.route) {
            @Requirement(
                "O.Plat_1#4",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "insecure Devices warning."
            )
            InsecureDeviceScreen(
                stringResource(id = R.string.insecure_device_title),
                painterResource(id = R.drawable.laptop_woman_yellow),
                stringResource(id = R.string.insecure_device_header),
                stringResource(id = R.string.insecure_device_info),
                stringResource(id = R.string.insecure_device_accept)
            ) {
                navController.navigate(MainNavigationScreens.Prescriptions.route)
            }
        }
        composable(MainNavigationScreens.MlKitIntroScreen.route) {
            MlKitIntroScreen(
                navController,
                settingsController
            )
        }
        composable(MainNavigationScreens.MlKitInformationScreen.route) {
            MlKitInformationScreen(
                navController
            )
        }
        composable(MainNavigationScreens.IntegrityNotOkScreen.route) {
            InsecureDeviceScreen(
                stringResource(id = R.string.insecure_device_title_safetynet),
                painterResource(id = R.drawable.laptop_woman_pink),
                stringResource(id = R.string.insecure_device_header_safetynet),
                stringResource(id = R.string.insecure_device_info_safetynet),
                stringResource(id = R.string.insecure_device_accept_safetynet),
                pinUseCase = false
            ) {
                navController.navigate(MainNavigationScreens.Prescriptions.route)
            }
        }
        composable(
            MainNavigationScreens.Redeem.route
        ) {
            RedeemNavigation(
                onFinish = {
                    navController.navigate(MainNavigationScreens.Prescriptions.route)
                }
            )
        }
        composable(
            MainNavigationScreens.Messages.route,
            MainNavigationScreens.Messages.arguments
        ) {
            val orderId =
                remember { it.arguments?.getString("orderId")!! }

            MessageScreen(
                orderId = orderId,
                mainNavController = navController
            )
        }
        composable(
            MainNavigationScreens.CardWall.route,
            MainNavigationScreens.CardWall.arguments
        ) {
            val profileId =
                remember { it.arguments?.getString("profileId")!! }
            CardWallScreen(
                navController,
                onResumeCardWall = {
                    navController.navigate(
                        MainNavigationScreens.Prescriptions.path(),
                        navOptions {
                            popUpTo(MainNavigationScreens.Prescriptions.route) {
                                inclusive = true
                            }
                        }
                    )
                },
                profileId = profileId
            )
        }
        composable(
            MainNavigationScreens.EditProfile.route,
            MainNavigationScreens.EditProfile.arguments
        ) {
            val profileId =
                remember { navController.currentBackStackEntry?.arguments?.getString("profileId")!! }
            EditProfileScreen(
                profileId,
                onBack = { navController.popBackStack() },
                mainNavController = navController
            )
        }
        composable(MainNavigationScreens.Debug.route) {
            DebugScreenWrapper(navController)
        }
        composable(MainNavigationScreens.Terms.route) {
            NavigationAnimation(mode = navigationMode) {
                @Requirement(
                    "O.Arch_8#3",
                    "O.Plat_11#3",
                    sourceSpecification = "BSI-eRp-ePA",
                    rationale = "Webview containing local html without javascript"
                )
                WebViewScreen(
                    title = stringResource(R.string.onb_terms_of_use),
                    onBack = { navController.popBackStack() },
                    url = URI_TERMS_OF_USE
                )
            }
        }
        composable(MainNavigationScreens.Imprint.route) {
            NavigationAnimation(mode = navigationMode) {
                LegalNoticeWithScaffold(
                    navController
                )
            }
        }
        composable(MainNavigationScreens.DataProtection.route) {
            NavigationAnimation(mode = navigationMode) {
                @Requirement(
                    "O.Arch_8#4",
                    "O.Plat_11#4",
                    sourceSpecification = "BSI-eRp-ePA",
                    rationale = "Webview containing local html without javascript"
                )
                WebViewScreen(
                    title = stringResource(R.string.onb_data_consent),
                    onBack = { navController.popBackStack() },
                    url = URI_DATA_TERMS
                )
            }
        }
        composable(MainNavigationScreens.OpenSourceLicences.route) {
            NavigationAnimation(mode = navigationMode) {
                LicenseScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(MainNavigationScreens.AdditionalLicences.route) {
            NavigationAnimation(mode = navigationMode) {
                PharmacyLicenseScreen {
                    navController.popBackStack()
                }
            }
        }
        composable(MainNavigationScreens.AllowAnalytics.route) {
            NavigationAnimation(mode = navigationMode) {
                AllowAnalyticsScreen(
                    onBack = { navController.popBackStack() },
                    onAllowAnalytics = {
                        if (it) {
                            settingsController.onTrackingAllowed()
                        } else {
                            settingsController.onTrackingDisallowed()
                        }
                    }
                )
            }
        }
        composable(MainNavigationScreens.Password.route) {
            NavigationAnimation(mode = navigationMode) {
                SecureAppWithPassword(
                    navController,
                    settingsController
                )
            }
        }
        composable(MainNavigationScreens.OrderHealthCard.route) {
            HealthCardContactOrderScreen(onBack = { navController.popBackStack() })
        }
        composable(
            MainNavigationScreens.EditProfile.route,
            MainNavigationScreens.EditProfile.arguments
        ) {
            val profilesController = rememberProfilesController()
            val profileId = remember { it.arguments?.getString("profileId") }
            val profiles by profilesController.getProfilesState()

            profiles.profileById(profileId)?.let { profile ->
                EditProfileScreen(
                    profile,
                    profilesController,
                    onRemoveProfile = {
                        profilesController.removeProfile(profile, it)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() },
                    mainNavController = navController
                )
            }
        }

        composable(
            MainNavigationScreens.ProfileImageCropper.route,
            MainNavigationScreens.ProfileImageCropper.arguments
        ) {
            val profileId = remember { it.arguments!!.getString("profileId")!! }
            val profilesController = rememberProfilesController()
            ProfileImageCropper(
                onSaveCroppedImage = {
                    profilesController.savePersonalizedProfileImage(profileId, it)
                    navController.navigate(MainNavigationScreens.Prescriptions.path())
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            MainNavigationScreens.UnlockEgk.route,
            MainNavigationScreens.UnlockEgk.arguments
        ) {
            val unlockMethod = remember { it.arguments!!.getString("unlockMethod") }

            NavigationAnimation(mode = navigationMode) {
                UnlockEgKScreen(
                    unlockMethod = when (unlockMethod) {
                        UnlockMethod.ChangeReferenceData.name -> UnlockMethod.ChangeReferenceData
                        UnlockMethod.ResetRetryCounter.name -> UnlockMethod.ResetRetryCounter
                        UnlockMethod.ResetRetryCounterWithNewSecret.name -> UnlockMethod.ResetRetryCounterWithNewSecret
                        else -> UnlockMethod.None
                    },
                    onCancel = { navController.popBackStack() },
                    onClickLearnMore = {
                        navController.navigate(
                            MainNavigationScreens.OrderHealthCard.path()
                        )
                    }
                )
            }
        }

        composable(
            MainNavigationScreens.Archive.route
        ) {
            val prescriptionController = rememberPrescriptionsController()

            NavigationAnimation(mode = navigationMode) {
                ArchiveScreen(
                    prescriptionsController = prescriptionController,
                    navController = navController
                ) {
                    navController.popBackStack()
                }
            }
        }
    }
}

@Composable
private fun checkFirstAppStart(settingsController: SettingsController) =
    if (settingsController.showOnboarding) {
        MainNavigationScreens.Onboarding.route
    } else {
        MainNavigationScreens.Prescriptions.route
    }

@Composable
fun calculatePrescriptionCount(mainScreenController: MainScreenController): Int {
    var prescriptionCount = 0
    var refreshEvent by remember { mutableStateOf<PrescriptionServiceState?>(null) }

    LaunchedEffect(Unit) {
        mainScreenController.onRefreshEvent.collect {
            refreshEvent = it
        }
    }
    refreshEvent?.let {
        when (it) {
            is RefreshedState -> {
                prescriptionCount = it.nrOfNewPrescriptions
            }
        }
    }
    return prescriptionCount
}
