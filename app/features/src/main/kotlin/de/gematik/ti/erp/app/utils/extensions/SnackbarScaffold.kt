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

package de.gematik.ti.erp.app.utils.extensions

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.snackbar.Snackbar
import de.gematik.ti.erp.app.app_core.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
interface SnackbarScaffold {
    fun show(
        text: String,
        actionTextId: Int? = null,
        length: Int = Snackbar.LENGTH_SHORT,
        onClickAction: () -> Unit? = {},
        @DrawableRes icon: Int = R.drawable.ic_logo_outlined,
        @ColorRes backgroundTint: Int = R.color.neutral_900
    )
}

@Deprecated("Use LocalSnackbarScaffold instead", ReplaceWith("LocalSnackbarScaffold"))
val LocalSnackbar: SnackbarScaffoldCompositionLocal = SnackbarScaffoldCompositionLocal

val LocalSnackbarScaffold = staticCompositionLocalOf<SnackbarHostState> { error("No snackbar provided!") }

fun SnackbarHostState.dismiss() = currentSnackbarData?.dismiss()

fun SnackbarHostState.showWithDismissButton(
    message: String,
    actionLabel: String,
    duration: SnackbarDuration = SnackbarDuration.Short,
    scope: CoroutineScope
) {
    scope.launch {
        val result = showSnackbar(
            message = message,
            duration = duration,
            actionLabel = actionLabel
        )

        when (result) {
            SnackbarResult.Dismissed -> dismiss()
            SnackbarResult.ActionPerformed -> dismiss()
        }
    }
}

fun SnackbarHostState.show(
    message: String,
    scope: CoroutineScope
) {
    scope.launch { showSnackbar(message = message) }
}

object SnackbarScaffoldCompositionLocal {
    val current: SnackbarScaffold
        @Composable
        get() = LocalContext.current as SnackbarScaffold
}
