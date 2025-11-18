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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.core

import androidx.activity.ComponentActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.navigation.BottomSheetNavigator
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import kotlinx.datetime.TimeZone
import org.kodein.di.DI

// Todo: some methods remaining from feature's AppContent.kt . Move all here after refactoring.

val LocalBiometricAuthenticator =
    staticCompositionLocalOf<BiometricAuthenticator> { error("No BiometricAuthenticator provided!") }

val LocalActivity =
    staticCompositionLocalOf<ComponentActivity> { error("No ComponentActivity provided!") }

val LocalDi = staticCompositionLocalOf<DI> { error("No DI provided!") }
val LocalTimeZone = staticCompositionLocalOf<TimeZone> { error("No Timezone provided!") }

val LocalBottomSheetNavigator =
    staticCompositionLocalOf<BottomSheetNavigator> { error("No BottomSheetNavigator provided!") }

@OptIn(ExperimentalMaterialApi::class)
val LocalBottomSheetNavigatorSheetState =
    staticCompositionLocalOf<ModalBottomSheetState> { error("No BottomSheetNavigator<ModalBottomSheetState> provided!") }

val LocalNavController =
    staticCompositionLocalOf<NavHostController> { error("No NavHostController provided!") }
