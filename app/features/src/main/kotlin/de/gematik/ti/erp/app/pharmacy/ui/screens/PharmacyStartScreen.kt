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

import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.base.openSettingsAsNewActivity
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.loading.LoadingIndicator
import de.gematik.ti.erp.app.permissions.getLocationPermissionLauncher
import de.gematik.ti.erp.app.permissions.isLocationPermissionAndServiceEnabled
import de.gematik.ti.erp.app.permissions.locationPermissions
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData.OverviewPharmacy
import de.gematik.ti.erp.app.pharmacy.model.SelectedFavouritePharmacyState
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRouteBackStackEntryArguments
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes
import de.gematik.ti.erp.app.pharmacy.presentation.FilterType
import de.gematik.ti.erp.app.pharmacy.presentation.PharmacyGraphController
import de.gematik.ti.erp.app.pharmacy.presentation.rememberPharmacyStartController
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyScreen
import de.gematik.ti.erp.app.pharmacy.ui.components.FavouritePharmacies
import de.gematik.ti.erp.app.pharmacy.ui.components.FilterSection
import de.gematik.ti.erp.app.pharmacy.ui.components.LocationPermissionDeniedDialog
import de.gematik.ti.erp.app.pharmacy.ui.components.LocationServicesNotAvailableDialog
import de.gematik.ti.erp.app.pharmacy.ui.components.MapsSection
import de.gematik.ti.erp.app.pharmacy.ui.components.MockMap
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacyMap
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacySearchButton
import de.gematik.ti.erp.app.pharmacy.ui.model.QuickFilter
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Coordinates
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.capitalizeFirstChar
import de.gematik.ti.erp.app.utils.extensions.isGooglePlayServiceAvailable
import de.gematik.ti.erp.app.utils.letNotNull
import kotlinx.datetime.Instant
import org.kodein.di.compose.rememberInstance

class PharmacyStartScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: PharmacyGraphController
) : PharmacyScreen() {

    @Suppress("CyclomaticComplexMethod")
    @Composable
    override fun Content() {
        val isModalFlow = navBackStackEntry.arguments?.getBoolean(
            PharmacyRoutes.PHARMACY_NAV_SHOW_BACK_ON_START_SCREEN
        ) ?: false

        val context = LocalContext.current
        val dialog = LocalDialog.current

        val controller = rememberPharmacyStartController()

        val selectedPharmacyState by controller.selectedPharmacyState.collectAsStateWithLifecycle()

        val retryEvent = ComposableEvent<Unit>()
        val acceptMissingEvent = ComposableEvent<Unit>()
        var showLoadingIndicator by remember { mutableStateOf(false) }

        val favouritePharmacies by graphController.favouritePharmacies()
        val previewCoordinates by graphController.previewCoordinates()
        val isGooglePlayServicesAvailable = context.isGooglePlayServiceAvailable()

        var quickFilterSelectedWithoutLocation by remember {
            mutableStateOf<QuickFilter?>(null)
        }

        // Allows us to switch between the real implementation and a mock implementation of PharmacyMap
        val pharmacyMap by rememberInstance<PharmacyMap>()

        LaunchedEffect(Unit) {
            graphController.init(context)
        }

        val locationPermissionLauncher = getLocationPermissionLauncher(
            onPermissionResult = {
                graphController.onLocationPermissionResult(it)
            }
        )

        LocationPermissionDeniedDialog(
            event = graphController.permissionDeniedEvent,
            dialog = dialog,
            onClick = {
                graphController.forceLocationFalse()
                quickFilterSelectedWithoutLocation = null
            }
        )

        LocationServicesNotAvailableDialog(
            event = graphController.serviceDisabledEvent,
            dialog = dialog,
            onClickDismiss = {
                graphController.forceLocationFalse()
                quickFilterSelectedWithoutLocation = null
            },
            onClickSettings = {
                quickFilterSelectedWithoutLocation = null
                context.openSettingsAsNewActivity(
                    action = Settings.ACTION_LOCATION_SOURCE_SETTINGS,
                    isSimpleIntent = true
                )
            }
        )

        graphController.askLocationPermissionEvent.listen {
            locationPermissionLauncher.launch(locationPermissions)
        }

        graphController.locationProvidedEvent.listen {
            when (quickFilterSelectedWithoutLocation) {
                // NEARBY filter is already set when location was enabled
                QuickFilter.OpenNowNearby -> updateOpenNowNearbyFilterAndNavigationToSearchList()
                QuickFilter.DeliveryNearby -> updateDeliveryNearbyFilterAndNavigationToSearchList()
                else -> {
                    // do nothing
                }
            }
        }

        PharmacyNotReachableDialog(
            event = retryEvent,
            dialogScaffold = dialog,
            onRetry = controller::getPharmacy
        )

        PharmacyNotFoundDialog(
            event = acceptMissingEvent,
            dialogScaffold = dialog,
            onClickAccept = controller::clearSelectedPharmacy
        )

        when (selectedPharmacyState) {
            is SelectedFavouritePharmacyState.Loading -> {
                showLoadingIndicator = true
            }

            is SelectedFavouritePharmacyState.Error -> {
                controller.resetSelectedPharmacyState()
                retryEvent.trigger()
            }

            is SelectedFavouritePharmacyState.Data -> {
                controller.resetSelectedPharmacyState()
                letNotNull(
                    (selectedPharmacyState as SelectedFavouritePharmacyState.Data).pharmacy,
                    PharmacyRouteBackStackEntryArguments(navBackStackEntry).getTaskId()
                ) { pharmacy, taskId ->
                    navController.navigate(
                        PharmacyRoutes.PharmacyDetailsFromPharmacyScreen.path(
                            pharmacy = pharmacy,
                            taskId = taskId
                        )
                    )
                }
            }

            SelectedFavouritePharmacyState.Idle -> {
                showLoadingIndicator = false
            }

            SelectedFavouritePharmacyState.Missing -> {
                controller.resetSelectedPharmacyState()
                acceptMissingEvent.trigger()
            }
        }

        Box {
            PharmacyStartScreenContent(
                isModalFlow = isModalFlow,
                favouritePharmacies = favouritePharmacies,
                listState = listState,
                isGooglePlayServicesAvailable = isGooglePlayServicesAvailable,
                previewCoordinates = previewCoordinates,
                previewMap = pharmacyMap,
                onBack = {
                    if (isModalFlow) {
                        graphController.reset()
                        navController.popBackStack()
                    }
                },
                onClickPharmacySearch = {
                    navController.navigate(
                        PharmacyRoutes.PharmacySearchListScreen.path(
                            taskId = PharmacyRouteBackStackEntryArguments(navBackStackEntry).getTaskId()
                        )
                    )
                },
                onClickMapsSearch = {
                    navController.navigate(
                        PharmacyRoutes.PharmacySearchMapsScreen.path(
                            taskId = PharmacyRouteBackStackEntryArguments(navBackStackEntry).getTaskId()
                        )
                    )
                },
                onClickQuickFilterSearch = { quickFilter ->
                    when (quickFilter) {
                        QuickFilter.OpenNowNearby -> {
                            if (context.isLocationPermissionAndServiceEnabled()) {
                                // NEARBY filter needs to be set since it is not set when location was enabled long ago
                                graphController.updateFilter(FilterType.NEARBY)
                                updateOpenNowNearbyFilterAndNavigationToSearchList()
                            } else {
                                quickFilterSelectedWithoutLocation = QuickFilter.OpenNowNearby
                                graphController.checkLocationServiceAndPermission(context)
                            }
                        }

                        QuickFilter.DeliveryNearby -> {
                            if (context.isLocationPermissionAndServiceEnabled()) {
                                // NEARBY filter needs to be set since it is not set when location was enabled long ago
                                graphController.updateFilter(FilterType.NEARBY)
                                updateDeliveryNearbyFilterAndNavigationToSearchList()
                            } else {
                                quickFilterSelectedWithoutLocation = QuickFilter.DeliveryNearby
                                graphController.checkLocationServiceAndPermission(context)
                            }
                        }

                        QuickFilter.Online -> {
                            updateOnlineFilterAndNavigateToSearchList()
                        }
                    }
                },
                onClickFilter = {
                    navController.navigate(
                        PharmacyRoutes.PharmacyFilterSheetScreen.path(
                            showNearbyFilter = true,
                            navigateWithSearchButton = true
                        )
                    )
                },
                onClickFavouritePharmacy = {
                    controller.onPharmacySelected(it)
                }
            )

            if (showLoadingIndicator) {
                LoadingIndicator()
            }
        }
    }

    private fun updateOpenNowNearbyFilterAndNavigationToSearchList() {
        graphController.updateFilter(FilterType.OPEN_NOW)
        navController.navigate(
            PharmacyRoutes.PharmacySearchListScreen.path(
                taskId = PharmacyRouteBackStackEntryArguments(navBackStackEntry).getTaskId()
            )
        )
    }

    private fun updateDeliveryNearbyFilterAndNavigationToSearchList() {
        graphController.updateFilter(FilterType.DELIVERY_SERVICE)
        navController.navigate(
            PharmacyRoutes.PharmacySearchListScreen.path(
                taskId = PharmacyRouteBackStackEntryArguments(navBackStackEntry).getTaskId()
            )
        )
    }

    private fun updateOnlineFilterAndNavigateToSearchList() {
        graphController.updateFilter(FilterType.ONLINE_SERVICE)
        navController.navigate(
            PharmacyRoutes.PharmacySearchListScreen.path(
                taskId = PharmacyRouteBackStackEntryArguments(navBackStackEntry).getTaskId()
            )
        )
    }
}

@Composable
private fun PharmacyStartScreenContent(
    isModalFlow: Boolean,
    favouritePharmacies: List<OverviewPharmacy> = emptyList(),
    previewCoordinates: Coordinates,
    previewMap: PharmacyMap,
    listState: LazyListState,
    isGooglePlayServicesAvailable: Boolean = true,
    onClickQuickFilterSearch: (QuickFilter) -> Unit,
    onClickFavouritePharmacy: (OverviewPharmacy) -> Unit,
    onClickPharmacySearch: () -> Unit,
    onClickMapsSearch: () -> Unit,
    onClickFilter: () -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        modifier = Modifier.imePadding(),
        isModalFlow = isModalFlow,
        topBarTitle = stringResource(R.string.redeem_header).capitalizeFirstChar(),
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        listState = listState,
        onBack = onBack,
        content = { contentPadding ->
            PharmacyStartScreenBody(
                contentPadding = contentPadding,
                favouritePharmacies = favouritePharmacies,
                previewCoordinates = previewCoordinates,
                previewMap = previewMap,
                listState = listState,
                isGooglePlayServicesAvailable = isGooglePlayServicesAvailable,
                onClickPharmacySearch = onClickPharmacySearch,
                onClickMapsSearch = onClickMapsSearch,
                onClickQuickFilterSearch = onClickQuickFilterSearch,
                onClickFilter = onClickFilter,
                onClickFavouritePharmacy = onClickFavouritePharmacy
            )
        }
    )
}

@Composable
private fun PharmacyStartScreenBody(
    contentPadding: PaddingValues,
    previewCoordinates: Coordinates,
    previewMap: PharmacyMap,
    favouritePharmacies: List<OverviewPharmacy>,
    listState: LazyListState,
    isGooglePlayServicesAvailable: Boolean,
    onClickPharmacySearch: () -> Unit,
    onClickMapsSearch: () -> Unit,
    onClickQuickFilterSearch: (QuickFilter) -> Unit,
    onClickFilter: () -> Unit,
    onClickFavouritePharmacy: (OverviewPharmacy) -> Unit
) {
    val activity = LocalActivity.current
    val padding = (activity as? BaseActivity)?.applicationInnerPadding

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = listState,
        contentPadding = padding?.combineWithInnerScaffold(contentPadding) ?: contentPadding,
    ) {
        item {
            SpacerLarge()
        }
        PharmacySearchButton(
            modifier = Modifier
                .padding(horizontal = PaddingDefaults.Medium),
            onStartSearch = onClickPharmacySearch
        )
        if (isGooglePlayServicesAvailable) {
            MapsSection(
                previewCoordinates = previewCoordinates,
                previewMap = previewMap
            ) { onClickMapsSearch() }
        }
        FilterSection(
            onClickFilter = onClickFilter,
            onSelectFilter = onClickQuickFilterSearch
        )
        if (favouritePharmacies.isNotEmpty()) {
            FavouritePharmacies(
                modifier = Modifier
                    .padding(horizontal = PaddingDefaults.Medium)
                    .fillMaxWidth(),
                pharmacies = favouritePharmacies
            ) {
                onClickFavouritePharmacy(it)
            }
        }
        item {
            SpacerLarge()
        }
    }
}

@Composable
private fun PharmacyNotReachableDialog(
    event: ComposableEvent<Unit>,
    dialogScaffold: DialogScaffold,
    onRetry: () -> Unit
) {
    event.listen {
        dialogScaffold.show {
            ErezeptAlertDialog(
                title = stringResource(R.string.pharmacy_search_apovz_call_no_internet_header),
                bodyText = stringResource(R.string.pharmacy_search_apovz_call_no_internet_info),
                dismissText = stringResource(R.string.pharmacy_search_apovz_call_no_internet_cancel),
                confirmText = stringResource(R.string.pharmacy_search_apovz_call_no_internet_retry),
                onDismissRequest = it::dismiss,
                onConfirmRequest = {
                    it.dismiss()
                    onRetry()
                }
            )
        }
    }
}

@Composable
private fun PharmacyNotFoundDialog(
    event: ComposableEvent<Unit>,
    dialogScaffold: DialogScaffold,
    onClickAccept: () -> Unit
) {
    event.listen {
        dialogScaffold.show {
            ErezeptAlertDialog(
                title = stringResource(R.string.pharmacy_search_apovz_call_failed_header),
                body = stringResource(R.string.pharmacy_search_apovz_call_failed_body),
                onDismissRequest = {
                    onClickAccept()
                    it.dismiss()
                }
            )
        }
    }
}

@Suppress("MagicNumber")
@LightDarkPreview
@Composable
fun PharmacyStartScreenPreview() {
    val time = Instant.parse("2022-01-01T00:00:00Z")
    val berlin = Coordinates(52.51947562977698, 13.404335795642881)

    val pharmacyMap = MockMap()

    PreviewAppTheme {
        Column {
            PharmacyStartScreenContent(
                favouritePharmacies = listOf(
                    OverviewPharmacy(
                        lastUsed = time,
                        isFavorite = true,
                        usageCount = 1,
                        telematikId = "123456789",
                        pharmacyName = "Berlin Apotheke",
                        address = "BerlinStr, 12345 Berlin"
                    ),
                    OverviewPharmacy(
                        lastUsed = time,
                        isFavorite = false,
                        usageCount = 1,
                        telematikId = "123456788",
                        pharmacyName = "Stuttgart Apotheke",
                        address = "StuttgartStr, 12345 Stuttgart"
                    )
                ),
                previewCoordinates = berlin,
                previewMap = pharmacyMap,
                listState = rememberLazyListState(),
                isGooglePlayServicesAvailable = true,
                onClickQuickFilterSearch = {},
                onClickFavouritePharmacy = {},
                onClickPharmacySearch = {},
                onClickMapsSearch = {},
                onClickFilter = {},
                onBack = {},
                isModalFlow = false
            )
        }
    }
}
