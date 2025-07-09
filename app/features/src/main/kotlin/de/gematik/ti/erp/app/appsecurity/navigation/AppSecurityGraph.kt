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

package de.gematik.ti.erp.app.appsecurity.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.appsecurity.navigation.AppSecurityRoutes.DeviceCheckLoadingScreen
import de.gematik.ti.erp.app.appsecurity.ui.DeviceCheckLoadingStartScreen
import de.gematik.ti.erp.app.appsecurity.ui.InsecureDeviceScreen
import de.gematik.ti.erp.app.appsecurity.ui.IntegrityWarningScreen
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.navigation.toNavigationString
import io.github.aakira.napier.Napier

fun NavGraphBuilder.appSecurityGraph(
    navController: NavController,
    startDestination: String = DeviceCheckLoadingScreen.route,
    onAppSecurityPassed: () -> Unit
) {
    navigation(startDestination = startDestination, route = AppSecurityRoutes.subGraphName()) {
        renderComposable(
            route = DeviceCheckLoadingScreen.route
        ) {
            DeviceCheckLoadingStartScreen(
                navController = navController,
                navBackStackEntry = it,
                onSecurityCheckResult = { appSecurityResult ->
                    Napier.d(
                        tag = "AppSecurity check",
                        message = "Results from  DeviceCheckLoadingStartScreen\n" +
                            "isIntegrityAttested: ${appSecurityResult.isIntegritySecure}\n" +
                            "isDeviceSecure: ${appSecurityResult.isDeviceSecure}\n"
                    )
                    when {
                        appSecurityResult.isIntegritySecure && appSecurityResult.isDeviceSecure -> onAppSecurityPassed()
                        appSecurityResult.isIntegritySecure && !appSecurityResult.isDeviceSecure ->
                            @Requirement(
                                "O.Plat_1#3",
                                sourceSpecification = "BSI-eRp-ePA",
                                rationale = "Check for insecure Devices on app start and during onboarding."
                            )
                            navController.navigate(AppSecurityRoutes.InsecureDeviceScreen.path()) {
                                launchSingleTop = true
                            }

                        else -> {
                            navController.navigate(
                                route = AppSecurityRoutes.IntegrityWarningScreen.path(
                                    appSecurityResult.toNavigationString()
                                )
                            ) {
                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        }
        renderComposable(
            route = AppSecurityRoutes.IntegrityWarningScreen.route,
            arguments = AppSecurityRoutes.IntegrityWarningScreen.arguments
        ) {
            Napier.d(
                tag = "AppSecurity check",
                message = "IntegrityWarningScreen loaded"
            )
            IntegrityWarningScreen(
                navController = navController,
                navBackStackEntry = it,
                onBack = { isDeviceSecure ->
                    if (isDeviceSecure) {
                        onAppSecurityPassed()
                    } else {
                        navController.navigate(AppSecurityRoutes.InsecureDeviceScreen.path()) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        renderComposable(
            route = AppSecurityRoutes.InsecureDeviceScreen.route
        ) {
            Napier.d(
                tag = "AppSecurity check",
                message = "InsecureDeviceScreen loaded"
            )
            InsecureDeviceScreen(
                navController = navController,
                navBackStackEntry = it,
                onBack = onAppSecurityPassed
            )
        }
    }
}
