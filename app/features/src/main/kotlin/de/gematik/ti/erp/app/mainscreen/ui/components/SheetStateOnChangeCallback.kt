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

package de.gematik.ti.erp.app.mainscreen.ui.components

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/**
 * This composable is used to react when the bottom sheet is dismissed
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SheetStateOnChangeCallback(
    sheetState: ModalBottomSheetState,
    onOpen: () -> Unit,
    onDismiss: () -> Unit
) {
    DisposableEffect(sheetState.currentValue) {
        onDispose {
            if (sheetState.currentValue == ModalBottomSheetValue.Hidden) {
                onDismiss()
            } else {
                onOpen()
            }
        }
    }
}
