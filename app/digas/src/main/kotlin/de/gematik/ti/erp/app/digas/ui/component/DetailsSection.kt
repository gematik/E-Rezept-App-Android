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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.digas.data.model.AdditionalDeviceStatus
import de.gematik.ti.erp.app.digas.ui.model.DigaBfarmUiModel
import de.gematik.ti.erp.app.digas.ui.model.DigaMainScreenUiModel
import de.gematik.ti.erp.app.error.ErrorScreenComponent
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.Banner
import de.gematik.ti.erp.app.utils.compose.BannerClickableIcon
import de.gematik.ti.erp.app.utils.compose.BannerIcon
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.annotatedLinkUnderlined
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.format
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isEmptyState

fun LazyListScope.detailSection(
    uiState: UiState<DigaMainScreenUiModel>,
    uiStateBfarm: UiState<DigaBfarmUiModel>,
    errorTitle: String,
    errorBody: String,
    onNavigateToPatient: () -> Unit = {},
    onNavigateTopPractitioner: () -> Unit = {},
    onNavigateTopOrganization: () -> Unit = {},
    onNavigateToBafim: () -> Unit = {},
    onNavigateToTechnicalInformation: () -> Unit = {},
    navigateToContributionInfo: () -> Unit = {}
) {
    item {
        Column(
            modifier = Modifier
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
                    AnimatedVisibility(
                        visible = uiStateBfarm.isEmptyState,
                        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top) + slideInVertically(),
                        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top) + slideOutVertically()
                    ) {
                        Banner(
                            modifier = Modifier.clickable { onNavigateToBafim() },
                            text = annotatedText.text,
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
                    DetailBfarmSection(uiStateBfarm, navigateToContributionInfo)

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
                }
            }
        }
    }
}

@Composable
private fun DetailBfarmSection(
    state: UiState<DigaBfarmUiModel>,
    navigateToContributionInfo: () -> Unit
) {
    UiStateMachine(
        state = state,
        onLoading = {
            DetailBfarmLoadingSection()
        },
        onEmpty = {
            BfarmSection(
                additionalDevicesText = stringResource(R.string.pres_details_no_value),
                contractMedicalServicesRequired = stringResource(R.string.pres_details_no_value),
                yourFee = stringResource(R.string.pres_details_no_value),
                navigateToContributionInfo = null
            )
        }
    ) { state ->
        val statusMap = mapOf(
            AdditionalDeviceStatus.OPTIONAL to stringResource(R.string.diga_additional_devices_possible),
            AdditionalDeviceStatus.REQUIRED to stringResource(R.string.diga_additional_devices_required),
            AdditionalDeviceStatus.INCLUDED to stringResource(R.string.diga_additional_devices_included)
        )
        val additionalDevicesText = state.additionalDevicesRequired
            ?.takeIf { it.isNotEmpty() }
            ?.mapNotNull { statusMap[it] }
            ?.joinToString("\n")

        BfarmSection(
            languages = state.languages?.joinToString(),
            supportedPlatforms = state.supportedPlatforms,
            contractMedicalServicesRequired = if (state.contractMedicalServicesRequired) {
                stringResource(R.string.diga_yes)
            } else {
                stringResource(R.string.diga_no)
            },
            maxCost = state.maxCost,
            additionalDevicesText = additionalDevicesText,
            navigateToContributionInfo = navigateToContributionInfo
        )
    }
}

@Composable
private fun BfarmSection(
    languages: String? = null,
    supportedPlatforms: String? = null,
    contractMedicalServicesRequired: String? = null,
    yourFee: String? = stringResource(R.string.invoice_details_cost, "0"),
    maxCost: String? = null,
    additionalDevicesText: String? = null,
    navigateToContributionInfo: (() -> Unit)?
) {
    Column {
        Label(
            label = stringResource(R.string.available_languages),
            text = languages,
            setHorizontalPadding = false
        )
        Label(
            label = stringResource(R.string.platforms),
            text = supportedPlatforms,
            setHorizontalPadding = false
        )
        Label(
            label = stringResource(R.string.medical_services_required),
            text = contractMedicalServicesRequired,
            setHorizontalPadding = false
        )
        Label(
            label = stringResource(R.string.additional_devices),
            text = additionalDevicesText ?: stringResource(R.string.diga_no_additional_devices_required),
            setHorizontalPadding = false
        )
        Label(
            label = stringResource(R.string.your_fee),
            text = yourFee,
            setHorizontalPadding = false,
            onClick = navigateToContributionInfo,
            imageVector = Icons.Outlined.Info,
            iconContentDescription = "",
            iconTint = AppTheme.colors.primary700
        )
        Label(
            label = stringResource(R.string.manufacturer_costs),
            text = maxCost?.let { stringResource(R.string.invoice_details_cost, it) },
            setHorizontalPadding = false
        )
    }
}
