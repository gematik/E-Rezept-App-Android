/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.pharmacy.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Moped
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.cardwall.ui.PrimaryButtonSmall
import de.gematik.ti.erp.app.pharmacy.model.OftenUsedPharmacyData
import de.gematik.ti.erp.app.pharmacy.repository.model.OftenUsedPharmaciesViewModel
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberViewModel

private const val LastUsedPharmaciesListLength = 5

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PharmacyOverviewScreen(
    onBack: () -> Unit,
    onStartSearch: () -> Unit,
    filter: PharmacyUseCaseData.Filter,
    onFilterChange: (PharmacyUseCaseData.Filter) -> Unit,
    onSelectPharmacy: (PharmacyUseCaseData.Pharmacy) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val modal = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    ModalBottomSheetLayout(
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContent = {
            FilterBottomSheet(
                modifier = Modifier.navigationBarsPadding(),
                filter = filter,
                onClickChip = onFilterChange,
                onClickClose = { scope.launch { modal.hide() } },
                extraContent = {
                    SpacerLarge()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        PrimaryButtonSmall(
                            onClick = {
                                scope.launch { modal.hide() }
                                onStartSearch()
                            }
                        ) {
                            Text(stringResource(R.string.search_pharmacies_start_search))
                        }
                    }
                    SpacerLarge()
                }
            )
        },
        sheetState = modal
    ) {
        AnimatedElevationScaffold(
            listState = listState,
            topBarTitle = stringResource(R.string.redeem_header),
            onBack = onBack
        ) {
            val pharmacyViewModel by rememberViewModel<OftenUsedPharmaciesViewModel>()
            OverviewContent(
                onSelectPharmacy = onSelectPharmacy,
                listState = listState,
                onFilterChange = onFilterChange,
                searchFilter = filter,
                onStartSearch = onStartSearch,
                pharmacyViewModel = pharmacyViewModel,
                onShowFilter = {
                    scope.launch { modal.show() }
                }
            )
        }
    }
}

@Composable
private fun OverviewContent(
    onSelectPharmacy: (PharmacyUseCaseData.Pharmacy) -> Unit,
    listState: LazyListState,
    searchFilter: PharmacyUseCaseData.Filter,
    onFilterChange: (PharmacyUseCaseData.Filter) -> Unit,
    onStartSearch: () -> Unit,
    pharmacyViewModel: OftenUsedPharmaciesViewModel,
    onShowFilter: () -> Unit
) {
    val oftenUsedPharmacyList by produceState(initialValue = listOf<OftenUsedPharmacyData.OftenUsedPharmacy>()) {
        pharmacyViewModel.oftenUsedPharmaciesState().collect { value = it }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = PaddingDefaults.Medium)
    ) {
        item {
            PharmacySearchButton(
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
            ) {
                onFilterChange(
                    searchFilter.copy(
                        ready = true,
                        onlineService = false,
                        deliveryService = false,
                        openNow = false
                    )
                )
                onStartSearch()
            }
        }
        item {
            FilterSection(
                filter = searchFilter,
                onClick = onFilterChange,
                onClickFilter = onShowFilter,
                onStartSearch = onStartSearch
            )
        }
        item {
            OftenUsedPharmacies(
                oftenUsedPharmacyList = oftenUsedPharmacyList,
                onSelectPharmacy = onSelectPharmacy,
                pharmacyViewModel = pharmacyViewModel
            )
        }
    }
}

@Composable
private fun OftenUsedPharmacies(
    oftenUsedPharmacyList: List<OftenUsedPharmacyData.OftenUsedPharmacy>,
    onSelectPharmacy: (PharmacyUseCaseData.Pharmacy) -> Unit,
    pharmacyViewModel: OftenUsedPharmaciesViewModel
) {
    if (oftenUsedPharmacyList.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PaddingDefaults.Medium)
        ) {
            Text(
                stringResource(R.string.pharmacy_often_used_header),
                style = AppTheme.typography.subtitle1,
                modifier = Modifier.padding(top = PaddingDefaults.XXLarge, bottom = PaddingDefaults.Medium),
                textAlign = TextAlign.Start
            )
            val shortOftenUsedPharmacyList = remember { oftenUsedPharmacyList.take(LastUsedPharmaciesListLength) }
            Column {
                for (oftenUsedPharmacy in shortOftenUsedPharmacyList) {
                    OftenUsedPharmacyCard(
                        oftenUsedPharmacy = oftenUsedPharmacy,
                        onSelectPharmacy = onSelectPharmacy,
                        pharmacyViewModel = pharmacyViewModel
                    )
                    SpacerMedium()
                }
            }
        }
    }
}

@Stable
private sealed interface RefreshState {
    @Stable
    object Loading : RefreshState

    @Stable
    class Success(val pharmacy: List<PharmacyUseCaseData.Pharmacy>) : RefreshState

    @Stable
    object NotFound : RefreshState

    @Stable
    object Error : RefreshState
}

@Composable
private fun OftenUsedPharmacyCard(
    oftenUsedPharmacy: OftenUsedPharmacyData.OftenUsedPharmacy,
    onSelectPharmacy: (PharmacyUseCaseData.Pharmacy) -> Unit,
    pharmacyViewModel: OftenUsedPharmaciesViewModel
) {
    var showFailedPharmacyCallDialog by remember { mutableStateOf(false) }
    var showNoInternetConnectionDialog by remember { mutableStateOf(false) }

    val refreshFlow = remember { MutableSharedFlow<Unit>() }
    var state by remember { mutableStateOf<RefreshState>(RefreshState.Loading) }
    LaunchedEffect(Unit) {
        refreshFlow
            .onStart { emit(Unit) } // emit once to start the flow directly
            .collectLatest {
                state = RefreshState.Loading
                pharmacyViewModel.findPharmacyByTelematikIdState(oftenUsedPharmacy.telematikId).first().fold(
                    onFailure = {
                        Napier.e("Could not find pharmacy by telematikId", it)
                        state = RefreshState.Error
                    },
                    onSuccess = {
                        state = if (it.isEmpty()) {
                            RefreshState.NotFound
                        } else {
                            RefreshState.Success(it)
                        }
                        showNoInternetConnectionDialog = false
                    }
                )
            }
    }

    val scope = rememberCoroutineScope()

    if (showNoInternetConnectionDialog) {
        CommonAlertDialog(
            header = stringResource(R.string.pharmacy_search_apovz_call_no_internet_header),
            info = stringResource(R.string.pharmacy_search_apovz_call_no_internet_info),
            cancelText = stringResource(R.string.pharmacy_search_apovz_call_no_internet_cancel),
            actionText = stringResource(R.string.pharmacy_search_apovz_call_no_internet_retry),
            onCancel = { showNoInternetConnectionDialog = false },
            onClickAction = {
                scope.launch {
                    refreshFlow.onStart { emit(Unit) }.collectLatest {
                        state = RefreshState.Loading
                        pharmacyViewModel.findPharmacyByTelematikIdState(
                            oftenUsedPharmacy.telematikId
                        ).first().fold(
                            onFailure = {
                                Napier.e("Could not find pharmacy by telematikId", it)
                                state = RefreshState.Error
                            },
                            onSuccess = {
                                state = if (it.isEmpty()) {
                                    RefreshState.NotFound
                                } else {
                                    RefreshState.Success(it)
                                }
                                showNoInternetConnectionDialog = false
                            }
                        )
                    }
                }
            }
        )
    }
    if (showFailedPharmacyCallDialog) {
        AcceptDialog(
            header = stringResource(R.string.pharmacy_search_apovz_call_failed_header),
            info = stringResource(R.string.pharmacy_search_apovz_call_failed_body),
            onClickAccept = {
                scope.launch { pharmacyViewModel.deleteOftenUsedPharmacy(oftenUsedPharmacy) }
                showFailedPharmacyCallDialog = false
            },
            acceptText = stringResource(R.string.pharmacy_search_apovz_call_failed_accept)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button) {
                when (state) {
                    is RefreshState.Success -> onSelectPharmacy((state as RefreshState.Success).pharmacy.first())
                    is RefreshState.Error -> showNoInternetConnectionDialog = true
                    is RefreshState.NotFound -> showFailedPharmacyCallDialog = true
                    else -> {}
                }
            },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.colors.neutral300),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            PharmacyImagePlaceholder(modifier = Modifier.padding(PaddingDefaults.Medium))

            Column(
                modifier = Modifier.padding(
                    end = PaddingDefaults.Medium,
                    top = PaddingDefaults.Medium,
                    bottom = PaddingDefaults.Medium
                )
            ) {
                Text(
                    oftenUsedPharmacy.pharmacyName,
                    style = AppTheme.typography.subtitle1
                )
                Text(
                    oftenUsedPharmacy.address,
                    style = AppTheme.typography.body2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FilterSection(
    filter: PharmacyUseCaseData.Filter,
    onClick: (PharmacyUseCaseData.Filter) -> Unit,
    onStartSearch: () -> Unit,
    onClickFilter: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.search_pharmacies_filter_header),
            style = AppTheme.typography.subtitle1,
            modifier = Modifier
                .padding(top = PaddingDefaults.XXLarge, bottom = PaddingDefaults.Medium)
                .padding(horizontal = PaddingDefaults.Medium),
            textAlign = TextAlign.Start
        )
        FilterButton(
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
            text = stringResource(R.string.search_pharmacies_filter_open_now_and_local),
            icon = Icons.Outlined.LocationOn,
            onClick = {
                onClick(
                    filter.copy(
                        nearBy = true,
                        ready = true,
                        openNow = true,
                        deliveryService = false,
                        onlineService = false
                    )
                )
                onStartSearch()
            }
        )
        FilterButton(
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
            text = stringResource(R.string.search_pharmacies_filter_delivery_service),
            icon = Icons.Outlined.Moped,
            onClick = {
                onClick(
                    filter.copy(
                        nearBy = true,
                        ready = true,
                        deliveryService = true,
                        onlineService = false,
                        openNow = false
                    )
                )
                onStartSearch()
            }
        )
        FilterButton(
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
            text = stringResource(R.string.search_pharmacies_filter_online_service),
            icon = Icons.Outlined.LocalShipping,
            onClick = {
                onClick(
                    filter.copy(
                        nearBy = false,
                        ready = true,
                        onlineService = true,
                        deliveryService = false,
                        openNow = false
                    )
                )
                onStartSearch()
            }
        )
        FilterButton(
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
            text = stringResource(R.string.search_pharmacies_filter_by),
            icon = Icons.Outlined.Tune,
            onClick = onClickFilter
        )
    }
}

@Composable
private fun FilterButton(
    modifier: Modifier,
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) { onClick() }
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            icon,
            null,
            tint = AppTheme.colors.neutral600
        )
        SpacerMedium()
        Text(
            text,
            modifier = Modifier.padding(vertical = PaddingDefaults.Medium),
            color = AppTheme.colors.neutral900,
            style = AppTheme.typography.body1,
            fontWeight = FontWeight.W400
        )
    }
}

@Composable
private fun PharmacySearchButton(
    modifier: Modifier,
    onStartSearch: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color = AppTheme.colors.neutral050, shape = RoundedCornerShape(16.dp))
            .clickable(role = Role.Button) { onStartSearch() }
            .padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.ShortMedium)
    ) {
        Icon(
            Icons.Rounded.Search,
            tint = AppTheme.colors.neutral600,
            contentDescription = null
        )
        SpacerSmall()
        Text(
            text = stringResource(R.string.pharmacy_start_search_text),
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f),
            style = AppTheme.typography.body1,
            color = AppTheme.colors.neutral600
        )
    }
}

@Composable
fun PharmacyImagePlaceholder(modifier: Modifier) {
    Image(
        painterResource(R.drawable.ic_green_cross),
        null,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .size(64.dp)
    )
}
