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

package de.gematik.ti.erp.app.digas.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.fhir.model.DigaStatus

@Composable
fun ActionSection(
    step: DigaStatus,
    isArchived: Boolean,
    onClickOnReady: () -> Unit,
    onClickOnCompletedSuccessfully: () -> Unit,
    onClickOnOpenAppWithRedeemCode: () -> Unit,
    onClickOnReadyForSelfArchive: () -> Unit,
    onClickOnDigaOpen: () -> Unit,
    onClickOnRevertArchive: () -> Unit
) {
    when {
        isArchived -> {
            DigaPrimaryButton(
                text = stringResource(R.string.archive_revert),
                onClick = onClickOnRevertArchive
            )
        }

        step == DigaStatus.SelfArchiveDiga -> {
            DigaPrimaryButton(
                text = stringResource(R.string.open_diga_app),
                onClick = onClickOnDigaOpen
            )
        }

        step == DigaStatus.Ready -> {
            DigaPrimaryButton(
                text = stringResource(R.string.diga_request_unlock_code),
                onClick = onClickOnReady
            )
        }

        step == DigaStatus.DownloadDigaApp ||
            step == DigaStatus.CompletedSuccessfully -> {
            DigaPrimaryButton(
                text = stringResource(R.string.download_diga_app),
                onClick = onClickOnCompletedSuccessfully
            )
        }

        step == DigaStatus.OpenAppWithRedeemCode -> {
            DigaPrimaryButton(
                text = stringResource(R.string.activate_diga_app),
                onClick = onClickOnOpenAppWithRedeemCode
            )
        }

        step == DigaStatus.ReadyForSelfArchiveDiga -> {
            DigaPrimaryButton(
                text = stringResource(R.string.archive_prescription),
                onClick = onClickOnReadyForSelfArchive
            )
        }

        else -> {
            // do nothing
        }
    }
}
