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

package de.gematik.ti.erp.app.digas.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.digas.presentation.DigasGraphController
import de.gematik.ti.erp.app.digas.presentation.rememberInsuranceListController
import de.gematik.ti.erp.app.digas.ui.components.BottomEdgeShape
import de.gematik.ti.erp.app.digas.ui.components.InsuranceListItem
import de.gematik.ti.erp.app.digas.ui.components.InsuranceListLoadingSection
import de.gematik.ti.erp.app.digas.ui.components.SearchInputField
import de.gematik.ti.erp.app.digas.ui.model.ErrorScreenData
import de.gematik.ti.erp.app.digas.ui.model.ErrorScreenDataWithoutRetry
import de.gematik.ti.erp.app.digas.ui.model.InsuranceUiModel
import de.gematik.ti.erp.app.digas.ui.preview.InsuranceSearchListPreviewData
import de.gematik.ti.erp.app.digas.ui.preview.InsuranceSearchListPreviewParameterProvider
import de.gematik.ti.erp.app.error.ErrorScreenComponent
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerXLarge
import de.gematik.ti.erp.app.utils.compose.NoElevationScaffold
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.preview.NamedPreviewBox
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.uistate.UiState

class InsuranceSearchListScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: DigasGraphController,
    private val errorScreenData: ErrorScreenData
) : Screen() {
    @Composable
    override fun Content() {
        val focusManager = LocalFocusManager.current

        var searchFieldValue by remember { mutableStateOf(TextFieldValue("")) }

        val searchListController = rememberInsuranceListController(searchFieldValue.text)
        val insuranceProvidersList by searchListController.insurancesState

        val lazyListState = rememberLazyListState()

        val isLoading by remember { mutableStateOf(false) }

        val onBack: () -> Unit = { navController.popBackStack() }

        val onSelectInsuranceProvider: (PharmacyUseCaseData.Pharmacy) -> Unit = { insuranceProvider ->
            graphController.updateInsuranceInfo(insuranceProvider.telematikId, insuranceProvider.name)
            navController.popBackStack()
        }

        InsuranceSearchListScreenContent(
            searchFieldValue = searchFieldValue,
            isLoading = isLoading,
            errorScreenData = errorScreenData,
            focusManager = focusManager,
            lazyListState = lazyListState,
            uiState = insuranceProvidersList,
            onSearchInputChange = {
                searchFieldValue = it.copy(selection = TextRange(it.text.length))
                searchListController.onSearchFieldValue(it.text)
            },
            onSelectInsuranceProvider = onSelectInsuranceProvider,
            onBack = onBack
        )
    }
}

@Composable
private fun InsuranceSearchListScreenContent(
    searchFieldValue: TextFieldValue,
    onSearchInputChange: (TextFieldValue) -> Unit,
    isLoading: Boolean,
    errorScreenData: ErrorScreenData,
    focusManager: FocusManager,
    lazyListState: LazyListState,
    uiState: UiState<List<InsuranceUiModel>>,
    onSelectInsuranceProvider: (PharmacyUseCaseData.Pharmacy) -> Unit,
    onBack: () -> Unit
) {
    NoElevationScaffold(
        listState = lazyListState,
        topBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(end = PaddingDefaults.Medium),
                horizontalAlignment = Alignment.End
            ) {
                TextButton(onClick = onBack) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    ) { contentPadding ->
        InsuranceSearchSection(
            contentPadding = contentPadding,
            lazyListState = lazyListState,
            uiState = uiState,
            errorScreenData = errorScreenData,
            searchFieldValue = searchFieldValue,
            focusManager = focusManager,
            onSearchInputChange = onSearchInputChange,
            isLoading = isLoading,
            onBack = onBack,
            onSelectInsuranceProvider = onSelectInsuranceProvider
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InsuranceSearchSection(
    contentPadding: PaddingValues,
    lazyListState: LazyListState,
    errorScreenData: ErrorScreenData,
    uiState: UiState<List<InsuranceUiModel>>,
    onSelectInsuranceProvider: (PharmacyUseCaseData.Pharmacy) -> Unit,
    searchFieldValue: TextFieldValue,
    onSearchInputChange: (TextFieldValue) -> Unit,
    isLoading: Boolean,
    focusManager: FocusManager,
    onBack: () -> Unit
) {
    val isHeaderElevated by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex != 0 }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTag.Settings.InsuranceCompanyList.InsuranceSelectionContent),
        state = lazyListState,
        contentPadding = contentPadding
    ) {
        item {
            SpacerSmall()
            Text(
                text = stringResource(R.string.diga_select_insurance_for_diga_header),
                style = AppTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.neutral900,
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
            )
            SpacerSmall()
            Text(
                text = stringResource(R.string.diga_select_insurance_for_diga_discription),
                style = AppTheme.typography.body2,
                color = AppTheme.colors.neutral600,
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
            )
            SpacerXLarge()
        }
        stickyHeader {
            Surface(
                elevation = when {
                    isHeaderElevated -> AppBarDefaults.TopAppBarElevation
                    else -> SizeDefaults.zero
                },
                shape = BottomEdgeShape()
            ) {
                SearchInputField(
                    searchValue = searchFieldValue,
                    focusManager = focusManager,
                    onSearchInputChange = onSearchInputChange,
                    isLoading = isLoading,
                    onBack = onBack
                )
            }
        }
        item {
            UiStateMachine(
                state = uiState,
                onLoading = { InsuranceListLoadingSection() },
                onEmpty = {
                    Column(
                        modifier = Modifier.fillParentMaxSize()
                    ) {
                        Center {
                            ErrorScreenComponent(
                                titleText = stringResource(R.string.diga_insurance_search_empty_title),
                                bodyText = stringResource(R.string.diga_insurance_search_empty_text)
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
                                titleText = stringResource(errorScreenData.title),
                                bodyText = stringResource(errorScreenData.body)
                            )
                        }
                    }
                }
            ) { state ->
                Column {
                    state.forEach { insuranceUiModel ->
                        InsuranceListItem(
                            insuranceUiModel = insuranceUiModel,
                            onSelectInsurance = onSelectInsuranceProvider
                        )
                    }
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
fun InsuranceSearchListScreenPreview(
    @PreviewParameter(InsuranceSearchListPreviewParameterProvider::class) previewData: InsuranceSearchListPreviewData
) {
    PreviewAppTheme {
        NamedPreviewBox(name = previewData.name) {
            val focusManager = LocalFocusManager.current
            val lazyListState = rememberLazyListState()
            var searchFieldValue by remember { mutableStateOf(TextFieldValue(previewData.searchTerm)) }
            val isLoading = previewData.name == "Loading state"

            InsuranceSearchListScreenContent(
                searchFieldValue = searchFieldValue,
                isLoading = isLoading,
                focusManager = focusManager,
                lazyListState = lazyListState,
                uiState = previewData.uiState,
                onSearchInputChange = {
                    searchFieldValue = it.copy(selection = TextRange(it.text.length))
                },
                onSelectInsuranceProvider = {},
                errorScreenData = ErrorScreenDataWithoutRetry(),
                onBack = {}
            )
        }
    }
}
