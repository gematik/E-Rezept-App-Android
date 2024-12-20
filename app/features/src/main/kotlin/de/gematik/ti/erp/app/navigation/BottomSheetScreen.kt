/*
 * Copyright 2024, gematik GmbH
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

@file:Suppress("UsingMaterialAndMaterial3Libraries")

package de.gematik.ti.erp.app.navigation

import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import de.gematik.ti.erp.app.theme.AppTheme

/**
 * The {currentRoute?.destination?.navigatorName} gives the navigator name of the current route.
 * This uses {composable} for screens and [BottomSheetNavigator] for bottom sheet screens.
 */
private const val BottomSheetNavigator = "BottomSheetNavigator"

/**
 * When a bottom-sheet screen is created and we add {Modifier.wrapContentSize()} to the content and
 * [forceToMaxHeight] is set to true, the bottom sheet will take the full height which is available
 * that is greater than the default height but less than the full screen height.
 */
abstract class BottomSheetScreen(
    private val forceToMaxHeight: Boolean = false
) : Screen() {

    @Composable
    fun BottomSheetContent() {
        Content(
            navController = navController,
            forceToMaxHeight = forceToMaxHeight
        ) {
            this@BottomSheetScreen.Content()
        }
    }
}

/**
Adding a bottom sheet inside the bottom-sheet navigation to allow forced full screen for smaller screens to allow
more than the allowed default height of the bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    navController: NavController,
    forceToMaxHeight: Boolean,
    content: @Composable () -> Unit
) {
    val modalBottomSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = forceToMaxHeight)

    ModalBottomSheet(
        sheetState = modalBottomSheetState,
        onDismissRequest = navController::navigateUp,
        containerColor = AppTheme.colors.neutral000,
        contentColor = AppTheme.colors.neutral000,
        dragHandle = {
            DragHandle(
                color = AppTheme.colors.neutral600
            )
        }
    ) {
        content()
    }
}

/**
 * This function is used to hide the bottom sheet impromptu.
 * It checks if the current route is a bottom sheet by checking its
 * navigator name and then pops it off the back stack.
 * This is used when we want to hide it when the user is asked to authenticate again.
 */
@Composable
@Suppress("ComposableNaming")
fun NavHostController.hideUnsafeBottomSheet() {
    val currentRoute by currentBackStackEntryAsState()
    val navigatorName = currentRoute?.destination?.navigatorName?.trim()
    LaunchedEffect(navigatorName) {
        if (navigatorName == BottomSheetNavigator) {
            navigateUp()
        }
    }
}
