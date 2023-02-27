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

package de.gematik.ti.erp.app.cardunlock.ui

import android.nfc.Tag
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.cardunlock.model.UnlockEgkNavigation
import de.gematik.ti.erp.app.cardwall.ui.CardAccessNumber
import de.gematik.ti.erp.app.cardwall.ui.CardHandlingScaffold
import de.gematik.ti.erp.app.cardwall.ui.ConformationSecretInputField
import de.gematik.ti.erp.app.cardwall.ui.NFCInstructionScreen
import de.gematik.ti.erp.app.cardwall.ui.SecretInputField
import de.gematik.ti.erp.app.cardwall.ui.rememberCardWallNfcPositionState
import de.gematik.ti.erp.app.troubleShooting.TroubleShootingScreen
import de.gematik.ti.erp.app.pharmacy.ui.scrollOnFocus
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationMode
import de.gematik.ti.erp.app.utils.compose.SimpleCheck
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import org.kodein.di.compose.rememberViewModel

const val SECRET_MIN_LENGTH = 6
const val SECRET_MAX_LENGTH = 8
const val ConfInputfieldPosition = 3

sealed class ToggleUnlock {
    data class ToggleByUser(val value: Boolean) : ToggleUnlock()
    data class ToggleByHealthCard(val tag: Tag) : ToggleUnlock()
}

@Composable
fun UnlockEgKScreen(
    unlockMethod: UnlockMethod,
    onCancel: () -> Unit,
    onClickLearnMore: () -> Unit
) {
    val viewModel by rememberViewModel<UnlockEgkViewModel>()
    var unlockMethod by rememberSaveable { mutableStateOf(unlockMethod) }

    val unlockNavController = rememberNavController()
    var cardAccessNumber by rememberSaveable { mutableStateOf("") }
    var personalUnblockingKey by rememberSaveable { mutableStateOf("") }
    var oldSecret by rememberSaveable { mutableStateOf("") }
    var newSecret by rememberSaveable { mutableStateOf("") }

    val onRetryCan = onRetryCan(unlockNavController)
    val onRetryOldSecret = onRetryOldSecret(unlockNavController)
    val onRetryPuk = onRetryPuk(unlockNavController)

    val onAssignPin = {
        unlockMethod = UnlockMethod.ChangeReferenceData
        unlockNavController.navigate(UnlockEgkNavigation.Intro.path()) {
            popUpTo(UnlockEgkNavigation.Intro.path()) { inclusive = true }
        }
    }

    NavHost(
        unlockNavController,
        startDestination = UnlockEgkNavigation.Intro.path()
    ) {
        composable(UnlockEgkNavigation.Intro.route) {
            NavigationAnimation {
                IntroScreen(unlockMethod = unlockMethod) {
                    unlockNavController.navigate(UnlockEgkNavigation.CardAccessNumber.path())
                }
            }
        }
        composable(UnlockEgkNavigation.CardAccessNumber.route) {
            NavigationAnimation {
                CardAccessNumberScreen(
                    unlockMethod = unlockMethod,
                    cardAccessNumber = cardAccessNumber,
                    onCanChanged = { cardAccessNumber = it },
                    onClickLearnMore = { onClickLearnMore() },
                    onCancel = onCancel
                ) {
                    if (unlockMethod == UnlockMethod.ChangeReferenceData) {
                        unlockNavController.navigate(UnlockEgkNavigation.OldSecret.path())
                    } else {
                        unlockNavController.navigate(UnlockEgkNavigation.PersonalUnblockingKey.path())
                    }
                }
            }
        }
        composable(UnlockEgkNavigation.PersonalUnblockingKey.route) {
            NavigationAnimation {
                PersonalUnblockingKeyScreen(
                    personalUnblockingKey = personalUnblockingKey,
                    unlockMethod = unlockMethod,
                    onPersonalUnblockingKeyChanged = { personalUnblockingKey = it },
                    onCancel = onCancel
                ) {
                    if (unlockMethod == UnlockMethod.ResetRetryCounterWithNewSecret) {
                        unlockNavController.navigate(UnlockEgkNavigation.NewSecret.path())
                    } else {
                        unlockNavController.navigate(UnlockEgkNavigation.UnlockEgk.path())
                    }
                }
            }
        }
        composable(UnlockEgkNavigation.OldSecret.route) {
            NavigationAnimation {
                OldSecretScreen(
                    oldSecret = oldSecret,
                    onSecretChange = { oldSecret = it },
                    onCancel = onCancel
                ) {
                    unlockNavController.navigate(UnlockEgkNavigation.NewSecret.path())
                }
            }
        }
        composable(UnlockEgkNavigation.NewSecret.route) {
            NavigationAnimation {
                NewSecretScreen(
                    newSecret = newSecret,
                    onSecretChange = { newSecret = it },
                    onCancel = onCancel
                ) {
                    unlockNavController.navigate(UnlockEgkNavigation.UnlockEgk.path())
                }
            }
        }
        composable(UnlockEgkNavigation.UnlockEgk.route) {
            NavigationAnimation {
                UnlockScreen(
                    unlockMethod = unlockMethod,
                    viewModel = viewModel,
                    cardAccessNumber = cardAccessNumber,
                    personalUnblockingKey = personalUnblockingKey,
                    oldSecret = oldSecret,
                    newSecret = newSecret,
                    onBack = { unlockNavController.popBackStack() },
                    onClickTroubleshooting = {
                        unlockNavController.navigate(UnlockEgkNavigation.TroubleShooting.path())
                    },
                    onRetryCan = onRetryCan,
                    onRetryOldSecret = onRetryOldSecret,
                    onRetryPuk = onRetryPuk,
                    onFinishUnlock = onCancel,
                    onAssignPin = onAssignPin
                )
            }
        }
        composable(UnlockEgkNavigation.TroubleShooting.route) {
            NavigationAnimation {
                TroubleShootingScreen(
                    onClickTryMe = {
                        unlockNavController.navigate(UnlockEgkNavigation.UnlockEgk.path())
                    },
                    onCancel = { unlockNavController.popBackStack() }
                )
            }
        }
    }
}

private fun onRetryPuk(unlockNavController: NavHostController): () -> Unit = {
    unlockNavController.navigate(UnlockEgkNavigation.PersonalUnblockingKey.path()) {
        popUpTo(UnlockEgkNavigation.PersonalUnblockingKey.path()) { inclusive = true }
    }
}

private fun onRetryOldSecret(unlockNavController: NavHostController): () -> Unit = {
    unlockNavController.navigate(UnlockEgkNavigation.OldSecret.path()) {
        popUpTo(UnlockEgkNavigation.OldSecret.path()) { inclusive = true }
    }
}

private fun onRetryCan(unlockNavController: NavHostController): () -> Unit = {
    unlockNavController.navigate(UnlockEgkNavigation.CardAccessNumber.path()) {
        popUpTo(UnlockEgkNavigation.CardAccessNumber.path()) { inclusive = true }
    }
}

@Composable
private fun IntroScreen(unlockMethod: UnlockMethod, onNext: () -> Unit) {
    val lazyListState = rememberLazyListState()
    CardHandlingScaffold(
        modifier = Modifier.testTag("unlockEgk/unlock"),
        title = when (unlockMethod) {
            UnlockMethod.ChangeReferenceData -> {
                stringResource(R.string.unlock_egk_top_bar_title_change_secret)
            }
            UnlockMethod.ResetRetryCounterWithNewSecret -> {
                stringResource(R.string.unlock_egk_top_bar_title_forgot_pin)
            }
            else -> {
                stringResource(R.string.unlock_egk_top_bar_title)
            }
        },
        onNext = onNext,
        nextText = stringResource(R.string.unlock_egk_next),
        listState = lazyListState
    ) {
        UnlockIntroContent(unlockMethod, lazyListState = lazyListState)
    }
}

@Composable
private fun UnlockIntroContent(
    unlockMethod: UnlockMethod,
    lazyListState: LazyListState
) {
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.padding(PaddingDefaults.Medium)
    ) {
        item {
            Text(
                text = stringResource(R.string.unlock_egk_intro_what_you_need),
                style = AppTheme.typography.h5
            )
            SpacerLarge()
            SimpleCheck(stringResource(R.string.unlock_egk_intro_egk))
            if (unlockMethod == UnlockMethod.ChangeReferenceData) {
                SimpleCheck(stringResource(R.string.unlock_egk_intro_pin))
                SpacerSmall()
                Text(
                    text = stringResource(R.string.cdw_pin_info),
                    style = AppTheme.typography.caption1l
                )
            } else {
                SimpleCheck(stringResource(R.string.unlock_egk_intro_puk))
                SpacerSmall()
                Text(
                    text = stringResource(R.string.unlock_egk_puk_info),
                    style = AppTheme.typography.caption1l
                )
            }
        }
    }
}

@Composable
private fun CardAccessNumberScreen(
    unlockMethod: UnlockMethod,
    cardAccessNumber: String,
    onCanChanged: (String) -> Unit,
    onClickLearnMore: () -> Unit,
    onCancel: () -> Unit,
    onNext: () -> Unit
) {
    CardAccessNumber(
        onClickLearnMore = onClickLearnMore,
        can = cardAccessNumber,
        screenTitle = when (unlockMethod) {
            UnlockMethod.ChangeReferenceData -> {
                stringResource(R.string.unlock_egk_top_bar_title_change_secret)
            }
            UnlockMethod.ResetRetryCounterWithNewSecret -> {
                stringResource(R.string.unlock_egk_top_bar_title_forgot_pin)
            }
            else -> {
                stringResource(R.string.unlock_egk_top_bar_title)
            }
        },
        onCanChange = onCanChanged,
        onNext = onNext,
        nextText = stringResource(R.string.unlock_egk_next),
        onCancel = { onCancel() }
    )
}

private val PUKLengthRange = 8..8

@Composable
private fun PersonalUnblockingKeyScreen(
    personalUnblockingKey: String,
    unlockMethod: UnlockMethod,
    onPersonalUnblockingKeyChanged: (String) -> Unit,
    onCancel: () -> Unit,
    onNext: (String) -> Unit
) {
    PukScreen(
        navMode = NavigationMode.Back,
        secret = personalUnblockingKey,
        unlockMethod = unlockMethod,
        secretRange = PUKLengthRange,
        onSecretChange = onPersonalUnblockingKeyChanged,
        onCancel = onCancel,
        next = onNext,
        nextText = stringResource(R.string.unlock_egk_next)
    )
}

@Composable
private fun PukScreen(
    unlockMethod: UnlockMethod,
    navMode: NavigationMode,
    secret: String,
    secretRange: IntRange,
    onSecretChange: (String) -> Unit,
    onCancel: () -> Unit,
    next: (String) -> Unit,
    nextText: String
) {
    val listState = rememberLazyListState()
    CardHandlingScaffold(
        modifier = Modifier.testTag("cardWall/secretScreen"),
        backMode = when (navMode) {
            NavigationMode.Forward,
            NavigationMode.Back,
            NavigationMode.Closed -> NavigationBarMode.Back
            NavigationMode.Open -> NavigationBarMode.Close
        },
        title = if (unlockMethod == UnlockMethod.ResetRetryCounterWithNewSecret) {
            stringResource(R.string.unlock_egk_top_bar_title_forgot_pin)
        } else {
            stringResource(R.string.unlock_egk_top_bar_title)
        },
        listState = listState,
        nextEnabled = secret.length in secretRange,
        onNext = { next(secret) },
        nextText = nextText,
        actions = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(PaddingDefaults.Medium),
            state = listState
        ) {
            item {
                Text(
                    stringResource(R.string.unlock_egk_enter_puk),
                    style = AppTheme.typography.h5
                )
                SpacerMedium()
            }
            item {
                SecretInputField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scrollOnFocus(2, listState),
                    secretRange = secretRange,
                    onSecretChange = onSecretChange,
                    secret = secret,
                    label = stringResource(R.string.unlock_egk_puk_label),
                    next = next
                )
                SpacerTiny()
                Text(
                    stringResource(R.string.unlock_egk_puk_info),
                    style = AppTheme.typography.caption1l
                )
            }
        }
    }
}

@Composable
private fun OldSecretScreen(
    oldSecret: String,
    onSecretChange: (String) -> Unit,
    onCancel: () -> Unit,
    onNext: (String) -> Unit
) {
    val secretRange = SECRET_MIN_LENGTH..SECRET_MAX_LENGTH

    val lazyListState = rememberLazyListState()
    CardHandlingScaffold(
        modifier = Modifier.testTag("cardWall/secretScreen"),
        backMode = NavigationBarMode.Back,
        title = stringResource(R.string.unlock_egk_top_bar_title_change_secret),
        nextEnabled = oldSecret.length in secretRange,
        listState = lazyListState,
        onNext = { onNext(oldSecret) },
        nextText = stringResource(R.string.unlock_egk_next),
        actions = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(PaddingDefaults.Medium)
        ) {
            item {
                Text(
                    stringResource(R.string.unlock_egk_enter_old_secret),
                    style = AppTheme.typography.h5
                )
                SpacerMedium()
            }
            item {
                SecretInputField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scrollOnFocus(2, lazyListState),
                    secretRange = secretRange,
                    onSecretChange = onSecretChange,
                    secret = oldSecret,
                    label = stringResource(R.string.unlock_egk_choose_new_secret_label),
                    next = onNext
                )
                SpacerTiny()
                Text(
                    stringResource(R.string.unlock_egk_pin_info),
                    style = AppTheme.typography.caption1l
                )
            }
        }
    }
}

@Composable
private fun NewSecretScreen(
    newSecret: String,
    onSecretChange: (String) -> Unit,
    onCancel: () -> Unit,
    onNext: (String) -> Unit
) {
    val secretRange = SECRET_MIN_LENGTH..SECRET_MAX_LENGTH
    var repeatedNewSecret by remember { mutableStateOf("") }
    val isConsistent by remember {
        derivedStateOf {
            repeatedNewSecret.isNotBlank() && newSecret == repeatedNewSecret
        }
    }

    val lazyListState = rememberLazyListState()
    CardHandlingScaffold(
        modifier = Modifier.testTag("cardWall/secretScreen"),
        backMode = NavigationBarMode.Back,
        title = stringResource(R.string.unlock_egk_top_bar_title_change_secret),
        nextEnabled = newSecret.length in secretRange && isConsistent,
        listState = lazyListState,
        onNext = { onNext(newSecret) },
        nextText = stringResource(R.string.unlock_egk_next),
        actions = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(PaddingDefaults.Medium)
        ) {
            item {
                Text(
                    stringResource(R.string.unlock_egk_new_secret_title),
                    style = AppTheme.typography.h5
                )
                SpacerMedium()
            }
            item {
                SecretInputField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scrollOnFocus(2, lazyListState),
                    secretRange = secretRange,
                    onSecretChange = onSecretChange,
                    isConsistent = isConsistent,
                    secret = newSecret,
                    label = stringResource(R.string.unlock_egk_choose_new_secret_label),
                    next = onNext
                )
                SpacerTiny()
                Text(
                    stringResource(R.string.unlock_egk_new_secret_info),
                    style = AppTheme.typography.caption1l
                )
            }

            item {
                SpacerLarge()
                ConformationSecretInputField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scrollOnFocus(ConfInputfieldPosition, lazyListState),
                    secretRange = secretRange,
                    repeatedSecret = repeatedNewSecret,
                    onSecretChange = { repeatedNewSecret = it },
                    secret = newSecret,
                    isConsistent = isConsistent,
                    label = stringResource(R.string.unlock_egk_repeat_secret_label),
                    next = onNext
                )
                if (repeatedNewSecret.isNotBlank() && !isConsistent) {
                    SpacerTiny()
                    Text(
                        stringResource(R.string.not_matching_entries),
                        style = AppTheme.typography.caption1,
                        color = AppTheme.colors.red600.copy(
                            alpha = ContentAlpha.high
                        )
                    )
                }
            }
            item {
                SpacerXXLarge()
                HintCard(
                    modifier = Modifier,
                    image = {
                        HintSmallImage(
                            painterResource(R.drawable.information),
                            innerPadding = it
                        )
                    },
                    title = { Text(stringResource(R.string.unlock_egk_new_secret_extra_content_title)) },
                    body = { Text(stringResource(R.string.unlock_egk_new_secret_extra_content_info)) }
                )
                SpacerMedium()
            }
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun UnlockScreen(
    unlockMethod: UnlockMethod,
    viewModel: UnlockEgkViewModel,
    cardAccessNumber: String,
    personalUnblockingKey: String,
    oldSecret: String,
    newSecret: String,
    onClickTroubleshooting: () -> Unit,
    onRetryCan: () -> Unit,
    onRetryOldSecret: () -> Unit,
    onRetryPuk: () -> Unit,
    onBack: () -> Unit,
    onFinishUnlock: () -> Unit,
    onAssignPin: () -> Unit
) {
    val nfcPositionState = rememberCardWallNfcPositionState()
    val state = nfcPositionState.state
    val dialogState = rememberUnlockEgkDialogState()

    UnlockEgkDialog(
        unlockMethod = unlockMethod,
        dialogState = dialogState,
        viewModel = viewModel,
        cardAccessNumber = cardAccessNumber,
        personalUnblockingKey = personalUnblockingKey,
        troubleShootingEnabled = true,
        onClickTroubleshooting = onClickTroubleshooting,
        oldSecret = oldSecret,
        newSecret = newSecret,
        onRetryCan = onRetryCan,
        onRetryOldSecret = onRetryOldSecret,
        onRetryPuk = onRetryPuk,
        onFinishUnlock = onFinishUnlock,
        onAssignPin = onAssignPin
    )

    NFCInstructionScreen(
        onBack = onBack,
        onClickTroubleshooting = onClickTroubleshooting,
        state = state
    )
}
