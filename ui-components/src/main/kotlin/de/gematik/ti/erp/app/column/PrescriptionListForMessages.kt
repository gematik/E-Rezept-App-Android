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
package de.gematik.ti.erp.app.column

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.prescriptionId
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PrescriptionListForMessages(
    modifier: Modifier = Modifier,
    items: List<String>,
    onName: (String) -> String,
    onClick: (String) -> Unit
) {
    Column(
        modifier
            .padding(PaddingDefaults.Medium)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
    ) {
        items.forEach { id ->
            val name = onName(id)
            Surface(
                modifier = Modifier
                    .testTag(TestTag.Orders.Details.PrescriptionListItem)
                    .semantics {
                        prescriptionId = id
                        role = Role.Button
                    },
                shape = RoundedCornerShape(SizeDefaults.one),
                border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral300),
                color = AppTheme.colors.neutral050,
                onClick = { onClick(id) }
            ) {
                Row(
                    Modifier.padding(PaddingDefaults.Medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = AppTheme.typography.subtitle1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    SpacerMedium()
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = AppTheme.colors.neutral400
                    )
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun PrescriptionListForMessagesPreview() {
    PreviewTheme {
        PrescriptionListForMessages(
            items = listOf("1", "2", "3"),
            onName = { "Name $it" },
            onClick = {}
        )
    }
}
