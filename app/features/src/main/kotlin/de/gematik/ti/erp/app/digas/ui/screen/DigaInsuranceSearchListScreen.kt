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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.digas.presentation.DigasGraphController
import de.gematik.ti.erp.app.digas.presentation.rememberInsuranceListController
import de.gematik.ti.erp.app.digas.ui.components.InsuranceListItem
import de.gematik.ti.erp.app.digas.ui.components.InsuranceListLoadingSection
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
import de.gematik.ti.erp.app.topbar.AnimatedTitleContent
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.EmptyScreenComponent
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.animatedElevationStickySearchField
import de.gematik.ti.erp.app.utils.compose.preview.NamedPreviewBox
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.uistate.UiState

class DigaInsuranceSearchListScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val sharedViewModel: DigasGraphController,
    private val errorScreenData: ErrorScreenData
) : Screen() {
    @Composable
    override fun Content() {
        val focusManager = LocalFocusManager.current

        val searchListController = rememberInsuranceListController()
        val searchFieldValue by searchListController.searchFieldValue.collectAsStateWithLifecycle()
        val insuranceProvidersList by searchListController.healthInsuranceList.collectAsStateWithLifecycle()

        val lazyListState = rememberLazyListState()

        val onBack: () -> Unit = { navController.popBackStack() }

        val onSelectInsuranceProvider: (PharmacyUseCaseData.Pharmacy) -> Unit = { insuranceProvider ->
            sharedViewModel.updateInsuranceInfo(insuranceProvider.telematikId, insuranceProvider.name)
            navController.popBackStack()
        }

        InsuranceSearchListScreenContent(
            searchFieldValue = searchFieldValue,
            errorScreenData = errorScreenData,
            focusManager = focusManager,
            lazyListState = lazyListState,
            uiState = insuranceProvidersList,
            onSearchInputChange = {
                searchListController.onSearchFieldValueChange(it)
            },
            onRemoveSearchInput = { searchListController.onRemoveSearchFieldValue() },
            onSelectInsuranceProvider = onSelectInsuranceProvider,
            onBack = onBack
        )
    }
}

@Composable
private fun InsuranceSearchListScreenContent(
    searchFieldValue: String,
    onSearchInputChange: (String) -> Unit,
    onRemoveSearchInput: () -> Unit,
    errorScreenData: ErrorScreenData,
    focusManager: FocusManager,
    lazyListState: LazyListState,
    uiState: UiState<List<InsuranceUiModel>>,
    onSelectInsuranceProvider: (PharmacyUseCaseData.Pharmacy) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            NavigationTopAppBar(
                navigationMode = NavigationBarMode.Back,
                onBack = onBack,
                elevation = SizeDefaults.zero,
                backLabel = stringResource(R.string.back),
                closeLabel = stringResource(R.string.cancel),
                title = {
                    AnimatedTitleContent(
                        listState = lazyListState,
                        title = stringResource(R.string.diga_select_insurance_for_diga_header)
                    )
                }
            )
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
            onRemoveSearchInput = onRemoveSearchInput,
            onSelectInsuranceProvider = onSelectInsuranceProvider
        )
    }
}

@Composable
private fun InsuranceSearchSection(
    contentPadding: PaddingValues,
    lazyListState: LazyListState,
    errorScreenData: ErrorScreenData,
    uiState: UiState<List<InsuranceUiModel>>,
    onSelectInsuranceProvider: (PharmacyUseCaseData.Pharmacy) -> Unit,
    searchFieldValue: String,
    onSearchInputChange: (String) -> Unit,
    onRemoveSearchInput: () -> Unit,
    focusManager: FocusManager
) {
    val description = stringResource(id = R.string.diga_insurance_searchbar)
    val placeholderText = stringResource(id = R.string.search_pharmacies_start_search)
    val contentDescriptionText = stringResource(id = R.string.a11y_deleted_text)
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
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
            )
        }
        item {
            SpacerSmall()
            Text(
                text = stringResource(R.string.diga_select_insurance_for_diga_discription),
                style = AppTheme.typography.body2,
                color = AppTheme.colors.neutral600,
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
            )
            SpacerMedium()
        }
        animatedElevationStickySearchField(
            lazyListState = lazyListState,
            indexOfPreviousItemInList = 1,
            value = searchFieldValue,
            onValueChange = onSearchInputChange,
            onRemoveValue = onRemoveSearchInput,
            focusManager = focusManager,
            description = description,
            placeholderText = placeholderText,
            contentDescriptionText = contentDescriptionText
        )
        item {
            UiStateMachine(
                state = uiState,
                onLoading = { InsuranceListLoadingSection() },
                onEmpty = {
                    EmptyScreenComponent(
                        modifier = Modifier.padding(top = PaddingDefaults.XXLarge),
                        title = stringResource(R.string.diga_insurance_search_empty_title),
                        body = stringResource(R.string.diga_insurance_search_empty_text),
                        image = {
                            Image(
                                painter = painterResource(id = R.drawable.girl_red_oh_no),
                                contentDescription = null,
                                modifier = Modifier.size(SizeDefaults.twentyfold)
                            )
                        },
                        button = {}
                    )
                },
                onError = {
                    ErrorScreenComponent(
                        modifier = Modifier.padding(top = PaddingDefaults.XXLarge),
                        titleText = stringResource(errorScreenData.title),
                        bodyText = stringResource(errorScreenData.body)
                    )
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
            val searchFieldValue by remember { mutableStateOf("") }

            InsuranceSearchListScreenContent(
                searchFieldValue = searchFieldValue,
                focusManager = focusManager,
                lazyListState = lazyListState,
                uiState = previewData.uiState,
                onSearchInputChange = { _ ->
                },
                onSelectInsuranceProvider = {},
                errorScreenData = ErrorScreenDataWithoutRetry(),
                onRemoveSearchInput = {},
                onBack = {}
            )
        }
    }
}
