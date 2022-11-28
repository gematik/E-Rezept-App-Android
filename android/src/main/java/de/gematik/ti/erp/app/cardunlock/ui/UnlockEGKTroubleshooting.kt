/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.cardunlock.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.cardwall.ui.TroubleshootingNoSuccessPageContent
import de.gematik.ti.erp.app.cardwall.ui.TroubleshootingPageAContent
import de.gematik.ti.erp.app.cardwall.ui.TroubleshootingPageBContent
import de.gematik.ti.erp.app.cardwall.ui.TroubleshootingPageCContent
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
@Composable
fun UnlockEGKTroubleshootingPageA(
    viewModel: UnlockEgkViewModel,
    cardAccessNumber: String,
    personalUnblockingKey: String,
    unlockMethod: UnlockMethod,
    oldSecret: String,
    newSecret: String,
    onRetryOldSecret: () -> Unit,
    onRetryCan: () -> Unit,
    onRetryPuk: () -> Unit,
    onFinishUnlock: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onAssignPin: () -> Unit
) {
    val dialogState = rememberUnlockEgkDialogState()
    UnlockEgkDialog(
        dialogState = dialogState,
        viewModel = viewModel,
        unlockMethod = unlockMethod,
        cardAccessNumber = cardAccessNumber,
        personalUnblockingKey = personalUnblockingKey,
        oldSecret = oldSecret,
        newSecret = newSecret,
        onRetryOldSecret = onRetryOldSecret,
        onRetryCan = onRetryCan,
        onRetryPuk = onRetryPuk,
        onFinishUnlock = onFinishUnlock,
        onAssignPin = onAssignPin
    )
    val coroutineScope = rememberCoroutineScope()
    TroubleshootingPageAContent(
        onBack = onBack,
        onNext = onNext,
        onClickTryMe = {
            coroutineScope.launch { dialogState.show() }
        }
    )
}

@Suppress("LongParameterList")
@Composable
fun UnlockEGKTroubleshootingPageB(
    viewModel: UnlockEgkViewModel,
    unlockMethod: UnlockMethod,
    cardAccessNumber: String,
    personalUnblockingKey: String,
    oldSecret: String,
    newSecret: String,
    onRetryCan: () -> Unit,
    onRetryOldSecret: () -> Unit,
    onRetryPuk: () -> Unit,
    onFinishUnlock: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onAssignPin: () -> Unit
) {
    val dialogState = rememberUnlockEgkDialogState()
    UnlockEgkDialog(
        dialogState = dialogState,
        viewModel = viewModel,
        unlockMethod = unlockMethod,
        cardAccessNumber = cardAccessNumber,
        personalUnblockingKey = personalUnblockingKey,
        oldSecret = oldSecret,
        newSecret = newSecret,
        onRetryCan = onRetryCan,
        onRetryOldSecret = onRetryOldSecret,
        onRetryPuk = onRetryPuk,
        onFinishUnlock = onFinishUnlock,
        onAssignPin = onAssignPin
    )

    val coroutineScope = rememberCoroutineScope()
    TroubleshootingPageBContent(
        onBack,
        onNext,
        onClickTryMe = {
            coroutineScope.launch { dialogState.show() }
        }
    )
}

@Suppress("LongParameterList")
@Composable
fun UnlockEGKTroubleshootingPageC(
    viewModel: UnlockEgkViewModel,
    unlockMethod: UnlockMethod,
    cardAccessNumber: String,
    personalUnblockingKey: String,
    oldSecret: String,
    newSecret: String,
    onRetryCan: () -> Unit,
    onRetryOldSecret: () -> Unit,
    onRetryPuk: () -> Unit,
    onFinishUnlock: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onAssignPin: () -> Unit
) {
    val dialogState = rememberUnlockEgkDialogState()
    UnlockEgkDialog(
        dialogState = dialogState,
        viewModel = viewModel,
        unlockMethod = unlockMethod,
        cardAccessNumber = cardAccessNumber,
        personalUnblockingKey = personalUnblockingKey,
        oldSecret = oldSecret,
        newSecret = newSecret,
        onRetryCan = onRetryCan,
        onRetryOldSecret = onRetryOldSecret,
        onRetryPuk = onRetryPuk,
        onFinishUnlock = onFinishUnlock,
        onAssignPin = onAssignPin
    )

    val coroutineScope = rememberCoroutineScope()
    TroubleshootingPageCContent(
        onBack,
        onNext,
        onClickTryMe = {
            coroutineScope.launch { dialogState.show() }
        }
    )
}

@Composable
fun UnlockEGKTroubleshootingNoSuccessPage(
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    TroubleshootingNoSuccessPageContent(
        onNext,
        onBack
    )
}
