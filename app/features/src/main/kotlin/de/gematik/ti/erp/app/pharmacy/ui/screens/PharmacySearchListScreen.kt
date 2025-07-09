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

package de.gematik.ti.erp.app.pharmacy.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes
import de.gematik.ti.erp.app.pharmacy.presentation.FilterType
import de.gematik.ti.erp.app.pharmacy.presentation.PharmacyGraphController
import de.gematik.ti.erp.app.pharmacy.presentation.WILDCARD
import de.gematik.ti.erp.app.pharmacy.presentation.rememberPharmacySearchListController
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyScreen
import de.gematik.ti.erp.app.pharmacy.ui.components.FilterButtonSection
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacyFullScreenSearchLoading
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacyMapButton
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacyResultCard
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacySearchErrorHint
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacySearchLoading
import de.gematik.ti.erp.app.pharmacy.ui.preview.PharmacyPreviewData
import de.gematik.ti.erp.app.pharmacy.ui.preview.PharmacySearchListScreenPreviewData
import de.gematik.ti.erp.app.pharmacy.ui.preview.PharmacySearchListScreenPreviewParameterProvider
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacyId
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.BorderDivider
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import kotlinx.coroutines.delay

@Requirement(
    "A_20285#1",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Search results are displayed solely based on search term and user-set filters."
)
class PharmacySearchListScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: PharmacyGraphController
) : PharmacyScreen() {
    @Composable
    override fun Content() {
        val filter by graphController.filter()
        val focusManager = LocalFocusManager.current
        val location by graphController.coordinates()

        var searchTerm by remember { mutableStateOf(TextFieldValue(WILDCARD)) }

        var isAtLeastOnePharmacyLoaded by remember { mutableStateOf(false) }

        val searchListController = rememberPharmacySearchListController(
            filter,
            location,
            searchTerm.text
        )

        val searchParam by searchListController.searchParamState

        val searchPagingItems = searchListController.pharmaciesState

        val lazyListState = rememberLazyListState()

        val isLoading by remember {
            derivedStateOf {
                searchPagingItems.loadState.append is LoadState.Loading ||
                    searchPagingItems.loadState.prepend is LoadState.Loading
            }
        }

        val onBack: () -> Unit = { navController.popBackStack() }

        PharmacySearchListScreenContent(
            searchPagingItems = searchPagingItems,
            searchTerm = searchTerm,
            filter = searchParam.filter,
            isLoading = isLoading,
            focusManager = focusManager,
            lazyListState = lazyListState,
            isAtLeastOnePharmacyLoaded = isAtLeastOnePharmacyLoaded,
            onSearchInputChange = {
                searchTerm = it.copy(selection = TextRange(it.text.length))
                searchListController.onSearchTerm(it.text)
            },
            onClickChip = { _, selectedFilter ->
                searchListController.onFilter(selectedFilter) {
                    graphController.updateFilter(it, clearLocation = (selectedFilter == FilterType.NEARBY))
                }
            },
            onClickPharmacy = { pharmacy ->
                navController.navigate(
                    PharmacyRoutes.PharmacyDetailsFromPharmacyScreen.path(
                        pharmacy = pharmacy,
                        taskId = navBackStackEntry.arguments?.getString(PharmacyRoutes.PHARMACY_NAV_TASK_ID) ?: ""
                    )
                )
            },
            onClickFilter = {
                navController.navigate(
                    PharmacyRoutes.PharmacyFilterSheetScreen.path(
                        showNearbyFilter = true,
                        navigateWithSearchButton = false
                    )
                )
            },
            onPharmacyLoaded = { isAtLeastOnePharmacyLoaded = true },
            onBack = onBack,
            onClickMaps = {
                navController.navigate(
                    PharmacyRoutes.PharmacySearchMapsScreen.path(
                        taskId = navBackStackEntry.arguments?.getString(PharmacyRoutes.PHARMACY_NAV_TASK_ID) ?: ""
                    )
                )
            }
        )
    }
}

@Composable
private fun PharmacySearchListScreenContent(
    searchTerm: TextFieldValue,
    onSearchInputChange: (TextFieldValue) -> Unit,
    filter: PharmacyUseCaseData.Filter,
    isAtLeastOnePharmacyLoaded: Boolean,
    isLoading: Boolean,
    focusManager: FocusManager,
    lazyListState: LazyListState,
    onPharmacyLoaded: () -> Unit,
    onClickChip: (Boolean, FilterType) -> Unit,
    onClickPharmacy: (PharmacyUseCaseData.Pharmacy) -> Unit,
    searchPagingItems: LazyPagingItems<PharmacyUseCaseData.Pharmacy>,
    onClickFilter: () -> Unit,
    onBack: () -> Unit,
    onClickMaps: () -> Unit
) {
    AnimatedElevationScaffold(
        listState = lazyListState,
        topBar = {
            Column {
                SearchTopAppBar(
                    searchValue = searchTerm,
                    focusManager = focusManager,
                    onSearchInputChange = onSearchInputChange,
                    isLoading = isLoading,
                    onBack = onBack
                )
                FilterButtonSection(
                    modifier = Modifier.padding(horizontal = PaddingDefaults.Small),
                    filter = filter,
                    onClickChip = onClickChip,
                    onClickFilter = onClickFilter
                )
            }
        },
        floatingActionButton = {
            PharmacyMapButton(onClick = onClickMaps)
        }
    ) { contentPadding ->
        SearchResults(
            contentPadding = contentPadding,
            searchPagingItems = searchPagingItems,
            isAtLeastOnePharmacyLoaded = isAtLeastOnePharmacyLoaded,
            lazyListState = lazyListState,
            onPharmacyLoaded = onPharmacyLoaded,
            onSelectPharmacy = onClickPharmacy
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(
    searchValue: TextFieldValue,
    onSearchInputChange: (TextFieldValue) -> Unit,
    isLoading: Boolean,
    focusManager: FocusManager,
    onBack: () -> Unit
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.colors.neutral000
        ),
        title = {
            Column {
                PharmacySearchInput(
                    modifier = Modifier.background(AppTheme.colors.neutral000),
                    onBack = onBack,
                    isLoading = isLoading,
                    focusManager = focusManager,
                    searchValue = searchValue,
                    onSearchInputChange = onSearchInputChange
                )
            }
        }
    )
}

@Composable
fun PharmacySearchInput(
    modifier: Modifier,
    isLoading: Boolean,
    searchValue: TextFieldValue,
    focusManager: FocusManager,
    onSearchInputChange: (TextFieldValue) -> Unit,
    onBack: () -> Unit
) {
    var isLoadingStable by remember { mutableStateOf(isLoading) }
    val description = stringResource(id = R.string.pharmacy_search_searchbar)
    LaunchedEffect(isLoading) {
        delay(timeMillis = 330)
        isLoadingStable = isLoading
    }

    TextField(
        value = searchValue,
        onValueChange = {
            onSearchInputChange(it)
        },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = description
            }
            .padding(horizontal = PaddingDefaults.Medium),
        keyboardOptions = KeyboardOptions(
            autoCorrect = true,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions {
            onSearchInputChange(searchValue)
            focusManager.clearFocus()
        },
        shape = RoundedCornerShape(SizeDefaults.double),
        textStyle = AppTheme.typography.body1,
        leadingIcon = {
            val contentDescription = stringResource(R.string.back)
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = contentDescription
                )
            }
        },
        trailingIcon = {
            Crossfade(
                isLoadingStable,
                animationSpec = tween(durationMillis = 550),
                label = "Search Loading Crossfade"
            ) {
                if (it) {
                    Box(Modifier.size(SizeDefaults.sixfold)) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(SizeDefaults.triple)
                                .align(Alignment.Center),
                            strokeWidth = SizeDefaults.quarter
                        )
                    }
                } else {
                    val contentDescription = stringResource(id = R.string.pharmacy_search_delete_button)
                    IconButton(
                        onClick = { onSearchInputChange(TextFieldValue(WILDCARD)) }
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = contentDescription
                        )
                    }
                }
            }
        },
        colors = TextFieldDefaults.textFieldColors(
            textColor = AppTheme.colors.neutral900,
            leadingIconColor = AppTheme.colors.neutral600,
            trailingIconColor = AppTheme.colors.neutral600,
            backgroundColor = AppTheme.colors.neutral100,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun SearchResults(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    isAtLeastOnePharmacyLoaded: Boolean,
    lazyListState: LazyListState,
    onPharmacyLoaded: () -> Unit,
    searchPagingItems: LazyPagingItems<PharmacyUseCaseData.Pharmacy>,
    onSelectPharmacy: (PharmacyUseCaseData.Pharmacy) -> Unit
) {
    val errorTitle = stringResource(R.string.search_pharmacy_error_title)
    val errorSubtitle = stringResource(R.string.search_pharmacy_error_subtitle)
    val errorAction = stringResource(R.string.search_pharmacy_error_action)

    val loadState = searchPagingItems.loadState

    val noPharmacies by remember(loadState, searchPagingItems.itemCount) {
        derivedStateOf {
            listOf(loadState.prepend, loadState.append)
                .all {
                    when (it) {
                        is LoadState.NotLoading ->
                            it.endOfPaginationReached && searchPagingItems.itemCount == 0

                        else -> false
                    }
                } && loadState.refresh is LoadState.NotLoading
        }
    }

    val showError by remember(loadState, searchPagingItems.itemCount) {
        derivedStateOf { searchPagingItems.itemCount <= 1 && loadState.hasError }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTag.PharmacySearch.ResultContent),
        state = lazyListState,
        contentPadding = contentPadding
    ) {
        if (noPharmacies) {
            NoPharmaciesFoundCard()
        }
        if (showError) {
            PharmacySearchErrorCard(
                isFullScreen = true,
                title = errorTitle,
                subtitle = errorSubtitle,
                action = errorAction,
                onClickAction = { searchPagingItems.retry() }
            )
        }
        items(
            count = searchPagingItems.itemCount,
            key = searchPagingItems.itemKey { it.telematikId }
        ) { index ->
            Crossfade(
                targetState = searchPagingItems[index],
                animationSpec = tween(durationMillis = 550),
                label = "Search Loading Crossfade"
            ) { pharmacy ->
                pharmacy?.let {
                    onPharmacyLoaded()
                    PharmacyListSearchResult(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(PaddingDefaults.Medium),
                        count = searchPagingItems.itemCount,
                        index = index,
                        pharmacy = pharmacy,
                        onSelectPharmacy = onSelectPharmacy
                    )
                }
            }
        }
        if (loadState.refresh is LoadState.Loading) {
            PharmacyFullScreenSearchLoading()
        }
        if (loadState.append is LoadState.Loading) {
            if (isAtLeastOnePharmacyLoaded) {
                PharmacySearchLoading()
            } else {
                PharmacyFullScreenSearchLoading()
            }
        }
    }
}

@Composable
private fun PharmacyListSearchResult(
    modifier: Modifier,
    count: Int,
    index: Int,
    pharmacy: PharmacyUseCaseData.Pharmacy,
    onSelectPharmacy: (PharmacyUseCaseData.Pharmacy) -> Unit
) {
    Column {
        PharmacyResultCard(
            modifier = modifier
                .semantics {
                    pharmacyId = pharmacy.telematikId
                }
                .testTag(TestTag.PharmacySearch.PharmacyListEntry),
            pharmacy = pharmacy
        ) {
            onSelectPharmacy(pharmacy)
        }
        BorderDivider()
        if (index < count - 1) {
            BorderDivider()
        }
    }
}

@Suppress("FunctionName")
private fun LazyListScope.NoPharmaciesFoundCard() {
    item {
        PharmacySearchErrorHint(
            title = stringResource(R.string.search_pharmacy_nothing_found_header),
            subtitle = stringResource(R.string.search_pharmacy_nothing_found_info),
            modifier = Modifier
                .fillMaxWidth()
                .fillParentMaxHeight()
                .imePadding()
        )
    }
}

@Suppress("FunctionName")
private fun LazyListScope.PharmacySearchErrorCard(
    title: String,
    subtitle: String,
    action: String?,
    onClickAction: () -> Unit,
    isFullScreen: Boolean = false
) {
    item {
        PharmacySearchErrorHint(
            title = title,
            subtitle = subtitle,
            action = action,
            onClickAction = onClickAction,
            modifier = Modifier
                .fillMaxWidth()
                .let {
                    if (isFullScreen) {
                        it.fillParentMaxHeight()
                    } else {
                        it.padding(PaddingDefaults.Medium)
                    }
                }
        )
    }
}

@LightDarkPreview
@Composable
fun PharmacySearchListScreenContentPreview(
    @PreviewParameter(
        PharmacySearchListScreenPreviewParameterProvider::class
    ) previewData: PharmacySearchListScreenPreviewData
) {
    PreviewAppTheme {
        val pagingItems = previewData.pagingData.collectAsLazyPagingItems()
        PharmacySearchListScreenContent(
            lazyListState = rememberLazyListState(),
            searchTerm = TextFieldValue(previewData.searchTerm),
            filter = previewData.filter,
            isLoading = previewData.isLoading,
            focusManager = LocalFocusManager.current,
            searchPagingItems = pagingItems,
            isAtLeastOnePharmacyLoaded = false,
            onSearchInputChange = {},
            onPharmacyLoaded = {},
            onClickChip = { _, _ -> },
            onClickPharmacy = {},
            onClickFilter = {},
            onBack = {},
            onClickMaps = {}
        )
    }
}

@LightDarkPreview
@Composable
fun PharmacyListSearchResultPreview() {
    PreviewAppTheme {
        Column(
            modifier = Modifier.padding(PaddingDefaults.Small)
        ) {
            PharmacyListSearchResult(
                modifier = Modifier,
                count = 3,
                index = 0,
                pharmacy = PharmacyPreviewData.ALL_PRESENT_DATA,
                onSelectPharmacy = {}
            )
            SpacerMedium()
            PharmacyListSearchResult(
                modifier = Modifier,
                count = 3,
                index = 1,
                pharmacy = PharmacyPreviewData.LOCAL_PICKUP_ONLY_DATA,
                onSelectPharmacy = {}
            )
            SpacerMedium()
            PharmacyListSearchResult(
                modifier = Modifier,
                count = 3,
                index = 2,
                pharmacy = PharmacyPreviewData.LOCAL_PICKUP_ONLY_DATA,
                onSelectPharmacy = {}
            )
            SpacerMedium()
            PharmacyListSearchResult(
                modifier = Modifier,
                count = 3,
                index = 3,
                pharmacy = PharmacyPreviewData.LOCAL_PICKUP_ONLY_DATA,
                onSelectPharmacy = {}
            )
            SpacerMedium()
        }
    }
}
