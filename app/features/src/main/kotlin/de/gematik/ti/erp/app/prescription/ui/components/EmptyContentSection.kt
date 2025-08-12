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

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.mainscreen.model.ProfileIconState
import de.gematik.ti.erp.app.prescription.ui.MainScreenAvatar
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile.Companion.ProfileConnectionState
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile.Companion.connectionState
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.TertiaryButton
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.uistate.UiState

fun LazyListScope.emptyContentSection(
    activeProfile: UiState<ProfilesUseCaseData.Profile>,
    profileIconState: ProfileIconState,
    onClickConnect: () -> Unit,
    onClickAvatar: () -> Unit,
    isRegistered: Boolean
) {
    item {
        Spacer(modifier = Modifier.size(SizeDefaults.tenfold))
        UiStateMachine(activeProfile) { profile ->
            MainScreenAvatar(
                activeProfile = profile,
                profileIconState = profileIconState,
                onClickAvatar = onClickAvatar,
                isRegistered = isRegistered
            )
        }
    }
    if (activeProfile.data?.connectionState() != ProfileConnectionState.LoggedIn) {
        item {
            SpacerMedium()
            TertiaryButton(onClickConnect, modifier = Modifier.testTag(TestTag.Main.LoginButton)) {
                Text(stringResource(R.string.mainscreen_login))
            }
        }
        item {
            SpacerLarge()
            Text(
                stringResource(R.string.mainscreen_empty_content_header),
                style = AppTheme.typography.subtitle1,
                modifier = Modifier.testTag(TestTag.Main.CenterScreenMessageField)
            )
        }
        item {
            SpacerSmall()
            Text(
                stringResource(R.string.mainscreen_empty_not_connected_info),
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                style = AppTheme.typography.body2,
                textAlign = TextAlign.Center
            )
        }
    } else {
        item {
            SpacerLarge()
            Text(
                stringResource(R.string.mainscreen_empty_content_header),
                style = AppTheme.typography.subtitle1
            )
        }
        item {
            SpacerMedium()
            Text(
                stringResource(R.string.mainscreen_empty_connected_info),
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                style = AppTheme.typography.body2,
                textAlign = TextAlign.Center
            )
            SpacerMedium()
            Icon(Icons.Rounded.ArrowDownward, null)
        }
    }
}
