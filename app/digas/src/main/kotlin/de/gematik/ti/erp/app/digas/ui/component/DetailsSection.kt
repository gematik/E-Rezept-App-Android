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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.digas.ui.model.DigaMainScreenUiModel
import de.gematik.ti.erp.app.error.ErrorScreenComponent
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.Banner
import de.gematik.ti.erp.app.utils.compose.BannerClickableIcon
import de.gematik.ti.erp.app.utils.compose.BannerIcon
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.annotatedLinkUnderlined
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.format
import de.gematik.ti.erp.app.utils.uistate.UiState

fun LazyListScope.detailSection(
    uiState: UiState<DigaMainScreenUiModel>,
    isBframReachable: Boolean,
    errorTitle: String,
    errorBody: String,
    onNavigateToPatient: () -> Unit = {},
    onNavigateTopPractitioner: () -> Unit = {},
    onNavigateTopOrganization: () -> Unit = {},
    onNavigateToBafim: () -> Unit = {},
    onNavigateToTechnicalInformation: () -> Unit = {}
) {
    item {
        Column(
            modifier = Modifier.padding(bottom = PaddingDefaults.XXLargeMedium)
        ) {
            UiStateMachine(
                state = uiState,
                onLoading = {
                    DetailLoadingSection()
                },
                onEmpty = {
                    Column(
                        modifier = Modifier.fillParentMaxSize()
                    ) {
                        Center {
                            ErrorScreenComponent(
                                titleText = errorTitle,
                                bodyText = errorBody
                            )
                        }
                    }
                },
                onError = {
                    Column(
                        modifier = Modifier.fillParentMaxSize()
                    ) {
                        Center {
                            ErrorScreenComponent(
                                titleText = errorTitle,
                                bodyText = errorBody
                            )
                        }
                    }
                }
            ) { state ->

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PaddingDefaults.Medium)
                ) {
                    SpacerMedium()
                    val annotatedText = annotatedLinkUnderlined(
                        fullText = stringResource(R.string.bfarm_info),
                        clickableText = stringResource(R.string.diga_bfarm_url),
                        tag = "DigaBfarmClick",
                        textColor = AppTheme.colors.yellow900
                    )

                    if (isBframReachable) {
                        Banner(
                            modifier = Modifier.clickable { onNavigateToBafim() },
                            annotatedText = annotatedText,
                            contentColor = AppTheme.colors.yellow900,
                            containerColor = AppTheme.colors.yellow100,
                            borderColor = AppTheme.colors.yellow900,
                            startIcon = BannerClickableIcon(BannerIcon.Warning) {}
                        )
                    }
                    SpacerMedium()
                    Label(
                        label = stringResource(R.string.digital_health_application),
                        text = state.name,
                        setHorizontalPadding = false
                    )
                    if (isBframReachable) {
                        Label(
                            label = stringResource(R.string.available_languages),
                            text = state.languages?.joinToString(),
                            setHorizontalPadding = false
                        )
                        Label(
                            label = stringResource(R.string.platforms),
                            text = state.supportedPlatforms?.joinToString(),
                            setHorizontalPadding = false,
                            onClick = {}
                        )
                        Label(
                            label = stringResource(R.string.medical_services_required),
                            text = if (state.medicalServicesRequired) stringResource(R.string.diga_yes) else stringResource(R.string.diga_no),
                            setHorizontalPadding = false
                        )
                        Label(
                            label = stringResource(R.string.additional_devices),
                            text = state.additionalDevices,
                            setHorizontalPadding = false,
                            onClick = state.additionalDevices?.let { {} }
                        )
                        Label(
                            label = stringResource(R.string.your_fee),
                            text = state.fee?.let { stringResource(R.string.invoice_details_cost, it) },
                            setHorizontalPadding = false,
                            onClick = state.fee?.let { {} }
                        )
                        Label(
                            label = stringResource(R.string.manufacturer_costs),
                            text = state.cost?.let { stringResource(R.string.invoice_details_cost, it) },
                            setHorizontalPadding = false
                        )
                    }
                    Label(
                        label = stringResource(R.string.insured_person),
                        text = state.insuredPerson,
                        setHorizontalPadding = false,
                        onClick = onNavigateToPatient
                    )
                    Label(
                        label = stringResource(R.string.prescribing_person),
                        text = state.prescribingPerson,
                        setHorizontalPadding = false,
                        onClick = onNavigateTopPractitioner
                    )
                    Label(
                        label = stringResource(R.string.institution),
                        text = state.institution,
                        setHorizontalPadding = false,
                        onClick = onNavigateTopOrganization
                    )
                    Label(
                        label = stringResource(R.string.date_of_issue),
                        text = state.lifeCycleTimestamps.issuedOn?.format(),
                        setHorizontalPadding = false,
                        onClick = state.lifeCycleTimestamps.issuedOn?.let { {} }
                    )
                    Label(
                        text = stringResource(R.string.pres_detail_technical_information),
                        setHorizontalPadding = false,
                        onClick = onNavigateToTechnicalInformation
                    )
                    SpacerXXLarge()
                }
            }
        }
    }
}
