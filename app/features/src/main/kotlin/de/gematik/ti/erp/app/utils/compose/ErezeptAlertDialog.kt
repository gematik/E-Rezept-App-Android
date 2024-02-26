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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.ErezeptText.ErezeptTextAlignment.Center
import de.gematik.ti.erp.app.utils.compose.ErezeptText.ErezeptTextAlignment.Default
import de.gematik.ti.erp.app.utils.letNotNullOnCondition

@Composable
fun ErezeptAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
    okText: String = stringResource(R.string.ok),
    titleAlignment: ErezeptText.ErezeptTextAlignment = Center,
    buttonsArrangement: Arrangement.Horizontal = Arrangement.End,
    dismissButtonIcon: ImageVector? = null,
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit
) {
    InternalErezeptAlertDialog(
        modifier = modifier,
        title = title,
        titleAlignment = titleAlignment,
        body = body,
        buttonsArrangement = buttonsArrangement,
        dismissText = okText,
        dismissButtonIcon = dismissButtonIcon,
        onConfirmRequest = onConfirmRequest,
        onDismissRequest = onDismissRequest
    )
}

@Composable
fun ErezeptAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
    confirmText: String = stringResource(R.string.ok),
    dismissText: String,
    titleAlignment: ErezeptText.ErezeptTextAlignment = Center,
    buttonsArrangement: Arrangement.Horizontal = Arrangement.End,
    confirmButtonIcon: ImageVector? = null,
    dismissButtonIcon: ImageVector? = null,
    onConfirmRequest: () -> Unit,
    onDismissRequest: () -> Unit
) {
    InternalErezeptAlertDialog(
        modifier = modifier,
        title = title,
        body = body,
        confirmText = confirmText,
        dismissText = dismissText,
        confirmButtonIcon = confirmButtonIcon,
        dismissButtonIcon = dismissButtonIcon,
        titleAlignment = titleAlignment,
        buttonsArrangement = buttonsArrangement,
        onConfirmRequest = onConfirmRequest,
        onDismissRequest = onDismissRequest
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InternalErezeptAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    titleAlignment: ErezeptText.ErezeptTextAlignment = Center,
    body: String,
    confirmText: String? = null,
    dismissText: String,
    buttonsArrangement: Arrangement.Horizontal = Arrangement.End,
    confirmButtonIcon: ImageVector? = null,
    dismissButtonIcon: ImageVector? = null,
    onConfirmRequest: (() -> Unit)? = null,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            color = AppTheme.colors.neutral025,
            shape = RoundedCornerShape(SizeDefaults.triple),
            border = BorderStroke(
                width = SizeDefaults.eighth,
                color = AppTheme.colors.neutral400
            ),
            contentColor = contentColorFor(AppTheme.colors.neutral025)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PaddingDefaults.Large)
            ) {
                SpacerMedium()
                ErezeptText.Title(
                    text = title,
                    textAlignment = titleAlignment
                )
                SpacerMedium()
                ErezeptText.Body(body)
                SpacerMedium()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = buttonsArrangement
                ) {
                    TextButton(
                        modifier = Modifier.testTag(TestTag.AlertDialog.CancelButton),
                        onClick = onDismissRequest
                    ) {
                        Text(dismissText)
                        dismissButtonIcon?.let { dismissIcon ->
                            SpacerTiny()
                            Icon(
                                imageVector = dismissIcon,
                                contentDescription = null
                            )
                        }
                    }
                    letNotNullOnCondition(
                        first = confirmText,
                        condition = { onConfirmRequest != null }
                    ) { text ->
                        TextButton(
                            modifier = Modifier.testTag(TestTag.AlertDialog.ConfirmButton),
                            onClick = {
                                onConfirmRequest?.invoke()
                            }
                        ) {
                            Text(text)
                            confirmButtonIcon?.let { confirmIcon ->
                                SpacerTiny()
                                Icon(
                                    imageVector = confirmIcon,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
                SpacerMedium()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErezeptAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    body: @Composable ColumnScope.() -> Unit,
    okText: String = stringResource(R.string.ok),
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            color = MaterialTheme.colors.surface,
            shape = RoundedCornerShape(SizeDefaults.triple),
            border = BorderStroke(
                width = SizeDefaults.eighth,
                color = AppTheme.colors.neutral400
            ),
            contentColor = contentColorFor(MaterialTheme.colors.surface)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = PaddingDefaults.Large)
            ) {
                SpacerMedium()
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ErezeptText.Title(title)
                }
                SpacerMedium()
                body()
                SpacerMedium()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        modifier = Modifier.testTag(TestTag.AlertDialog.ConfirmButton),
                        onClick = onDismissRequest
                    ) {
                        Text(okText)
                    }
                }
                SpacerMedium()
            }
        }
    }
}

@LightDarkPreview
@Composable
fun ErezeptAlertDialogPreview() {
    PreviewAppTheme {
        ErezeptAlertDialog(
            title = "Dialog with one button",
            body = "A dialog is a type of modal window that apperars in front of app content to " +
                "provide critical information, or ask for decision",
            onConfirmRequest = {},
            onDismissRequest = {}
        )
    }
}

@LightDarkPreview
@Composable
fun ErezeptAlertDialogWithIconPreview() {
    PreviewAppTheme {
        ErezeptAlertDialog(
            title = "Dialog with one button",
            titleAlignment = Default,
            dismissButtonIcon = Icons.Default.ArrowForward,
            body = "A dialog is a type of modal window that apperars in front of app content to " +
                "provide critical information, or ask for decision",
            onConfirmRequest = {},
            onDismissRequest = {}
        )
    }
}

@LightDarkPreview
@Composable
fun ErezeptAlertTwoButtonsDialogPreview() {
    PreviewAppTheme {
        ErezeptAlertDialog(
            title = "Dialog with two buttons",
            body = "A dialog is a type of modal window that apperars in front of app content to " +
                "provide critical information, or ask for decision",
            confirmText = "Ok",
            dismissText = "Cancel",
            onDismissRequest = {},
            onConfirmRequest = {}
        )
    }
}

@LightDarkPreview
@Composable
fun ErezeptAlertTwoButtonsWithIconsDialogPreview() {
    PreviewAppTheme {
        ErezeptAlertDialog(
            title = "Dialog with two buttons",
            body = "A dialog is a type of modal window that apperars in front of app content to " +
                "provide critical information, or ask for decision",
            confirmText = "Ok",
            dismissText = "Cancel",
            confirmButtonIcon = Icons.Default.AddTask,
            dismissButtonIcon = Icons.Default.Cancel,
            onDismissRequest = {},
            onConfirmRequest = {}
        )
    }
}
