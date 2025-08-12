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
// from ui-compoenent module
@file:Suppress("TooManyFunctions", "MagicNumber")

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.semantics.semanticsHeading

@Composable
fun NavigationClose(
    modifier: Modifier = Modifier,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .testTag(TestTag.TopNavigation.CloseButton)
    ) {
        Icon(
            Icons.Rounded.Close,
            null,
            tint = MaterialTheme.colors.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun NavigateBackButton(
    modifier: Modifier = Modifier,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .testTag(TestTag.TopNavigation.BackButton)
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.ArrowBack,
            null,
            tint = MaterialTheme.colors.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

enum class NavigationBarMode {
    Back,
    Close
}

@Composable
fun NavigationTopAppBar(
    modifier: Modifier = Modifier,
    navigationMode: NavigationBarMode?,
    title: String,
    isTitleCentered: Boolean = false,
    backgroundColor: Color = MaterialTheme.colors.surface,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    actions: @Composable RowScope.() -> Unit = {},
    backLabel: String,
    closeLabel: String,
    onBack: () -> Unit
) = TopAppBar(
    modifier = modifier.semanticsHeading(),
    title = {
        if (isTitleCentered) {
            Center {
                Text(title, overflow = TextOverflow.Ellipsis)
            }
        } else {
            Text(title, overflow = TextOverflow.Ellipsis)
        }
    },
    backgroundColor = backgroundColor,
    navigationIcon = {
        when (navigationMode) {
            NavigationBarMode.Back -> NavigateBackButton(
                contentDescription = backLabel
            ) { onBack() }
            NavigationBarMode.Close -> NavigationClose(
                contentDescription = closeLabel
            ) { onBack() }
            else -> {}
        }
    },
    elevation = elevation,
    actions = actions
)

@Composable
fun NavigationTopAppBar(
    modifier: Modifier = Modifier,
    navigationMode: NavigationBarMode?,
    title: @Composable () -> Unit,
    isTitleCentered: Boolean = false,
    backgroundColor: Color = MaterialTheme.colors.surface,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    actions: @Composable RowScope.() -> Unit = {},
    backLabel: String,
    closeLabel: String,
    onBack: () -> Unit
) = TopAppBar(
    modifier = modifier.semanticsHeading(),
    title = {
        if (isTitleCentered) {
            Center { title() }
        } else {
            title()
        }
    },
    backgroundColor = backgroundColor,
    navigationIcon = {
        when (navigationMode) {
            NavigationBarMode.Back -> NavigateBackButton(
                contentDescription = backLabel
            ) { onBack() }
            NavigationBarMode.Close -> NavigationClose(
                contentDescription = closeLabel
            ) { onBack() }
            else -> {}
        }
    },
    elevation = elevation,
    actions = actions
)
