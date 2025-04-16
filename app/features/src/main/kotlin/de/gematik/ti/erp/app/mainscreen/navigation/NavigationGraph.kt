/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.mainscreen.navigation

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.composable
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import de.gematik.ti.erp.app.analytics.navigation.trackingGraph
import de.gematik.ti.erp.app.app.ApplicationInnerPadding
import de.gematik.ti.erp.app.appsecurity.navigation.AppSecurityRoutes
import de.gematik.ti.erp.app.appsecurity.navigation.appSecurityGraph
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.cardunlock.navigation.cardUnlockGraph
import de.gematik.ti.erp.app.cardwall.navigation.cardWallGraph
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.LocalBottomSheetNavigator
import de.gematik.ti.erp.app.core.LocalDi
import de.gematik.ti.erp.app.core.LocalNavController
import de.gematik.ti.erp.app.debugsettings.navigation.showcaseScreensGraph
import de.gematik.ti.erp.app.mainscreen.presentation.rememberAppController
import de.gematik.ti.erp.app.medicationplan.navigation.MedicationPlanRoutes
import de.gematik.ti.erp.app.messages.navigation.messagesGraph
import de.gematik.ti.erp.app.mlkit.navigation.mlKitGraph
import de.gematik.ti.erp.app.navigation.NavigationGraphBuilder
import de.gematik.ti.erp.app.navigation.navigateAndClearStack
import de.gematik.ti.erp.app.onboarding.navigation.OnboardingRoutes
import de.gematik.ti.erp.app.onboarding.navigation.onboardingGraph
import de.gematik.ti.erp.app.orderhealthcard.navigation.orderHealthCardGraph
import de.gematik.ti.erp.app.pharmacy.navigation.pharmacyGraph
import de.gematik.ti.erp.app.pkv.navigation.pkvGraph
import de.gematik.ti.erp.app.prescription.detail.navigation.prescriptionDetailGraph
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.prescription.navigation.prescriptionGraph
import de.gematik.ti.erp.app.medicationplan.navigation.medicationPlanGraph
import de.gematik.ti.erp.app.profiles.navigation.profileGraph
import de.gematik.ti.erp.app.redeem.navigation.redeemGraph
import de.gematik.ti.erp.app.settings.navigation.settingsGraph
import de.gematik.ti.erp.app.troubleshooting.navigation.troubleShootingGraph
import de.gematik.ti.erp.app.ui.DebugScreenWrapper
import de.gematik.ti.erp.app.userauthentication.navigation.UserAuthenticationRoutes
import de.gematik.ti.erp.app.userauthentication.navigation.userAuthenticationGraph
import de.gematik.ti.erp.app.userauthentication.observer.AuthenticationModeAndMethod

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterialNavigationApi::class)
@Suppress("LongMethod")
@Composable
fun NavigationGraph(
    authentication: AuthenticationModeAndMethod?,
    isDemoMode: Boolean,
    padding: ApplicationInnerPadding
) {
    val dependencyInjector = LocalDi.current
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    val navHostController = LocalNavController.current

    LocalActivity.current.setApplicationInnerPadding(padding)
    val currentActivity = LocalActivity.current as BaseActivity

    val mainScreenController = rememberAppController()

    val onboardingSucceeded = mainScreenController.onboardingSucceeded
    val showMedicationSuccess by currentActivity.shouldShowMedicationSuccess.collectAsStateWithLifecycle()

    val startDestinationScreen = shouldOnboardingBeDone(onboardingSucceeded, showMedicationSuccess)

    val currentBackStack by navHostController.currentBackStack.collectAsStateWithLifecycle()

    DisposableEffect(showMedicationSuccess) {
        if (showMedicationSuccess && currentBackStack.isNotEmpty()) {
            navHostController.navigate(MedicationPlanRoutes.MedicationPlanNotificationSuccess.path())
        }
        onDispose {
            currentActivity.medicationSuccessHasBeenShown()
        }
    }
    LaunchedEffect(authentication) {
        if (authentication is AuthenticationModeAndMethod.AuthenticationRequired && !isDemoMode) {
            navHostController.navigate(UserAuthenticationRoutes.UserAuthenticationScreen.path())
        }
    }

    NavigationGraphBuilder(
        bottomSheetNavigator = bottomSheetNavigator,
        navHostController = navHostController,
        startDestination = AppSecurityRoutes.subGraphName()
    ) {
        appSecurityGraph(navController = navHostController) {
            navHostController.navigateAndClearStack(route = startDestinationScreen)
        }
        onboardingGraph(
            dependencyInjector = dependencyInjector,
            navController = navHostController
        )
        mlKitGraph(navController = navHostController)
        pkvGraph(navController = navHostController)
        prescriptionGraph(navController = navHostController)
        prescriptionDetailGraph(navController = navHostController)
        messagesGraph(navController = navHostController)
        profileGraph(navController = navHostController)
        pharmacyGraph(
            dependencyInjector = dependencyInjector,
            navController = navHostController
        )
        orderHealthCardGraph(
            dependencyInjector = dependencyInjector,
            navController = navHostController
        )
        showcaseScreensGraph(navController = navHostController)
        trackingGraph(navController = navHostController)
        settingsGraph(navController = navHostController)
        troubleShootingGraph(navController = navHostController)
        cardUnlockGraph(
            dependencyInjector = dependencyInjector,
            navController = navHostController
        )
        medicationPlanGraph(navController = navHostController)
        cardWallGraph(
            dependencyInjector = dependencyInjector,
            navController = navHostController
        )
        redeemGraph(
            dependencyInjector = dependencyInjector,
            navController = navHostController
        )
        userAuthenticationGraph(navController = navHostController)
        composable(MainNavigationScreens.Debug.route) {
            DebugScreenWrapper(navHostController)
        }
    }
}

private fun shouldOnboardingBeDone(onboardingSucceeded: Boolean, showMedicationSuccess: Boolean): String =
    when (onboardingSucceeded) {
        true -> {
            if (showMedicationSuccess) {
                MedicationPlanRoutes.MedicationPlanNotificationSuccess.path()
            } else {
                PrescriptionRoutes.PrescriptionsScreen.path()
            }
        }
        false -> OnboardingRoutes.OnboardingWelcomeScreen.path()
    }

private fun ComponentActivity.setApplicationInnerPadding(
    padding: ApplicationInnerPadding
) {
    (this as? BaseActivity)?.applicationInnerPadding = padding
}
