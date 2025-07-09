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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Divider
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.digas.ui.model.DigaMainScreenUiModel
import de.gematik.ti.erp.app.digas.ui.model.DigasActions
import de.gematik.ti.erp.app.error.ErrorScreenComponent
import de.gematik.ti.erp.app.fhir.model.DigaStatus
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Instant

fun LazyListScope.overviewSection(
    modifier: Modifier = Modifier,
    uiState: UiState<DigaMainScreenUiModel>,
    lastRefreshedTime: Instant,
    isDownloading: Boolean,
    isBframReachable: Boolean,
    errorTitle: String,
    errorBody: String,
    actions: DigasActions
) {
    item {
        Column(
            modifier = Modifier
                .fillParentMaxSize()
                .padding(bottom = PaddingDefaults.XLarge)
        ) {
            UiStateMachine(
                state = uiState,
                onLoading = {
                    OverviewLoadingSection()
                },
                onEmpty = {
                    ErrorScreenComponent(
                        titleText = errorTitle,
                        bodyText = errorBody
                    )
                },
                onError = {
                    ErrorScreenComponent(
                        titleText = errorTitle,
                        bodyText = errorBody
                    )
                }
            ) { data ->
                val current = data.status
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = PaddingDefaults.Medium)
                ) {
                    SpacerMedium()
                    OverviewHeader(data)
                    SpacerLarge()
                    ProcessSection(
                        current = current,
                        code = data.code ?: "",
                        declineNote = data.declineNote,
                        onClickCopy = actions.onClickCopy,
                        onRegisterFeedback = actions.onRegisterFeedBack
                    )
                    if (current is DigaStatus.InProgress) {
                        OverviewWaitStateSection(
                            isDownloading = isDownloading,
                            lastRefreshedTime = lastRefreshedTime,
                            onRefresh = actions.onClickRefresh
                        )
                    }
                    SpacerLarge()
                    Divider(color = AppTheme.colors.neutral400, thickness = SizeDefaults.eighth)
                    SpacerMedium()
                    if (isBframReachable) {
                        OverviewDescriptionRow(actions.onNavigateToDescriptionScreen)
                        SpacerMedium()
                        if ((current.step) >= DigaStatus.CompletedSuccessfully.step || current is DigaStatus.CompletedWithRejection) {
                            OverviewSupportRow(actions.onShowSupportBottomSheet)
                            SpacerMedium()
                        }
                    }
                    OverviewExpirationInfo(
                        data = data,
                        current = current,
                        displayBottomSheet = actions.onShowHowLongValidBottomSheet
                    )
                }
            }
        }
    }
}
