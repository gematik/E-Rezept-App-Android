/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.redeem.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.ui.LocalProfileHandler
import de.gematik.ti.erp.app.redeem.usecase.RedeemUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import org.kodein.di.compose.rememberInstance

class RedeemController(
    scope: CoroutineScope,
    val profileId: ProfileIdentifier,
    private val useCase: RedeemUseCase
) {
    private val hasRedeemableTasksFlow =
        useCase
            .hasRedeemablePrescriptions(profileId)
            .shareIn(scope, SharingStarted.Lazily, 1)

    val hasRedeemableTasks
        @Composable
        get() = hasRedeemableTasksFlow.collectAsState(false)

    suspend fun redeemScannedTasks(taskIds: List<String>) {
        useCase.redeemScannedTasks(taskIds)
    }
}

@Composable
fun rememberRedeemController(): RedeemController {
    val activeProfile = LocalProfileHandler.current.activeProfile
    val useCase by rememberInstance<RedeemUseCase>()
    val scope = rememberCoroutineScope()
    return remember(activeProfile.id) {
        RedeemController(
            scope,
            activeProfile.id,
            useCase
        )
    }
}
