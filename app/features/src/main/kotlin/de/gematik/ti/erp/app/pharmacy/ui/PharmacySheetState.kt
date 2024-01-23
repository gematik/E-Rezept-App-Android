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

package de.gematik.ti.erp.app.pharmacy.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.SwipeableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Stable
class PharmacySheetState(
    content: PharmacySearchSheetContentState
) : SwipeableState<ModalBottomSheetValue>(
    initialValue = ModalBottomSheetValue.Hidden,
    animationSpec = SwipeableDefaults.AnimationSpec,
    confirmStateChange = { true }
) {
    lateinit var scope: CoroutineScope

    var content: PharmacySearchSheetContentState by mutableStateOf(content)
        private set

    val isVisible: Boolean
        get() = this.currentValue != ModalBottomSheetValue.Hidden

    fun show(content: PharmacySearchSheetContentState, snap: Boolean = false) {
        this.content = content
        scope.launch {
            val state = when (content) {
                is PharmacySearchSheetContentState.FilterSelected -> ModalBottomSheetValue.Expanded
                is PharmacySearchSheetContentState.PharmacySelected -> ModalBottomSheetValue.HalfExpanded
            }
            if (snap) {
                snapTo(state)
            } else {
                animateTo(state)
            }
        }
    }

    fun hide() {
        scope.launch {
            animateTo(ModalBottomSheetValue.Hidden)
        }
    }
}
