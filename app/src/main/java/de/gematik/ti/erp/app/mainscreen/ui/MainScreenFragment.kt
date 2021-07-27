/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import de.gematik.ti.erp.app.core.ComposeBaseFragment
import de.gematik.ti.erp.app.messages.ui.MessageViewModel
import de.gematik.ti.erp.app.prescription.ui.PrescriptionViewModel
import de.gematik.ti.erp.app.redeem.ui.RedeemViewModel

val LocalPermissionLauncher =
    staticCompositionLocalOf<ActivityResultLauncher<String>> { error("No activity launcher provided!") }

val LocalPermissionResult =
    staticCompositionLocalOf<MutableState<Boolean>> { error("No activity result provided!") }

@AndroidEntryPoint
class MainScreenFragment : ComposeBaseFragment() {
    private val prescriptionScreenViewModel by viewModels<PrescriptionViewModel>()
    private val mainScreenViewModel by viewModels<MainScreenViewModel>()
    private val redeemViewModel by viewModels<RedeemViewModel>()
    private val messageViewModel by viewModels<MessageViewModel>()

    private val permissionResult = mutableStateOf(false, policy = neverEqualPolicy())
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            permissionResult.value = it
        }

    override fun viewModels() =
        setOf(
            prescriptionScreenViewModel,
            mainScreenViewModel,
            redeemViewModel,
            messageViewModel
        )

    override val content = @Composable {
        CompositionLocalProvider(
            LocalPermissionLauncher provides permissionLauncher,
            LocalPermissionResult provides permissionResult
        ) {
            MainScreen()
        }
    }
}
