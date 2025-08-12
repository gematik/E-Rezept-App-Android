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

@file:Suppress("UnusedPrivateMember")

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.rounded.Coronavirus
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.ErezeptText.TextAlignment
import de.gematik.ti.erp.app.utils.compose.ErezeptText.TextAlignment.Center
import de.gematik.ti.erp.app.utils.compose.ErezeptText.TextAlignment.Default
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.letNotNullOnCondition

//region Alert dialog with only title and one button
@Composable
fun ErezeptAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    okText: String = stringResource(R.string.ok),
    dismissTextColor: Color? = null,
    titleAlignment: TextAlignment = Center,
    dismissButtonTestTag: String = TestTag.AlertDialog.CancelButton,
    buttonsArrangement: Arrangement.Horizontal = Arrangement.End,
    dismissButtonIcon: ImageVector? = null,
    onDismissRequest: () -> Unit
) {
    InternalErezeptAlertDialog(
        modifier = modifier,
        title = title,
        titleAlignment = titleAlignment,
        body = null,
        buttonsArrangement = buttonsArrangement,
        dismissButtonTestTag = dismissButtonTestTag,
        dismissText = okText,
        dismissTextColor = dismissTextColor,
        dismissButtonIcon = dismissButtonIcon,
        onConfirmRequest = onDismissRequest,
        onDismissRequest = onDismissRequest,
        onDismissButtonRequest = onDismissRequest
    )
}

@LightDarkPreview
@Composable
fun ErezeptAlertDialogWithOnlyTitlePreview() {
    PreviewAppTheme {
        ErezeptAlertDialog(
            title = "Dialog with only title and one button",
            onDismissRequest = {}
        )
    }
}

//endregion

//region Alert dialog with only body and one button
@Composable
fun ErezeptAlertDialog(
    modifier: Modifier = Modifier,
    body: String,
    dismissButtonTestTag: String = TestTag.AlertDialog.CancelButton,
    confirmButtonTestTag: String = TestTag.AlertDialog.ConfirmButton,
    okText: String = stringResource(R.string.ok),
    dismissText: String = stringResource(R.string.cancel),
    buttonsArrangement: Arrangement.Horizontal = Arrangement.End,
    onConfirmRequest: () -> Unit,
    onDismissRequest: () -> Unit
) {
    InternalErezeptAlertDialog(
        modifier = modifier,
        body = body,
        buttonsArrangement = buttonsArrangement,
        confirmText = okText,
        dismissText = dismissText,
        confirmButtonTestTag = confirmButtonTestTag,
        dismissButtonTestTag = dismissButtonTestTag,
        onConfirmRequest = onConfirmRequest,
        onDismissRequest = onDismissRequest,
        onDismissButtonRequest = onDismissRequest
    )
}

@LightDarkPreview
@Composable
fun ErezeptAlertDialogWithOnlyBodyPreview() {
    PreviewAppTheme {
        ErezeptAlertDialog(
            body = "Dialog with only body and two buttons",
            onConfirmRequest = {},
            onDismissRequest = {}
        )
    }
}

//endregion

//region Alert dialog with title and one button
@Composable
fun ErezeptAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    body: String,
    dismissButtonTestTag: String = TestTag.AlertDialog.CancelButton,
    okText: String = stringResource(R.string.ok),
    titleAlignment: TextAlignment = Center,
    buttonsArrangement: Arrangement.Horizontal = Arrangement.End,
    dismissButtonIcon: ImageVector? = null,
    onDismissRequest: () -> Unit
) {
    InternalErezeptAlertDialog(
        modifier = modifier,
        title = title,
        titleAlignment = titleAlignment,
        body = body,
        buttonsArrangement = buttonsArrangement,
        dismissButtonTestTag = dismissButtonTestTag,
        dismissText = okText,
        dismissButtonIcon = dismissButtonIcon,
        onConfirmRequest = onDismissRequest,
        onDismissRequest = onDismissRequest,
        onDismissButtonRequest = onDismissRequest
    )
}

@LightDarkPreview
@Composable
fun ErezeptAlertDialogPreview() {
    PreviewAppTheme {
        ErezeptAlertDialog(
            title = "Dialog with one button",
            body = "A dialog is a type of modal window that appears in front of app content to " +
                "provide critical information, or ask for decision",
            onDismissRequest = {}
        )
    }
}

@LightDarkPreview
@Composable
fun ErezeptAlertDialogWithIconPreview() {
    PreviewAppTheme {
        ErezeptAlertDialog(
            title = "Dialog with one button and dismiss icon",
            titleAlignment = Default,
            dismissButtonIcon = Icons.AutoMirrored.Filled.ArrowForward,
            body = "A dialog is a type of modal window that appears in front of app content to " +
                "provide critical information, or ask for decision",
            onDismissRequest = {}
        )
    }
}
//endregion

//region Alert dialog with body and one button
@Composable
fun ErezeptAlertDialog(
    modifier: Modifier = Modifier,
    body: String,
    dismissButtonTestTag: String = TestTag.AlertDialog.CancelButton,
    dismissText: String = stringResource(R.string.ok),
    titleAlignment: TextAlignment = Center,
    buttonsArrangement: Arrangement.Horizontal = Arrangement.End,
    dismissButtonIcon: ImageVector? = null,
    onDismissRequest: () -> Unit
) {
    InternalErezeptAlertDialog(
        modifier = modifier,
        title = null,
        titleAlignment = titleAlignment,
        body = body,
        buttonsArrangement = buttonsArrangement,
        dismissButtonTestTag = dismissButtonTestTag,
        dismissText = dismissText,
        dismissButtonIcon = dismissButtonIcon,
        onConfirmRequest = onDismissRequest,
        onDismissRequest = onDismissRequest,
        onDismissButtonRequest = onDismissRequest
    )
}

@LightDarkPreview
@Composable
fun ErezeptAlertBodyOneButtonsDialogPreview() {
    PreviewAppTheme {
        ErezeptAlertDialog(
            body = "A dialog with body and one button",
            dismissText = "Cancel",
            onDismissRequest = {}
        )
    }
}

//endregion

//region Alert dialog with two buttons
@Composable
fun ErezeptAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    titleIcon: ImageVector? = null,
    bodyText: String,
    confirmButtonTestTag: String = TestTag.AlertDialog.ConfirmButton,
    dismissButtonTestTag: String = TestTag.AlertDialog.CancelButton,
    confirmText: String = stringResource(R.string.ok),
    dismissText: String,
    confirmTextColor: Color? = null,
    dismissTextColor: Color? = null,
    titleAlignment: TextAlignment = Center,
    buttonsArrangement: Arrangement.Horizontal = Arrangement.End,
    confirmButtonIcon: ImageVector? = null,
    dismissButtonIcon: ImageVector? = null,
    onConfirmRequest: () -> Unit,
    onDismissRequest: () -> Unit
) {
    InternalErezeptAlertDialog(
        modifier = modifier,
        title = title,
        titleIcon = titleIcon,
        body = bodyText,
        confirmText = confirmText,
        dismissText = dismissText,
        confirmTextColor = confirmTextColor,
        dismissTextColor = dismissTextColor,
        dismissButtonTestTag = dismissButtonTestTag,
        confirmButtonTestTag = confirmButtonTestTag,
        confirmButtonIcon = confirmButtonIcon,
        dismissButtonIcon = dismissButtonIcon,
        titleAlignment = titleAlignment,
        buttonsArrangement = buttonsArrangement,
        onConfirmRequest = onConfirmRequest,
        onDismissRequest = onDismissRequest,
        onDismissButtonRequest = onDismissRequest
    )
}

// If we need the dismiss button to do something different than the cancel button, use this composable
@Composable
fun ErezeptAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    titleIcon: ImageVector? = null,
    body: String,
    confirmText: String = stringResource(R.string.ok),
    dismissText: String,
    titleAlignment: TextAlignment = Center,
    buttonsArrangement: Arrangement.Horizontal = Arrangement.End,
    confirmButtonIcon: ImageVector? = null,
    dismissButtonIcon: ImageVector? = null,
    onConfirmRequest: () -> Unit,
    onDismissRequest: () -> Unit,
    onDismissButtonRequest: () -> Unit
) {
    InternalErezeptAlertDialog(
        modifier = modifier,
        title = title,
        titleIcon = titleIcon,
        body = body,
        confirmText = confirmText,
        dismissText = dismissText,
        confirmButtonIcon = confirmButtonIcon,
        dismissButtonIcon = dismissButtonIcon,
        titleAlignment = titleAlignment,
        buttonsArrangement = buttonsArrangement,
        onConfirmRequest = onConfirmRequest,
        onDismissRequest = onDismissRequest,
        onDismissButtonRequest = onDismissButtonRequest
    )
}

@LightDarkPreview
@Composable
fun ErezeptAlertTwoButtonsDialogPreview() {
    PreviewAppTheme {
        ErezeptAlertDialog(
            title = "Dialog with two buttons",
            bodyText = "A dialog is a type of modal window that appears in front of app content to " +
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
            title = "Dialog with two buttons and icons",
            bodyText = "A dialog is a type of modal window that appears in front of app content to " +
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

//endregion

//region Alert dialog with custom buttons
@Composable
fun ErezeptAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    titleIcon: ImageVector? = null,
    titleAlignment: TextAlignment = Center,
    bodyAlignment: TextAlignment = Default,
    body: String,
    onDismissRequest: () -> Unit,
    buttons: @Composable ColumnScope.() -> Unit
) {
    InternalErezeptAlertDialog(
        modifier = modifier,
        title = title,
        titleIcon = titleIcon,
        titleAlignment = titleAlignment,
        bodyAlignment = bodyAlignment,
        body = body,
        onDismissRequest = onDismissRequest,
        buttons = {
            // centering the buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                buttons()
            }
        }
    )
}

@LightDarkPreview
@Composable
fun ErezeptAlertMultiButtonsDialogPreview() {
    PreviewAppTheme {
        ErezeptAlertDialog(
            title = "Dialog with composable view for buttons",
            body = "A dialog is a type of modal window that appears in front of app content to " +
                "provide critical information, or ask for decision",
            onDismissRequest = {},
            buttons = {
                TextButton(onClick = {}) {
                    Text("Button 1")
                }
                TextButton(onClick = {}) {
                    Text("Button 2")
                }
                TextButton(onClick = {}) {
                    Text("Button 3")
                }
            }
        )
    }
}

//endregion

//region Alert dialog with composable body and one button

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErezeptAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    body: (@Composable ColumnScope.() -> Unit),
    confirmButtonTestTag: String = TestTag.AlertDialog.ConfirmButton,
    okText: String = stringResource(R.string.ok),
    onDismissRequest: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colors.surface,
            shape = RoundedCornerShape(SizeDefaults.triple),
            border = BorderStroke(
                width = SizeDefaults.sixteenth,
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
                body.invoke(this)
                SpacerMedium()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        modifier = Modifier.testTag(confirmButtonTestTag),
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
private fun ErezeptAlertDialogWithNoComposableBodyPreview() {
    PreviewAppTheme {
        ErezeptAlertDialog(
            title = "Alert dialog with no composable body and one button",
            body = {},
            okText = stringResource(R.string.ok),
            onDismissRequest = {}
        )
    }
}

@LightDarkPreview
@Composable
fun ErezeptAlertDialogWithComposableBodyPreview() {
    PreviewAppTheme {
        ErezeptAlertDialog(
            title = "Alert dialog with composable body and one button",
            body = {
                Text(
                    modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                    text = "BeispielText"
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Coronavirus, null)
                }
            },
            onDismissRequest = {}
        )
    }
}

//endregion

//region Alert dialog with two buttons and composable body
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErezeptAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    body: @Composable ColumnScope.() -> Unit,
    confirmButtonTestTag: String = TestTag.AlertDialog.ConfirmButton,
    dismissButtonTestTag: String = TestTag.AlertDialog.CancelButton,
    cancelText: String = stringResource(R.string.cancel),
    okText: String = stringResource(R.string.ok),
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier
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
                ErezeptText.Title(title)
                SpacerMedium()
                body()
                SpacerMedium()
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        modifier = Modifier.testTag(dismissButtonTestTag),
                        onClick = onDismissRequest
                    ) {
                        Text(cancelText)
                    }
                    TextButton(
                        modifier = Modifier.testTag(confirmButtonTestTag),
                        onClick = onConfirmRequest
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
private fun ErezeptAlertDialogWithNoComposableBodyTwoButtonsPreview() {
    PreviewAppTheme {
        ErezeptAlertDialog(
            title = "Alert dialog with no composable body and two buttons",
            body = {},
            okText = stringResource(R.string.ok),
            onDismissRequest = {},
            onConfirmRequest = {}
        )
    }
}

@LightDarkPreview
@Composable
fun ErezeptAlertDialogWithComposableBodyTwoButtonsPreview() {
    PreviewAppTheme {
        ErezeptAlertDialog(
            title = "Alert dialog with composable body and two buttons",
            body = {
                Text(
                    modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                    text = "BeispielText"
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Coronavirus, null)
                }
            },
            onDismissRequest = {},
            onConfirmRequest = {}
        )
    }
}

//endregion

//region Alert dialog with two buttons and composable extra actions and composable body
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErezeptAlertDialog(
    modifier: Modifier = Modifier,
    title: @Composable ColumnScope.() -> Unit,
    body: @Composable ColumnScope.() -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    confirmButtonTestTag: String = TestTag.AlertDialog.ConfirmButton,
    dismissButtonTestTag: String = TestTag.AlertDialog.CancelButton,
    cancelText: String = stringResource(R.string.cancel),
    okText: String = stringResource(R.string.ok),
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier
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
                modifier = Modifier.padding(horizontal = PaddingDefaults.Large),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SpacerMedium()
                title()
                SpacerMedium()
                body()
                SpacerMedium()
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    actions()
                    TextButton(
                        modifier = Modifier.testTag(dismissButtonTestTag),
                        onClick = onDismissRequest
                    ) {
                        Text(cancelText)
                    }
                    TextButton(
                        modifier = Modifier.testTag(confirmButtonTestTag),
                        onClick = onConfirmRequest
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
private fun ErezeptAlertDialogWithNoComposableBodyTwoButtonsWithActionsPreview() {
    PreviewAppTheme {
        ErezeptAlertDialog(
            title = {
                Text(
                    "Alert dialog with no composable body and two buttons and extra action"
                )
            },
            body = {},
            okText = stringResource(R.string.ok),
            actions = {
                IconButton({}) {
                    Icon(Icons.Rounded.Keyboard, null)
                }
                Spacer(Modifier.weight(1f))
            },
            onDismissRequest = {},
            onConfirmRequest = {}
        )
    }
}

@LightDarkPreview
@Composable
fun ErezeptAlertDialogWithComposableBodyTwoButtonsWithActionsPreview() {
    PreviewAppTheme {
        ErezeptAlertDialog(
            title = {
                Text("Alert dialog with composable body and two buttons and extra action")
            },
            body = {
                Text(
                    modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                    text = "BeispielText"
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Coronavirus, null)
                }
            },
            actions = {
                IconButton({}) {
                    Icon(Icons.Rounded.Keyboard, null)
                }
                Spacer(Modifier.weight(1f))
            },
            onDismissRequest = {},
            onConfirmRequest = {}
        )
    }
}

//endregion
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InternalErezeptAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    titleIcon: ImageVector? = null,
    titleAlignment: TextAlignment = Center,
    bodyAlignment: TextAlignment = Default,
    body: String,
    onDismissRequest: () -> Unit,
    buttons: @Composable ColumnScope.() -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier.padding(horizontal = PaddingDefaults.Large)
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
                titleIcon?.let { titleIcon ->
                    SpacerMedium()
                    Icon(
                        imageVector = titleIcon,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                SpacerMedium()
                ErezeptText.Title(
                    text = title,
                    textAlignment = titleAlignment
                )
                SpacerMedium()
                ErezeptText.Body(
                    text = body,
                    textAlignment = bodyAlignment
                )
                SpacerMedium()
                buttons()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InternalErezeptAlertDialog(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleIcon: ImageVector? = null,
    titleAlignment: TextAlignment = Center,
    confirmButtonTestTag: String = TestTag.AlertDialog.ConfirmButton,
    dismissButtonTestTag: String = TestTag.AlertDialog.CancelButton,
    body: String? = null,
    confirmText: String? = null,
    dismissText: String,
    confirmTextColor: Color? = null,
    dismissTextColor: Color? = null,
    buttonsArrangement: Arrangement.Horizontal = Arrangement.End,
    confirmButtonIcon: ImageVector? = null,
    dismissButtonIcon: ImageVector? = null,
    onConfirmRequest: (() -> Unit)? = null,
    onDismissRequest: () -> Unit,
    onDismissButtonRequest: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier.padding(horizontal = PaddingDefaults.Large)
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
                titleIcon?.let { titleIcon ->
                    SpacerMedium()
                    Icon(
                        imageVector = titleIcon,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                SpacerMedium()
                title?.let { nonNullTitle ->
                    ErezeptText.Title(
                        text = nonNullTitle,
                        textAlignment = titleAlignment
                    )
                    SpacerMedium()
                }
                body?.let { nonNullBody ->
                    ErezeptText.Body(nonNullBody)
                    SpacerMedium()
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = buttonsArrangement
                ) {
                    TextButton(
                        modifier = Modifier.testTag(dismissButtonTestTag),
                        onClick = onDismissButtonRequest
                    ) {
                        Text(
                            text = dismissText,
                            color = dismissTextColor ?: AppTheme.colors.primary700
                        )
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
                            modifier = Modifier.testTag(confirmButtonTestTag),
                            onClick = {
                                onConfirmRequest?.invoke()
                            }
                        ) {
                            Text(
                                text = text,
                                color = confirmTextColor ?: AppTheme.colors.primary700
                            )
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
