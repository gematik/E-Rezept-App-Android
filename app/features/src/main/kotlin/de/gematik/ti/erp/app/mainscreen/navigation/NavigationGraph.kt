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

package de.gematik.ti.erp.app.mainscreen.navigation

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.composable
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import de.gematik.ti.erp.app.analytics.navigation.trackingGraph
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
import de.gematik.ti.erp.app.digas.navigation.DigasRoutes
import de.gematik.ti.erp.app.digas.navigation.digasGraph
import de.gematik.ti.erp.app.mainscreen.presentation.rememberAppController
import de.gematik.ti.erp.app.medicationplan.navigation.MedicationPlanRoutes
import de.gematik.ti.erp.app.medicationplan.navigation.medicationPlanGraph
import de.gematik.ti.erp.app.messages.navigation.messagesGraph
import de.gematik.ti.erp.app.mlkit.navigation.mlKitGraph
import de.gematik.ti.erp.app.navigation.NavigationGraphBuilder
import de.gematik.ti.erp.app.navigation.navigateAndClearStack
import de.gematik.ti.erp.app.onboarding.navigation.OnboardingRoutes
import de.gematik.ti.erp.app.onboarding.navigation.onboardingGraph
import de.gematik.ti.erp.app.orderhealthcard.navigation.orderHealthCardGraph
import de.gematik.ti.erp.app.padding.ApplicationInnerPadding
import de.gematik.ti.erp.app.pkv.navigation.pkvGraph
import de.gematik.ti.erp.app.prescription.detail.navigation.prescriptionDetailGraph
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.prescription.navigation.prescriptionGraph
import de.gematik.ti.erp.app.profiles.navigation.profileGraph
import de.gematik.ti.erp.app.settings.navigation.settingsGraph
import de.gematik.ti.erp.app.shared.navigation.redeemAndPharmacySharedGraph
import de.gematik.ti.erp.app.translation.navigation.translationGraph
import de.gematik.ti.erp.app.troubleshooting.navigation.troubleShootingGraph
import de.gematik.ti.erp.app.ui.DebugScreenWrapper
import de.gematik.ti.erp.app.userauthentication.navigation.UserAuthenticationRoutes
import de.gematik.ti.erp.app.userauthentication.navigation.userAuthenticationGraph
import de.gematik.ti.erp.app.userauthentication.observer.AuthenticationModeAndMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterialNavigationApi::class)
@Suppress("LongMethod")
@Composable
fun NavigationGraph(
    authentication: AuthenticationModeAndMethod?,
    isDemoMode: Boolean,
    padding: ApplicationInnerPadding,
    digaPromptFeedback: Flow<Boolean>,
    onDigaNavigationActivated: () -> Unit
) {
    val dependencyInjector = LocalDi.current
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    val navHostController = LocalNavController.current

    val authRequired = remember(authentication, isDemoMode) {
        authentication is AuthenticationModeAndMethod.AuthenticationRequired && !isDemoMode
    }

    val isAuthenticated = remember(authentication, isDemoMode) {
        authentication is AuthenticationModeAndMethod.Authenticated && !isDemoMode
    }

    LocalActivity.current.setApplicationInnerPadding(padding)
    val currentActivity = LocalActivity.current as BaseActivity

    val mainScreenController = rememberAppController()

    val onboardingSucceeded = mainScreenController.onboardingSucceeded
    val showMedicationSuccess by currentActivity.pendingNavigationToMedicationNotificationScreen.collectAsStateWithLifecycle()

    val startDestinationScreen = calculateStartDestination(onboardingSucceeded, showMedicationSuccess)

    LaunchedEffect(authRequired, showMedicationSuccess) {
        if (authRequired) {
            navHostController.navigate(UserAuthenticationRoutes.UserAuthenticationScreen.path())
        }
        if (isAuthenticated && showMedicationSuccess) {
            navHostController.navigate(MedicationPlanRoutes.MedicationPlanNotificationScreen.path())
        }
    }

    ObserveDigaFeedbackNavigation(digaPromptFeedback, isAuthenticated) {
        navHostController.navigate(DigasRoutes.DigaFeedbackPromptScreen.path())
        onDigaNavigationActivated()
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
        redeemAndPharmacySharedGraph(
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
        cardWallGraph(navController = navHostController)
        digasGraph(
            navController = navHostController
        )
        userAuthenticationGraph(navController = navHostController)
        translationGraph(navController = navHostController)
        composable(MainNavigationScreens.Debug.route) {
            DebugScreenWrapper(navHostController)
        }
    }
}

@Composable
private fun ObserveDigaFeedbackNavigation(
    promptDigaFeedbackFlow: Flow<Boolean>,
    isAuthenticated: Boolean,
    onNavigate: () -> Unit
) {
    LaunchedEffect(isAuthenticated) {
        promptDigaFeedbackFlow.collectLatest { shouldNavigate ->
            if (shouldNavigate && isAuthenticated) {
                onNavigate()
            }
        }
    }
}

private fun calculateStartDestination(onboardingSucceeded: Boolean, showMedicationSuccess: Boolean): String =
    when (onboardingSucceeded) {
        true -> {
            if (showMedicationSuccess) {
                MedicationPlanRoutes.MedicationPlanNotificationScreen.path()
            } else {
                PrescriptionRoutes.PrescriptionListScreen.path()
            }
        }

        false -> OnboardingRoutes.OnboardingWelcomeScreen.path()
    }

private fun ComponentActivity.setApplicationInnerPadding(
    padding: ApplicationInnerPadding
) {
    (this as? BaseActivity)?.applicationInnerPadding = padding
}
