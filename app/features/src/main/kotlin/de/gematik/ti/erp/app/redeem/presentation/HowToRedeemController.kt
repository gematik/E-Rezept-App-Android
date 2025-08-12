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

package de.gematik.ti.erp.app.redeem.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.profiles.presentation.GetActiveProfileController
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.redeem.usecase.HasEuRedeemablePrescriptionsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import org.kodein.di.compose.rememberInstance
import androidx.compose.runtime.State as ComposeState

@Suppress("ConstructorParameterNaming")
@Stable
class HowToRedeemController(
    getActiveProfileUseCase: GetActiveProfileUseCase,
    private val hasEuRedeemablePrescriptionsUseCase: HasEuRedeemablePrescriptionsUseCase
) : GetActiveProfileController(getActiveProfileUseCase) {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val hasEuRedeemablePrescriptionsFlow: Flow<Boolean> by lazy {
        activeProfile.flatMapLatest { profile ->
            val profileData = profile.data
            if (profileData != null) {
                hasEuRedeemablePrescriptionsUseCase(profileData.id)
            } else {
                flowOf(false)
            }
        }
    }

    val hasEuRedeemablePrescriptions: ComposeState<Boolean>
        @Composable
        get() = hasEuRedeemablePrescriptionsFlow.collectAsStateWithLifecycle(false)
}

@Composable
fun rememberHowToRedeemController(): HowToRedeemController {
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val hasEuRedeemablePrescriptionsUseCase by rememberInstance<HasEuRedeemablePrescriptionsUseCase>()

    return remember {
        HowToRedeemController(
            getActiveProfileUseCase = getActiveProfileUseCase,
            hasEuRedeemablePrescriptionsUseCase = hasEuRedeemablePrescriptionsUseCase
        )
    }
}
