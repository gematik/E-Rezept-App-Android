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

package de.gematik.ti.erp.app.digas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.digas.ui.component.CustomSegmentedButton
import de.gematik.ti.erp.app.digas.ui.component.HeaderSection
import de.gematik.ti.erp.app.digas.ui.component.detailSection
import de.gematik.ti.erp.app.digas.ui.component.overviewSection
import de.gematik.ti.erp.app.digas.ui.model.DigaMainScreenUiModel
import de.gematik.ti.erp.app.digas.ui.model.DigaSegmentedControllerTap
import de.gematik.ti.erp.app.digas.ui.model.DigasActions
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigaContent(
    listState: LazyListState = rememberLazyListState(),
    isBfarmReachable: Boolean,
    errorTitle: String,
    errorBody: String,
    isDownloading: Boolean,
    uiState: UiState<DigaMainScreenUiModel>,
    lastRefreshedTime: Instant,
    selectedTab: DigaSegmentedControllerTap,
    actions: DigasActions,
    onTabChange: (Int) -> Unit
) {
    val options = listOf(stringResource(R.string.diga_overview), stringResource(R.string.diga_details))
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.Top
    ) {
        item {
            HeaderSection(data = uiState)
        }
        item {
            SpacerLarge()
        }
        item {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PaddingDefaults.Medium)
            ) {
                options.forEachIndexed { index, label ->
                    CustomSegmentedButton(
                        index = index,
                        options = options,
                        selectedIndex = selectedTab.index,
                        label = label,
                        onClick = { onTabChange(index) }
                    )
                }
            }
        }

        when (selectedTab) {
            DigaSegmentedControllerTap.OVERVIEW -> overviewSection(
                uiState = uiState,
                isDownloading = isDownloading,
                isBframReachable = isBfarmReachable,
                errorTitle = errorTitle,
                errorBody = errorBody,
                lastRefreshedTime = lastRefreshedTime,
                actions = actions
            )

            DigaSegmentedControllerTap.DETAIL -> detailSection(
                uiState = uiState,
                isBframReachable = isBfarmReachable,
                errorTitle = errorTitle,
                errorBody = errorBody,
                onNavigateToPatient = actions.onNavigateToPatient,
                onNavigateTopPractitioner = actions.onNavigateToPractitioner,
                onNavigateTopOrganization = actions.onNavigateToOrganization,
                onNavigateToTechnicalInformation = actions.onNavigateToTechnicalInformation,
                onNavigateToBafim = actions.onNavigatetoBfarm
            )
        }
    }
}
