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

package de.gematik.ti.erp.app.cardwall.ui.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.idp.model.error.GematikResponseError
import de.gematik.ti.erp.app.idp.model.error.GematikResponseError.Companion.prettyErrorCode
import de.gematik.ti.erp.app.idp.model.error.GematikResponseError.Companion.prettyErrorText
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.ErezeptText
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

private const val MARQUEE_DELAY = 2000

@Suppress("MagicNumber")
@Composable
fun GematikErrorDialog(
    error: GematikResponseError,
    onDismissRequest: () -> Unit
) {
    ErezeptAlertDialog(
        modifier = Modifier.fillMaxWidth(),
        title = stringResource(R.string.main_fasttrack_error_title),
        body = {
            ErezeptText.SubtitleOne(stringResource(R.string.gid_gematik_error_title))
            SpacerMedium()

            Column(
                modifier = Modifier
                    .padding(PaddingDefaults.Small)
            ) {
                ErezeptText.SubtitleTwo(text = stringResource(R.string.gid_gematik_error_subtitle))
                ErezeptText.Body(error.prettyErrorCode())
                ErezeptText.Body(error.prettyErrorText())
                SpacerSmall()
                ErezeptText.SubtitleTwo(stringResource(R.string.gid_gematik_error_code))
                ErezeptText.Body(error.gematikCode)
                SpacerSmall()
                ErezeptText.SubtitleTwo(stringResource(R.string.gid_gematik_error_uuid))
                ErezeptText.Body(
                    modifier = Modifier.basicMarquee(repeatDelayMillis = MARQUEE_DELAY),
                    text = error.gematikUuid,
                    maxLines = 1
                )
                SpacerSmall()
                ErezeptText.SubtitleTwo(stringResource(R.string.gid_gematik_error_timestamp))
                ErezeptText.Body(
                    text = error.gematikTimestamp,
                    maxLines = 1
                )
                SpacerSmall()
            }
        },
        onDismissRequest = onDismissRequest
    )
}

@LightDarkPreview
@Composable
fun GematikErrorDialogPreview() {
    PreviewAppTheme {
        GematikErrorDialog(
            error = GematikResponseError(
                error = "Invalid request",
                gematikCode = "7014",
                gematikUuid = "f7b3b3b3-3b3b-3b3b",
                gematikTimestamp = "173970246",
                gematikErrorText = "kein Entity statement für idp_iss vorhanden"
            )
        ) {}
    }
}
