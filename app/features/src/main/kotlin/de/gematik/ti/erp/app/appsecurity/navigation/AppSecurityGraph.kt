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

package de.gematik.ti.erp.app.appsecurity.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.appsecurity.navigation.AppSecurityRoutes.DeviceCheckLoadingScreen
import de.gematik.ti.erp.app.appsecurity.ui.DeviceCheckLoadingStartScreen
import de.gematik.ti.erp.app.appsecurity.ui.InsecureDeviceScreen
import de.gematik.ti.erp.app.appsecurity.ui.IntegrityWarningScreen
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.navigation.toNavigationString
import io.github.aakira.napier.Napier

fun NavGraphBuilder.appSecurityGraph(
    startDestination: String = DeviceCheckLoadingScreen.route,
    navController: NavController,
    onAppSecurityPassed: () -> Unit
) {
    navigation(startDestination = startDestination, route = AppSecurityRoutes.subGraphName()) {
        renderComposable(
            route = DeviceCheckLoadingScreen.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up)
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down)
            }
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
            arguments = AppSecurityRoutes.IntegrityWarningScreen.arguments,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up)
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down)
            }
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
            route = AppSecurityRoutes.InsecureDeviceScreen.route,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up)
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down)
            }
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
