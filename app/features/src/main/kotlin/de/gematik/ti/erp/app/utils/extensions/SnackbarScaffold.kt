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

package de.gematik.ti.erp.app.utils.extensions

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import de.gematik.ti.erp.app.features.R

interface SnackbarScaffold {
    fun show(
        text: String,
        @DrawableRes icon: Int = R.drawable.ic_logo_outlined,
        @ColorRes backgroundTint: Int = R.color.primary_600
    )
}

val LocalSnackbar: SnackbarScaffoldCompositionLocal = SnackbarScaffoldCompositionLocal

object SnackbarScaffoldCompositionLocal {
    val current: SnackbarScaffold
        @Composable
        get() = LocalContext.current as SnackbarScaffold
}
