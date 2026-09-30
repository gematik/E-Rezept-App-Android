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

package de.gematik.ti.erp.app.messages.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Public
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.SpacerMedium

// EU
// CommResV3 design: https://www.figma.com/design/Xg4X8ULc7fwxzkxaCnxW0I/%F0%9F%A4%96-eRezept-Android?node-id=41435-3389&m=dev
@Composable
internal fun EuAccessCodeCreatedMessageCard(
    time: String,
    title: String,
    description: String,
    onClickShowCode: () -> Unit,
    onClickRevokeAccess: () -> Unit
) {
    EuAccessCodeMessageCard(
        time = time,
        title = title,
        description = description,
        primaryActionLabel = stringResource(R.string.eu_messages_show_code_button_text),
        secondaryActionLabel = stringResource(R.string.eu_messages_revoke_code_button_text),
        onClickPrimaryAction = onClickShowCode,
        onClickSecondaryAction = onClickRevokeAccess
    )
}

@Composable
internal fun EuAccessCodeRevokedMessageCard(
    time: String,
    title: String,
    description: String
) {
    EuAccessCodeMessageCard(
        time = time,
        title = title,
        description = description,
        primaryActionLabel = stringResource(R.string.eu_messages_code_revoked_button_text),
        primaryActionEnabled = false,
        primaryActionTint = AppTheme.colors.red700
    )
}

@Composable
internal fun EuAccessCodeGeneratedMessageCard(
    time: String,
    title: String,
    description: String,
    onClickShowCode: (() -> Unit)? = null,
    onClickRevokeAccess: (() -> Unit)? = null
) {
    EuAccessCodeMessageCard(
        time = time,
        title = title,
        description = description,
        primaryActionLabel = onClickShowCode?.let { stringResource(R.string.eu_messages_show_code_button_text) },
        secondaryActionLabel = onClickRevokeAccess?.let { stringResource(R.string.eu_messages_revoke_code_button_text) },
        onClickPrimaryAction = onClickShowCode,
        onClickSecondaryAction = onClickRevokeAccess
    )
}

@Composable
internal fun EuPrescriptionRemovedMessageCard(
    time: String,
    description: String
) {
    CommResV3MessageCard(
        cardIcon = Icons.Outlined.Close,
        cardIconTint = AppTheme.colors.red900,
        cardIconBackgroundColor = AppTheme.colors.red100,
        time = time,
        title = stringResource(R.string.eu_messages_prescription_removed_title),
        content = {
            CommResV3TextContent(text = description)
        }
    )
}

@Composable
internal fun EuPrescriptionAddedMessageCard(
    time: String,
    description: String
) {
    CommResV3MessageCard(
        cardIcon = Icons.Outlined.Add,
        cardIconTint = AppTheme.colors.primary900,
        cardIconBackgroundColor = AppTheme.colors.primary100,
        time = time,
        title = stringResource(R.string.eu_messages_prescription_added_title),
        content = {
            CommResV3TextContent(text = description)
        }
    )
}

@Composable
internal fun EuPrescriptionRedeemedMessageCard(
    time: String,
    title: String,
    description: String
) {
    CommResV3MessageCard(
        cardIcon = Icons.Outlined.Check,
        cardIconTint = AppTheme.colors.green900,
        cardIconBackgroundColor = AppTheme.colors.green100,
        time = time,
        title = title,
        content = {
            CommResV3TextContent(text = description)
        }
    )
}

@Composable
private fun EuAccessCodeMessageCard(
    time: String,
    title: String,
    description: String,
    primaryActionLabel: String? = null,
    primaryActionEnabled: Boolean = true,
    primaryActionTint: Color = AppTheme.colors.primary700,
    secondaryActionLabel: String? = null,
    onClickPrimaryAction: (() -> Unit)? = null,
    onClickSecondaryAction: (() -> Unit)? = null
) {
    CommResV3MessageCard(
        cardIcon = Icons.Outlined.Public,
        cardIconTint = AppTheme.colors.primary900,
        cardIconBackgroundColor = AppTheme.colors.primary100,
        time = time,
        title = title,
        content = {
            CommResV3TextContent(text = description)
        },
        actionContent = if (primaryActionLabel != null || secondaryActionLabel != null) {
            {
                Column {
                    primaryActionLabel?.let { label ->
                        CommResV3ActionContent(
                            text = label,
                            enabled = primaryActionEnabled,
                            tint = primaryActionTint,
                            onClickAction = { onClickPrimaryAction?.invoke() }
                        )
                    }
                    secondaryActionLabel?.let { label ->
                        if (primaryActionLabel != null) {
                            SpacerMedium()
                        }
                        CommResV3ActionContent(
                            text = label,
                            onClickAction = { onClickSecondaryAction?.invoke() }
                        )
                    }
                }
            }
        } else {
            null
        }
    )
}
