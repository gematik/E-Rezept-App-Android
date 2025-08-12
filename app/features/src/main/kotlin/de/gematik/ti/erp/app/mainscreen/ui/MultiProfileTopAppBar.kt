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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.animated.AnimationTime
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.mainscreen.model.MultiProfileAppBarWrapper
import de.gematik.ti.erp.app.mainscreen.model.ProfileIconState.IsError.rememberProfileIconState
import de.gematik.ti.erp.app.mainscreen.ui.components.AddProfileChip
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.TopAppBarWithContent
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.delay

@Composable
internal fun MultiProfileTopAppBar(
    multiProfileData: MultiProfileAppBarWrapper,
    elevated: Boolean,
    onClickAddPrescription: () -> Unit,
    onClickChangeProfileName: (profile: Profile) -> Unit,
    onClickAddProfile: () -> Unit,
    switchActiveProfile: (Profile) -> Unit
) {
    val accScan = stringResource(R.string.main_scan_acc)
    val elevation = remember(elevated) { if (elevated) AppBarDefaults.TopAppBarElevation else SizeDefaults.zero }

    TopAppBarWithContent(
        title = {
            MainScreenTopBarTitle()
        },
        elevation = elevation,
        backgroundColor = AppTheme.colors.neutral025,
        actions = @Composable {
            // data matrix code scanner
            IconButton(
                onClick = onClickAddPrescription,
                modifier = Modifier
                    .testTag("erx_btn_scn_prescription")
                    .semantics { contentDescription = accScan }
            ) {
                Icon(
                    imageVector = Icons.Rounded.AddCircle,
                    contentDescription = null,
                    tint = AppTheme.colors.primary700,
                    modifier = Modifier.size(SizeDefaults.triple)
                )
            }
        },
        content = {
            ProfilesChipBar(
                multiProfileData = multiProfileData,
                onClickChangeProfileName = onClickChangeProfileName,
                onClickAddProfile = onClickAddProfile,
                onClickChangeActiveProfile = switchActiveProfile
            )
        }
    )
}

@Composable
private fun MainScreenTopBarTitle() {
    Text(
        text = stringResource(R.string.pres_bottombar_prescriptions),
        style = AppTheme.typography.h5,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun ProfilesChipBar(
    multiProfileData: MultiProfileAppBarWrapper,
    onClickChangeActiveProfile: (Profile) -> Unit,
    onClickChangeProfileName: (profile: Profile) -> Unit,
    onClickAddProfile: () -> Unit
) {
    val rowState = rememberLazyListState()

    // flows are collected inside the composable to avoid recomposition when the states change
    val profiles by multiProfileData.existingProfiles.collectAsStateWithLifecycle()

    val activeProfile by multiProfileData.activeProfile.collectAsStateWithLifecycle()

    val profileIconState by rememberProfileIconState(multiProfileData.profileLifecycleState, UiState.Data(activeProfile))

    val indexOfActiveProfile by remember(multiProfileData.existingProfiles, multiProfileData.activeProfile) {
        mutableIntStateOf(profiles.indexOfFirst { it.id == activeProfile.id }.plus(1))
    }

    LaunchedEffect(indexOfActiveProfile) {
        delay(timeMillis = AnimationTime.SHORT_L)
        rowState.animateScrollToItem(indexOfActiveProfile)
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = PaddingDefaults.Medium, bottom = PaddingDefaults.Small),
        state = rowState,
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            SpacerSmall()
        }
        items(profiles) { profile ->
            ProfileChip(
                profile = profile,
                profileIconState = profileIconState,
                selected = profile.id == activeProfile.id,
                onClickChangeProfileName = onClickChangeProfileName,
                onClickChip = onClickChangeActiveProfile
            )
            SpacerSmall()
        }
        item {
            AddProfileChip(
                onClickAddProfile = onClickAddProfile
            )
            SpacerMedium()
        }
    }
}
