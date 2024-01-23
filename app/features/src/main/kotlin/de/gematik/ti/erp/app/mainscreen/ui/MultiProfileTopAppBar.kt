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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.mainscreen.presentation.MainScreenController
import de.gematik.ti.erp.app.profiles.presentation.ProfileController
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.TopAppBarWithContent
import kotlinx.coroutines.delay

/**
 * The top appbar of the actual main screen.
 */
@Composable
internal fun MultiProfileTopAppBar(
    mainScreenController: MainScreenController,
    profileController: ProfileController,
    isInPrescriptionScreen: Boolean,
    elevated: Boolean,
    onClickAddProfile: () -> Unit,
    onClickChangeProfileName: (profile: Profile) -> Unit,
    onClickAddPrescription: () -> Unit,
    showToolTipps: Boolean,
    tooltipBounds: MutableState<Map<Int, Rect>>
) {
    val profiles by profileController.getProfilesState()
    val activeProfile by profileController.getActiveProfileState()
    val accScan = stringResource(R.string.main_scan_acc)
    val elevation = remember(elevated) { if (elevated) AppBarDefaults.TopAppBarElevation else 0.dp }

    TopAppBarWithContent(
        title = {
            MainScreenTopBarTitle(isInPrescriptionScreen)
        },
        elevation = elevation,
        backgroundColor = AppTheme.colors.neutral025,
        actions = @Composable {
            if (isInPrescriptionScreen) {
                // data matrix code scanner
                IconButton(
                    onClick = onClickAddPrescription,
                    modifier = Modifier
                        .testTag("erx_btn_scn_prescription")
                        .semantics { contentDescription = accScan }
                        .onGloballyPositioned { coordinates ->
                            if (showToolTipps) {
                                tooltipBounds.value += Pair(0, coordinates.boundsInRoot())
                            }
                        }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddCircle,
                        contentDescription = null,
                        tint = AppTheme.colors.primary700,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        content = {
            ProfilesChipBar(
                mainScreenController = mainScreenController,
                profiles = profiles,
                activeProfile = activeProfile,
                tooltipBounds = tooltipBounds,
                toolTipBoundsRequired = showToolTipps,
                onClickChangeProfileName = onClickChangeProfileName,
                onClickAddProfile = onClickAddProfile,
                onClickChangeActiveProfile = { profile ->
                    profileController.switchActiveProfile(profile.id)
                }
            )
        }
    )
}

@Composable
private fun MainScreenTopBarTitle(isInPrescriptionScreen: Boolean) {
    val text = if (isInPrescriptionScreen) {
        stringResource(R.string.pres_bottombar_prescriptions)
    } else {
        stringResource(R.string.orders_title)
    }
    Text(
        text = text,
        style = AppTheme.typography.h5,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun ProfilesChipBar(
    mainScreenController: MainScreenController,
    profiles: List<Profile>,
    activeProfile: Profile,
    tooltipBounds: MutableState<Map<Int, Rect>>,
    toolTipBoundsRequired: Boolean,
    onClickChangeActiveProfile: (Profile) -> Unit,
    onClickChangeProfileName: (profile: Profile) -> Unit,
    onClickAddProfile: () -> Unit
) {
    val rowState = rememberLazyListState()

    var indexOfActiveProfile by remember { mutableStateOf(0) }

    LaunchedEffect(indexOfActiveProfile) {
        delay(timeMillis = 300L)
        rowState.animateScrollToItem(indexOfActiveProfile)
    }

    LazyRow(
        state = rowState,
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = PaddingDefaults.Medium, bottom = PaddingDefaults.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            SpacerSmall()
        }
        profiles.forEachIndexed { index, profile ->
            if (profile.id == activeProfile.id) {
                indexOfActiveProfile = index + 1
            }

            item {
                ProfileChip(
                    profile = profile,
                    mainScreenController = mainScreenController,
                    selected = profile.id == activeProfile.id,
                    onClickChip = onClickChangeActiveProfile,
                    onClickChangeProfileName = onClickChangeProfileName,
                    tooltipBounds = tooltipBounds,
                    toolTipBoundsRequired = toolTipBoundsRequired
                )
                SpacerSmall()
            }
        }
        item {
            AddProfileChip(
                onClickAddProfile = onClickAddProfile,
                tooltipBounds = tooltipBounds,
                toolTipBoundsRequired = toolTipBoundsRequired
            )
            SpacerMedium()
        }
    }
}
