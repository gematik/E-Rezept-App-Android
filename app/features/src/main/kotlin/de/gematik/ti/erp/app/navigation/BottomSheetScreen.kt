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

package de.gematik.ti.erp.app.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable

/**
 * @param skipPartiallyExpanded Whether the partially expanded state, if the sheet is tall enough,
 *  should be skipped. If true, the sheet will always expand to the [Expanded] state and move to the
 *  [Hidden] state when hiding the sheet, either programmatically or by user interaction.
 *
 *  @param allowStateChange Optional callback invoked to confirm or veto a pending state change.
 */
abstract class BottomSheetScreen(
    val skipPartiallyExpanded: Boolean = true,
    val allowStateChange: Boolean = true
) : Screen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BottomSheetContent() {
        ModalBottomSheet(
            onDismissRequest = { navController.popBackStack() },
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = skipPartiallyExpanded,
                confirmValueChange = { allowStateChange }
            )
        ) {
            this@BottomSheetScreen.Content()
        }
    }
}
