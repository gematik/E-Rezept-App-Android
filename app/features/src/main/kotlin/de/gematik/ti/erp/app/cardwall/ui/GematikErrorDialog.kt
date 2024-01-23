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

package de.gematik.ti.erp.app.cardwall.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.idp.model.error.GematikResponseError
import de.gematik.ti.erp.app.idp.model.error.GematikResponseError.Companion.emptyResponseError
import de.gematik.ti.erp.app.idp.model.error.GematikResponseError.Companion.prettyErrorCode
import de.gematik.ti.erp.app.idp.model.error.GematikResponseError.Companion.prettyErrorText
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.ErezeptText
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.SpacerSmall

private const val MARQUEE_DELAY = 2000

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GematikErrorDialog(
    error: GematikResponseError,
    onDismissRequest: () -> Unit
) {
    ErezeptAlertDialog(
        title = stringResource(R.string.main_fasttrack_error_title),
        body = {
            ErezeptText.Subtitle(stringResource(R.string.gid_gematik_error_subtitle))
            ErezeptText.Body(error.prettyErrorCode())
            ErezeptText.Body(error.prettyErrorText())
            SpacerSmall()
            ErezeptText.Subtitle(stringResource(R.string.gid_gematik_error_code))
            ErezeptText.Body(error.gematikCode)
            SpacerSmall()
            ErezeptText.Subtitle(stringResource(R.string.gid_gematik_error_uuid))
            ErezeptText.Body(
                modifier = Modifier.basicMarquee(delayMillis = MARQUEE_DELAY),
                text = error.gematikUuid,
                maxLines = 1
            )
            SpacerSmall()
            ErezeptText.Subtitle(stringResource(R.string.gid_gematik_error_timestamp))
            ErezeptText.Body(
                text = error.gematikTimestamp,
                maxLines = 1
            )
            SpacerSmall()
        },
        onDismissRequest = onDismissRequest
    )
}

@LightDarkPreview
@Composable
fun GematikErrorDialogPreview() {
    PreviewAppTheme {
        GematikErrorDialog(error = emptyResponseError()) {}
    }
}
