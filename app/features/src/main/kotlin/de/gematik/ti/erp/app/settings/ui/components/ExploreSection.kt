/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.settings.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.settings.model.ExploreClickActions
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.LabelButton
import de.gematik.ti.erp.app.utils.compose.LabeledSwitch
import de.gematik.ti.erp.app.utils.extensions.sectionPadding

@Composable
fun ExploreSection(
    isDemoMode: Boolean,
    exploreClickActions: ExploreClickActions
) {
    Column {
        Text(
            text = stringResource(R.string.settings_explore_headline),
            style = AppTheme.typography.h6,
            modifier = Modifier.sectionPadding().semanticsHeading()
        )
        DemoModeSwitch(isDemoMode = isDemoMode) {
            exploreClickActions.onToggleDemoMode()
        }
        LabelButton(
            icon = Icons.Rounded.VolunteerActivism,
            text = stringResource(R.string.organ_donation_menu_entry),
            onClick = { exploreClickActions.onClickOrganDonationRegister() }
        )
        LabelButton(
            icon = Icons.Outlined.People,
            text = stringResource(R.string.settings_contact_community_label),
            onClick = {
                exploreClickActions.onClickForum()
            }
        )
        LabelButton(
            icon = Icons.Outlined.Info,
            text = stringResource(id = R.string.gesund_bund_de)
        ) {
            exploreClickActions.onClickGesundBund()
        }
    }
}

@Composable
private fun DemoModeSwitch(
    modifier: Modifier = Modifier,
    isDemoMode: Boolean,
    onToggleDemoMode: (Boolean) -> Unit
) {
    LabeledSwitch(
        modifier = modifier,
        checked = isDemoMode,
        onCheckedChange = onToggleDemoMode,
        icon = Icons.Outlined.AutoFixHigh,
        header = stringResource(R.string.demo_mode_settings_title)
    )
}
