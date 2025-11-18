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

package de.gematik.ti.erp.app.digas.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.diga.model.DigaStatus
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.ClickText
import de.gematik.ti.erp.app.utils.ClickableText
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty

@Composable
fun ActionSection(
    step: DigaStatus,
    insuranceName: String?,
    isLoadingTask: Boolean,
    isArchived: Boolean,
    onClickOnReady: () -> Unit,
    onClickOnCompletedSuccessfully: () -> Unit,
    onClickOnOpenAppWithRedeemCode: () -> Unit,
    onClickOnReadyForSelfArchive: () -> Unit,
    onClickOnDigaOpen: () -> Unit,
    onClickOnRevertArchive: () -> Unit,
    onClickOnNavigateToInsuranceSearch: () -> Unit
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
            when {
                isLoadingTask -> {
                    DigaPrimaryButton(
                        text = stringResource(R.string.diga_request_unlock_code),
                        onClick = onClickOnReady,
                        enabled = false
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PaddingDefaults.Medium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row {
                            Text(
                                text = stringResource(R.string.diga_Insurance_is_determined),
                                style = AppTheme.typography.caption1,
                                textAlign = TextAlign.Center
                            )

                            CircularProgressIndicator(
                                modifier = Modifier.size(SizeDefaults.double)
                            )
                        }
                    }
                }

                insuranceName.isNotNullOrEmpty() -> {
                    DigaPrimaryButton(
                        text = stringResource(R.string.diga_request_unlock_code),
                        onClick = onClickOnReady
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PaddingDefaults.Medium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ClickableText(
                            modifier = Modifier
                                .padding(horizontal = PaddingDefaults.Large)
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = stringResource(R.string.a11y_diga_change_insurance)
                                ) { onClickOnNavigateToInsuranceSearch() },
                            text = stringResource(R.string.diga_requesting_code_from, insuranceName ?: ""),
                            textStyle = AppTheme.typography.caption1l.copy(textAlign = TextAlign.Center),
                            clickText = ClickText(
                                text = insuranceName ?: "",
                                onClick = onClickOnNavigateToInsuranceSearch
                            )
                        )
                    }
                }

                else -> {
                    DigaPrimaryButton(
                        text = stringResource(R.string.diga_insurance_selection),
                        onClick = onClickOnNavigateToInsuranceSearch
                    )
                }
            }
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
