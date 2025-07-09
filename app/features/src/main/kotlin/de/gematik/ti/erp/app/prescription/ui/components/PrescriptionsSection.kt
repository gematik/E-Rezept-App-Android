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

package de.gematik.ti.erp.app.prescription.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.mainscreen.model.ProfileIconState
import de.gematik.ti.erp.app.mainscreen.model.ProfileIconState.IsError.rememberProfileIconState
import de.gematik.ti.erp.app.mainscreen.model.ProfileLifecycleState
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.uistate.UiState

@Composable
internal fun PrescriptionsSection(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    profileLifecycleState: ProfileLifecycleState,
    activeProfile: UiState<ProfilesUseCaseData.Profile>,
    activePrescriptions: UiState<List<Prescription>>,
    isArchiveEmpty: Boolean,
    onElevateTopBar: (Boolean) -> Unit,
    onClickPrescription: (String, Boolean, Boolean) -> Unit,
    onClickLogin: () -> Unit,
    onClickRefresh: () -> Unit,
    onClickAvatar: () -> Unit,
    onClickArchive: () -> Unit
) {
    LaunchedEffect(Unit) {
        snapshotFlow {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }.collect {
            onElevateTopBar(it)
        }
    }

    val profileIconState: ProfileIconState by rememberProfileIconState(
        profileLifecycleState = profileLifecycleState,
        activeProfile = activeProfile
    )

    val isRegistered by profileLifecycleState.isRegistered.collectAsStateWithLifecycle()

    UiStateMachine(
        state = activePrescriptions,
        onLoading = {
            Center {
                CircularProgressIndicator()
            }
        },
        onError = {
            ErrorScreenComponent(
                onClickRetry = onClickLogin
            )
        },
        onEmpty = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(TestTag.Prescriptions.Content),
                state = listState,
                contentPadding = PaddingValues(bottom = SizeDefaults.eightfoldAndThreeQuarter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                emptyContentSection(
                    activeProfile = activeProfile,
                    profileIconState = profileIconState,
                    isRegistered = isRegistered,
                    onClickConnect = onClickLogin,
                    onClickAvatar = onClickAvatar
                )
                archiveSection(
                    isArchiveEmpty = isArchiveEmpty,
                    onClickArchive = onClickArchive
                )
            }
        },
        onContent = { prescriptions ->
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .testTag(TestTag.Prescriptions.Content),
                state = listState,
                contentPadding = PaddingValues(bottom = SizeDefaults.eightfoldAndThreeQuarter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                profileConnectorSection(
                    activeProfile = activeProfile,
                    profileIconState = profileIconState,
                    isRegistered = isRegistered,
                    onClickAvatar = onClickAvatar,
                    onClickLogin = onClickLogin,
                    onClickRefresh = onClickRefresh
                )
                prescriptionContentSection(
                    activePrescriptions = prescriptions,
                    onClickPrescription = onClickPrescription
                )
                archiveSection(
                    isArchiveEmpty = isArchiveEmpty,
                    onClickArchive = onClickArchive
                )
            }
        }
    )
}
