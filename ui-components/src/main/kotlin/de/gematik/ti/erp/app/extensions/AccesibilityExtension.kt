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

package de.gematik.ti.erp.app.extensions

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.popup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

/**
 * Applies a comprehensive set of accessibility semantics to the [Modifier], enabling improved screen reader support
 * and UI testability. This function allows for both string resource IDs and raw strings for descriptions and tags,
 * making it flexible for localization and dynamic content.
 *
 * It merges child semantics into this node, marks it with descriptive metadata (such as content description, role, state),
 * and optionally annotates it as a dialog, popup, or accessibility heading.
 *
 * @param contentDescriptionRes Optional string resource ID for the content description read by screen readers.
 *                              Takes precedence over [contentDescriptionString] if both are provided.
 * @param contentDescriptionString Optional raw string used as content description.
 * @param stateDescriptionRes Optional string resource ID describing the current state of the UI element.
 *                             Takes precedence over [stateDescriptionString] if both are provided.
 * @param stateDescriptionString Optional raw string describing the current state of the UI element.
 * @param tagRes Optional string resource ID used as a test tag. Takes precedence over [tagString] if both are provided.
 * @param tagString Optional raw string used as a test tag.
 * @param role Optional [Role] describing the semantic purpose of the element (e.g., [Role.Button], [Role.Switch]).
 *             Useful for custom elements where the role is not inferred automatically.
 * @param isDialog If true, marks this element as a dialog using [Modifier.dialog()].
 * @param isPopup If true, marks this element as a popup using [Modifier.popup()].
 * @param isEnabled Whether the element is enabled. A disabled element will be announced as such by screen readers.
 * @param isHeading If true, marks this node as a heading for accessibility grouping and navigation.
 * @param invisibleToUser If true, hides this node from screen readers and other accessibility services.
 * @param liveRegion Optional [LiveRegionMode] that tells screen readers how to announce updates to this element.
 *
 * @return A [Modifier] with all specified accessibility semantics applied.
 *
 * ### Examples
 *
 * #### 1. Basic Button with content description and test tag
 * ```
 * Button(
 *     onClick = { ... },
 *     modifier = Modifier.accessibility(
 *         contentDescriptionRes = R.string.submit,
 *         tagString = "SubmitButton",
 *         role = Role.Button
 *     )
 * ) {
 *     Text("Submit")
 * }
 * ```
 *
 * #### 2. Icon-only button with content description
 * ```
 * IconButton(
 *     onClick = { ... },
 *     modifier = Modifier.accessibility(
 *         contentDescriptionString = "Settings",
 *         role = Role.Button
 *     )
 * ) {
 *     Icon(Icons.Default.Settings, contentDescription = null)
 * }
 * ```
 *
 * #### 3. Switch with dynamic state description
 * ```
 * val isChecked by remember { mutableStateOf(true) }
 *
 * Row(
 *     modifier = Modifier
 *         .clickable { isChecked = !isChecked }
 *         .accessibility(
 *             contentDescriptionRes = R.string.notifications,
 *             stateDescriptionString = if (isChecked) "Enabled" else "Disabled",
 *             role = Role.Switch
 *         )
 * ) {
 *     Text("Notifications")
 *     Switch(checked = isChecked, onCheckedChange = null)
 * }
 * ```
 *
 * #### 4. Dialog container
 * ```
 * Surface(
 *     modifier = Modifier.accessibility(
 *         contentDescriptionRes = R.string.dialog_title,
 *         isDialog = true,
 *         role = Role.Dialog
 *     )
 * ) {
 *     // Dialog content
 * }
 * ```
 *
 * #### 5. Image with description and test tag
 * ```
 * Image(
 *     painter = painterResource(id = R.drawable.profile),
 *     contentDescription = null,
 *     modifier = Modifier.accessibility(
 *         contentDescriptionString = "Profile picture",
 *         tagString = "ProfileImage",
 *         role = Role.Image
 *     )
 * )
 * ```
 *
 * #### 6. Visually hidden but semantically available label
 * ```
 * Text(
 *     text = "Invisible label",
 *     modifier = Modifier.accessibility(
 *         contentDescriptionString = "This label is for screen readers only",
 *         invisibleToUser = true
 *     )
 * )
 * ```
 */
@Composable
fun Modifier.accessibility(
    @StringRes contentDescriptionRes: Int? = null,
    contentDescriptionString: String? = null,
    @StringRes stateDescriptionRes: Int? = null,
    stateDescriptionString: String? = null,
    @StringRes tagRes: Int? = null,
    tagString: String? = null,
    role: Role? = null,
    isDialog: Boolean = false,
    isPopup: Boolean = false,
    isEnabled: Boolean = true,
    isHeading: Boolean = false,
    invisibleToUser: Boolean = false,
    liveRegion: LiveRegionMode? = null
): Modifier {
    val contentDescription = contentDescriptionRes?.let { stringResource(id = it) } ?: contentDescriptionString
    val stateDescription = stateDescriptionRes?.let { stringResource(id = it) } ?: stateDescriptionString
    val tag = tagRes?.let { stringResource(id = it) } ?: tagString

    return this
        .then(tag?.let { this.testTag(it) } ?: this)
        .semantics(mergeDescendants = true) {
            contentDescription?.let { this.contentDescription = it }
            if (!isEnabled) {
                disabled()
            }
            if (isDialog) {
                dialog()
            }
            if (isPopup) {
                popup()
            }
            if (isHeading) {
                heading()
            }
            if (invisibleToUser) {
                this.invisibleToUser()
            }
            stateDescription?.let { this.stateDescription = it }
            liveRegion?.let { this.liveRegion = it }
            role?.let { this.role = it }
        }
}
