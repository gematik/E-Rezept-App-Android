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

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PreviewAppTheme

@Composable
fun UniversalLinkWrongDialog(
    onDismissRequest: () -> Unit
) {
    ErezeptAlertDialog(
        title = stringResource(R.string.main_fasttrack_error_title),
        body = stringResource(id = R.string.main_fasttrack_error_info),
        onDismissRequest = onDismissRequest
    )
}

@LightDarkPreview
@Composable
fun UniversalLinkWrongDialogPreview() {
    PreviewAppTheme {
        UniversalLinkWrongDialog {
        }
    }
}
