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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.featuretoggle.FeatureToggleManager
import de.gematik.ti.erp.app.featuretoggle.Features
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.ui.AvatarPicker
import de.gematik.ti.erp.app.profiles.ui.ColorPicker
import de.gematik.ti.erp.app.profiles.ui.LocalProfileHandler
import de.gematik.ti.erp.app.profiles.ui.ProfileImage
import de.gematik.ti.erp.app.profiles.ui.ProfileSettingsViewModel
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.ui.SettingsScreen
import de.gematik.ti.erp.app.settings.ui.SettingsViewModel
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.BottomSheetAction
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonLarge
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.sanitizeProfileName
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberViewModel

@Stable
sealed class MainScreenBottomSheetContentState {
    @Stable
    object Redeem : MainScreenBottomSheetContentState()

    @Stable
    object EditProfile : MainScreenBottomSheetContentState()

    @Stable
    class EditOrAddProfileName(
        val addProfile: Boolean = false
    ) : MainScreenBottomSheetContentState()

    @Stable
    object Connect : MainScreenBottomSheetContentState()
}

@Composable
fun MainScreenBottomSheetContentState(
    settingsViewModel: SettingsViewModel,
    profileSettingsViewModel: ProfileSettingsViewModel,
    infoContentState: MainScreenBottomSheetContentState?,
    redeemState: RedeemState,
    mainNavController: NavController,
    profileToRename: ProfilesUseCaseData.Profile,
    onCancel: () -> Unit
) {
    val profileHandler = LocalProfileHandler.current

    val title = when (infoContentState) {
        MainScreenBottomSheetContentState.EditProfile ->
            stringResource(R.string.mainscreen_bottom_sheet_edit_profile_image)
        MainScreenBottomSheetContentState.Connect ->
            stringResource(R.string.mainscreen_welcome_drawer_header)
        is MainScreenBottomSheetContentState.EditOrAddProfileName ->
            stringResource(R.string.bottom_sheet_edit_profile_name_title)
        else -> null
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium)
            .padding(top = PaddingDefaults.Small, bottom = PaddingDefaults.XXLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.DragHandle,
            null,
            tint = AppTheme.colors.neutral600
        )
        SpacerMedium()
        title?.let {
            Text(it, style = AppTheme.typography.subtitle1)
            SpacerMedium()
        }
        Box(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            infoContentState?.let {
                when (it) {
                    MainScreenBottomSheetContentState.Redeem ->
                        RedeemSheetContent(
                            redeemState = redeemState,
                            onClickLocalRedeem = { taskIds ->
                                mainNavController.navigate(
                                    MainNavigationScreens.RedeemLocally.path(
                                        TaskIds(taskIds)
                                    )
                                )
                            },
                            onClickOnlineRedeem = {
                                mainNavController.navigate(
                                    MainNavigationScreens.Pharmacies.path()
                                )
                            }
                        )
                    MainScreenBottomSheetContentState.EditProfile ->
                        EditProfileAvatar(
                            profile = profileHandler.activeProfile,
                            clearPersonalizedImage = {
                                profileSettingsViewModel.clearPersonalizedImage(profileHandler.activeProfile.id)
                            },
                            onPickPersonalizedImage = {
                                mainNavController.navigate(
                                    MainNavigationScreens.ProfileImageCropper.path(
                                        profileId = profileHandler.activeProfile.id
                                    )
                                )
                            },
                            onSelectAvatar = { avatar ->
                                profileSettingsViewModel.saveAvatarFigure(profileHandler.activeProfile.id, avatar)
                            },
                            onSelectProfileColor = { color ->
                                profileSettingsViewModel.updateProfileColor(profileHandler.activeProfile, color)
                            }
                        )
                    is MainScreenBottomSheetContentState.EditOrAddProfileName ->
                        ProfileSheetContent(
                            settingsViewModel = settingsViewModel,
                            profileSettingsViewModel = profileSettingsViewModel,
                            addProfile = it.addProfile,
                            profileToEdit = if (!it.addProfile) {
                                profileToRename
                            } else { null },
                            onCancel = onCancel
                        )
                    MainScreenBottomSheetContentState.Connect ->
                        ConnectBottomSheetContent(onClickConnect = {
                            mainNavController.navigate(
                                MainNavigationScreens.CardWall.path(profileHandler.activeProfile.id)
                            )
                        }, onCancel)
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProfileSheetContent(
    settingsViewModel: SettingsViewModel,
    profileSettingsViewModel: ProfileSettingsViewModel,
    profileToEdit: ProfilesUseCaseData.Profile?,
    addProfile: Boolean = false,
    onCancel: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val settingsScreenState by produceState(SettingsScreen.defaultState) {
        settingsViewModel.screenState().collect {
            value = it
        }
    }
    var textValue by remember { mutableStateOf(profileToEdit?.name ?: "") }
    var duplicated by remember { mutableStateOf(false) }

    val onEdit = {
        if (!addProfile) {
            profileToEdit?.let { profileSettingsViewModel.updateProfileName(it.id, textValue) }
        } else {
            settingsViewModel.addProfile(textValue)
        }
        onCancel()
        keyboardController?.hide()
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            modifier = Modifier.testTag(TestTag.Settings.AddProfileDialog.ProfileNameTextField),
            shape = RoundedCornerShape(8.dp),
            value = textValue,
            singleLine = true,
            onValueChange = {
                val name = sanitizeProfileName(it.trimStart())
                textValue = name
                duplicated = textValue.trim() != profileToEdit?.name &&
                    settingsScreenState.containsProfileWithName(textValue)
            },
            keyboardOptions = KeyboardOptions(
                autoCorrect = true,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions {
                if (!duplicated && textValue.isNotEmpty()) {
                    onEdit()
                }
            },
            placeholder = { Text(stringResource(R.string.profile_edit_name_place_holder)) },
            isError = duplicated
        )

        if (duplicated) {
            Text(
                stringResource(R.string.edit_profile_duplicated_profile_name),
                color = AppTheme.colors.red600,
                style = AppTheme.typography.caption1,
                modifier = Modifier.padding(start = PaddingDefaults.Medium)
            )
        }
        SpacerLarge()
        PrimaryButton(
            modifier = Modifier.testTag(TestTag.Settings.AddProfileDialog.ConfirmButton),
            enabled = !duplicated && textValue.isNotEmpty(),
            onClick = {
                onEdit()
            }
        ) {
            Text(stringResource(R.string.profile_bottom_sheet_save))
        }
    }
}

@Composable
private fun EditProfileAvatar(
    profile: ProfilesUseCaseData.Profile,
    clearPersonalizedImage: () -> Unit,
    onPickPersonalizedImage: () -> Unit,
    onSelectAvatar: (ProfilesData.AvatarFigure) -> Unit,
    onSelectProfileColor: (ProfilesData.ProfileColorNames) -> Unit
) {
    ProfileColorAndImagePickerContent(
        profile,
        clearPersonalizedImage = clearPersonalizedImage,
        onPickPersonalizedImage = onPickPersonalizedImage,
        onSelectAvatar = onSelectAvatar,
        onSelectProfileColor = onSelectProfileColor
    )
}

@Composable
private fun ProfileColorAndImagePickerContent(
    profile: ProfilesUseCaseData.Profile,
    clearPersonalizedImage: () -> Unit,
    onPickPersonalizedImage: () -> Unit,
    onSelectAvatar: (ProfilesData.AvatarFigure) -> Unit,
    onSelectProfileColor: (ProfilesData.ProfileColorNames) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        SpacerMedium()
        ProfileImage(profile) {
            clearPersonalizedImage()
        }

        SpacerXXLarge()
        AvatarPicker(
            profile = profile,
            currentAvatarFigure = profile.avatarFigure,
            onPickPersonalizedImage = onPickPersonalizedImage,
            onSelectAvatar = onSelectAvatar
        )

        if (profile.avatarFigure != ProfilesData.AvatarFigure.PersonalizedImage) {
            SpacerXXLarge()
            SpacerMedium()
            Text(
                stringResource(R.string.edit_profile_background_color),
                style = AppTheme.typography.h6
            )
            SpacerLarge()

            ColorPicker(profile.color, onSelectProfileColor)
            SpacerLarge()
        }
    }
}

@Composable
private fun ConnectBottomSheetContent(onClickConnect: () -> Unit, onCancel: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.mainscreen_welcome_drawer_info),
            style = AppTheme.typography.body2l,
            textAlign = TextAlign.Center
        )
        SpacerLarge()
        PrimaryButtonLarge(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTag.MainScreenBottomSheet.ConnectButton),
            onClick = onClickConnect,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = AppTheme.colors.primary100,
                contentColor = AppTheme.colors.primary700
            )
        ) {
            Text(
                stringResource(R.string.mainscreen_connect_bottomsheet_connect)
            )
        }
        SpacerMedium()
        TextButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxSize()
                .testTag(TestTag.MainScreenBottomSheet.ConnectLaterButton),
            contentPadding = PaddingValues(
                vertical = 13.dp
            )
        ) {
            Text(
                stringResource(R.string.mainscreen_connect_bottomsheet_connect_later)
            )
        }
    }
}

interface RedeemStateBridge {
    fun scannedTasks(profileIdentifier: ProfileIdentifier): Flow<List<ScannedTaskData.ScannedTask>>
    fun syncedTasks(profileIdentifier: ProfileIdentifier): Flow<List<SyncedTaskData.SyncedTask>>
    fun allowRedeemWithoutTiFeatureEnabled(): Flow<Boolean>
}

class RedeemStateViewModel(
    private val prescriptionUseCase: PrescriptionUseCase,
    private val toggleManager: FeatureToggleManager
) : ViewModel(), RedeemStateBridge {

    override fun scannedTasks(profileIdentifier: ProfileIdentifier) =
        prescriptionUseCase.scannedTasks(profileIdentifier)
            .shareIn(viewModelScope, SharingStarted.Eagerly)

    override fun syncedTasks(profileIdentifier: ProfileIdentifier) =
        prescriptionUseCase.syncedTasks(profileIdentifier)
            .shareIn(viewModelScope, SharingStarted.Eagerly)

    override fun allowRedeemWithoutTiFeatureEnabled() =
        toggleManager.isFeatureEnabled(Features.REDEEM_WITHOUT_TI.featureName)
}

@Stable
class RedeemState(
    private val redeemStateBridge: RedeemStateBridge
) {
    @Stable
    private class InternalState(
        val onPremiseRedeemableTaskIds: List<String>,
        val onlineRedeemableTaskIds: List<String>,
        val redeemedMedicationNames: List<String>
    )

    private val timeTrigger = MutableSharedFlow<Unit>()

    private var internalState by mutableStateOf(InternalState(emptyList(), emptyList(), emptyList()))

    val localTaskIds by derivedStateOf { internalState.onPremiseRedeemableTaskIds }

    val onlineTaskIds by derivedStateOf { internalState.onlineRedeemableTaskIds }

    val alreadyRedeemedMedications by derivedStateOf { internalState.redeemedMedicationNames }

    val hasRedeemableTasks by derivedStateOf { onlineTaskIds.isNotEmpty() || localTaskIds.isNotEmpty() }

    suspend fun produceState(profileIdentifier: ProfileIdentifier) = coroutineScope {
        launch {
            while (true) {
                delay(timeMillis = 60_000L)
                timeTrigger.emit(Unit)
            }
        }
        combine(
            redeemStateBridge.allowRedeemWithoutTiFeatureEnabled(),
            redeemStateBridge.scannedTasks(profileIdentifier),
            redeemStateBridge.syncedTasks(profileIdentifier),
            timeTrigger.onStart { emit(Unit) }
        ) { allowWithout, scannedTasks, syncedTasks, _ ->
            val redeemableSyncedTasks = syncedTasks
                .asSequence()
                .filter {
                    it.redeemState().isRedeemable()
                }

            val alreadyRedeemedSyncedTasks = syncedTasks
                .asSequence()
                .filter {
                    it.redeemState() == SyncedTaskData.SyncedTask.RedeemState.RedeemableAfterDelta
                }
                .map {
                    it.medicationRequestMedicationName() ?: ""
                }
                .take(2) // we only require at least two

            val allRedeemableTasks =
                scannedTasks.filter { it.isRedeemable() }.map { it.taskId } + redeemableSyncedTasks.map { it.taskId }

            InternalState(
                onPremiseRedeemableTaskIds = allRedeemableTasks,
                onlineRedeemableTaskIds = if (allowWithout) {
                    allRedeemableTasks
                } else {
                    redeemableSyncedTasks.map { it.taskId }.toList()
                },
                redeemedMedicationNames = alreadyRedeemedSyncedTasks.toList()
            )
        }.collect {
            internalState = it
        }
    }
}

@Composable
fun rememberRedeemState(profile: ProfilesUseCaseData.Profile): RedeemState {
    val redeemStateViewModel by rememberViewModel<RedeemStateViewModel>()
    val state = remember { RedeemState(redeemStateViewModel) }
    LaunchedEffect(profile.id) {
        state.produceState(profile.id)
    }
    return state
}

@Composable
private fun RedeemSheetContent(
    redeemState: RedeemState,
    onClickLocalRedeem: (taskIds: List<String>) -> Unit,
    onClickOnlineRedeem: (taskIds: List<String>) -> Unit
) {
    val onlineRedeemButtonEnabled by derivedStateOf {
        redeemState.onlineTaskIds.isNotEmpty()
    }

    val shouldShowAlreadySentDialog by derivedStateOf {
        redeemState.alreadyRedeemedMedications.isNotEmpty()
    }

    var showAlreadySentDialog by remember { mutableStateOf(false) }

    if (showAlreadySentDialog) {
        SendTasksAgainDialog(
            redeemedMedicationNames = redeemState.alreadyRedeemedMedications,
            onSendAgain = {
                onClickOnlineRedeem(redeemState.onlineTaskIds)
                showAlreadySentDialog = false
            },
            onCancel = {
                showAlreadySentDialog = false
            }
        )
    }

    Column {
        BottomSheetAction(
            icon = Icons.Rounded.QrCode,
            title = stringResource(R.string.dialog_redeem_headline),
            info = stringResource(R.string.dialog_redeem_info),
            modifier = Modifier.testTag("main/redeemInLocalPharmacyButton")
        ) {
            onClickLocalRedeem(redeemState.localTaskIds)
        }

        BottomSheetAction(
            enabled = onlineRedeemButtonEnabled,
            icon = Icons.Rounded.ShoppingBag,
            title = stringResource(R.string.dialog_order_headline),
            info = stringResource(R.string.dialog_order_info),
            modifier = Modifier.testTag("main/redeemRemoteButton")
        ) {
            if (shouldShowAlreadySentDialog) {
                showAlreadySentDialog = true
            } else {
                onClickOnlineRedeem(redeemState.onlineTaskIds)
            }
        }

        Box(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun SendTasksAgainDialog(
    redeemedMedicationNames: List<String>,
    onSendAgain: () -> Unit,
    onCancel: () -> Unit
) {
    val medication = remember(redeemedMedicationNames) { redeemedMedicationNames.first() }

    val taskAlreadySentInfo = buildAnnotatedString {
        append(
            annotatedPluralsResource(
                R.plurals.task_already_sent_info,
                redeemedMedicationNames.size,
                AnnotatedString(medication)
            )
        )
        append("\n\n")
        append(stringResource(R.string.task_already_sent_sub_info))
    }

    CommonAlertDialog(
        header = AnnotatedString(stringResource(R.string.task_already_sent_header)),
        info = taskAlreadySentInfo,
        cancelText = stringResource(R.string.cancel_sent_task_again),
        actionText = stringResource(R.string.sent_task_again),
        onCancel = onCancel,
        onClickAction = onSendAgain
    )
}
