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

package de.gematik.ti.erp.app.profiles.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.listitem.GemListItemDefaults
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults

@Requirement(
    "O.Auth_6#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Button to display audit events for profile."
)
@Composable
fun ProfileSecuritySection(onClickAuditEvents: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)) {
    }
    Text(
        text = stringResource(R.string.profile_security_section),
        style = AppTheme.typography.h6,
        modifier = Modifier
            .semanticsHeading().padding(horizontal = PaddingDefaults.Medium)
    )
    @Requirement(
        "A_19177#2",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Button to display audit events for profile."
    )
    ListItem(
        colors = GemListItemDefaults.gemListItemColors(),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    onClickAuditEvents()
                }
            )
            .testTag(TestTag.Profile.OpenAuditEventsScreenButton)
            .semantics(mergeDescendants = true) {},
        leadingContent = { Icon(Icons.Outlined.CloudQueue, null, tint = AppTheme.colors.primary700) },
        headlineContent = {
            Text(
                stringResource(
                    R.string.settings_show_audit_events
                ),
                style = AppTheme.typography.body1
            )
        },
        supportingContent = {
            Text(
                stringResource(
                    R.string.settings_show_audit_events_info
                ),
                style = AppTheme.typography.body2l
            )
        }
    )
}
