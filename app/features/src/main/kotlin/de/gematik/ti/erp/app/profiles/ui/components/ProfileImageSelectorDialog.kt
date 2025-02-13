/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.profiles.ui.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.Center
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.ErezeptText
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold

// to show dialog with event
@Composable
internal fun ProfileImageSelectorDialog(
    composableEvent: ComposableEvent<Unit>,
    dialogScaffold: DialogScaffold,
    onPickEmojiImage: () -> Unit,
    onPickPersonalizedImage: () -> Unit,
    onPickCamera: () -> Unit
) {
    composableEvent.listen {
        dialogScaffold.show {
            ProfileImageSelectorDialog(
                onPickEmoji = {
                    onPickEmojiImage()
                    it.dismiss()
                },
                onPickCamera = {
                    onPickCamera()
                    it.dismiss()
                },
                onPickPersonalizedImage = {
                    onPickPersonalizedImage()
                    it.dismiss()
                },
                onDismiss = {
                    it.dismiss()
                }
            )
        }
    }
}

// to show dialog without an event, but just click
internal fun showProfileImageSelectorDialog(
    dialogScaffold: DialogScaffold,
    onPickEmojiImage: () -> Unit,
    onPickPersonalizedImage: () -> Unit,
    onPickCamera: () -> Unit
) {
    dialogScaffold.show {
        ProfileImageSelectorDialog(
            onPickEmoji = {
                onPickEmojiImage()
                it.dismiss()
            },
            onPickCamera = {
                onPickCamera()
                it.dismiss()
            },
            onPickPersonalizedImage = {
                onPickPersonalizedImage()
                it.dismiss()
            },
            onDismiss = {
                it.dismiss()
            }
        )
    }
}

@Suppress("MagicNumber")
@Composable
private fun ProfileImageSelectorDialog(
    onPickEmoji: () -> Unit,
    onPickPersonalizedImage: () -> Unit,
    onPickCamera: () -> Unit,
    onDismiss: () -> Unit
) {
    ErezeptAlertDialog(
        title = stringResource(R.string.profile_image_selector_dialog_title),
        body = stringResource(R.string.profile_image_selector_dialog_body),
        bodyAlignment = ErezeptText.TextAlignment.Center,
        onDismissRequest = onDismiss,
        buttons = {
            ProfileImageSelectorButton(
                text = stringResource(R.string.profile_image_selector_dialog_gallery_button),
                onClick = onPickPersonalizedImage
            )
            HorizontalDivider(modifier = Modifier.alpha(0.3f))
            ProfileImageSelectorButton(
                text = stringResource(R.string.profile_image_selector_dialog_camera_button),
                onClick = onPickCamera
            )
            HorizontalDivider(modifier = Modifier.alpha(0.3f))
            ProfileImageSelectorButton(
                text = stringResource(R.string.profile_image_selector_dialog_emoji_button),
                onClick = onPickEmoji
            )
            HorizontalDivider(modifier = Modifier.alpha(0.3f))
            ProfileImageSelectorButton(
                text = stringResource(R.string.profile_image_selector_dialog_cancel_button),
                onClick = onDismiss
            )
            SpacerMedium()
        }
    )
}

@Suppress("MagicNumber")
@Composable
private fun ProfileImageSelectorButton(
    text: String,
    tint: Color = AppTheme.colors.primary700,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Center {
            Text(
                color = tint,
                textAlign = TextAlign.Center,
                text = text
            )
        }
    }
}

@LightDarkPreview
@Composable
fun ProfileImageSelectorDialogPreview() {
    PreviewAppTheme {
        ProfileImageSelectorDialog(
            onPickEmoji = {},
            onPickPersonalizedImage = {},
            onPickCamera = {},
            onDismiss = {}
        )
    }
}
