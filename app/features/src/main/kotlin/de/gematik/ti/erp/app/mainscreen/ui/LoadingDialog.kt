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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PreviewAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingDialog(
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.size(SizeDefaults.fivefold),
        onDismissRequest = onDismissRequest
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(SizeDefaults.doubleHalf),
            color = AppTheme.colors.primary400
        )
    }
}

@LightDarkPreview
@Composable
fun LoadingDialogPreview() {
    PreviewAppTheme {
        LoadingDialog {
        }
    }
}
