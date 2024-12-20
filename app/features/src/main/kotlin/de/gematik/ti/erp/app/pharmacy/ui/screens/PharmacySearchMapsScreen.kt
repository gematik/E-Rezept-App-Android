/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.ui.screens

import android.graphics.Point
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import de.gematik.ti.erp.app.base.openSettingsAsNewActivity
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.fhir.model.Coordinates
import de.gematik.ti.erp.app.permissions.getLocationPermissionLauncher
import de.gematik.ti.erp.app.permissions.isLocationPermissionAndServiceEnabled
import de.gematik.ti.erp.app.permissions.locationPermissions
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes
import de.gematik.ti.erp.app.pharmacy.presentation.PharmacyGraphController
import de.gematik.ti.erp.app.pharmacy.presentation.rememberPharmacySearchMapsController
import de.gematik.ti.erp.app.pharmacy.presentation.toCoordinates
import de.gematik.ti.erp.app.pharmacy.presentation.toLatLng
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyScreen
import de.gematik.ti.erp.app.pharmacy.ui.components.DefaultZoomLevel
import de.gematik.ti.erp.app.pharmacy.ui.components.GooglePharmacyMap
import de.gematik.ti.erp.app.pharmacy.ui.components.LocationPermissionDeniedDialog
import de.gematik.ti.erp.app.pharmacy.ui.components.LocationServicesNotAvailableDialog
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacyMap
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacyProperties
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacySettings
import de.gematik.ti.erp.app.pharmacy.ui.components.PositionState
import de.gematik.ti.erp.app.pharmacy.ui.components.pharmacyMapsOverlay
import de.gematik.ti.erp.app.pharmacy.ui.model.SelectedPharmacyUi
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.disableZoomWhileActive
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbar
import de.gematik.ti.erp.app.utils.letNotNull
import io.github.aakira.napier.Napier
import org.kodein.di.compose.rememberInstance

private const val MyLocationZoomLevel = 15f
private const val AnimationDuration = 730

class PharmacySearchMapsScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: PharmacyGraphController
) : PharmacyScreen() {

    @Composable
    override fun Content() {
        val snackbar = LocalSnackbar.current
        val dialog = LocalDialog.current
        val context = LocalContext.current
        LocalActivity.current.disableZoomWhileActive()

        val mapHolder by rememberInstance<PharmacyMap>()
        val filter by graphController.filter()
        val coordinates by graphController.coordinates()
        val previewCoordinates by graphController.previewCoordinates()

        val mapsListController = rememberPharmacySearchMapsController(filter, coordinates)

        val locationNotFoundEvent = graphController.locationNotFoundEvent
        val areMapsLoadingEvent = mapsListController.areMapsLoadingEvent
        val pharmacies by mapsListController.pharmaciesState
        val searchState by mapsListController.searchParamState

        var isLoading by remember { mutableStateOf(false) }
        var isMapLocationEnabled by remember { mutableStateOf(false) }
        var showSearchButton by remember { mutableStateOf(false) }

        // NOTE: forcing it as a Google map, could lead to crashes if the map is not a Google map
        val cameraPositionState by (mapHolder as GooglePharmacyMap).cameraPositionState
        val cameraRadius by mapsListController.cameraRadiusState
        var mapZoomState by remember { mutableFloatStateOf(0f) }
        var selectedPharmacy by remember { mutableStateOf<SelectedPharmacyUi?>(null) }
        var radiusFromSearch by remember { mutableDoubleStateOf(0.0) }
        val positionState = remember(coordinates, previewCoordinates) {
            PositionState(
                position = coordinates ?: previewCoordinates,
                zoom = DefaultZoomLevel
            )
        }

        val properties = remember(isMapLocationEnabled) {
            PharmacyProperties.Default
                .copy(isMyLocationEnabled = isMapLocationEnabled)
        }

        val locationPermissionLauncher = getLocationPermissionLauncher(
            onPermissionResult = { isLocationEnabled ->
                isMapLocationEnabled = isLocationEnabled
                graphController.onLocationPermissionResult(isLocationEnabled)
            }
        )

        LaunchedEffect(true) {
            locationPermissionLauncher.launch(locationPermissions)
        }

        areMapsLoadingEvent.listen {
            isLoading = it
        }

        LocationPermissionDeniedDialog(
            event = graphController.permissionDeniedEvent,
            dialog = dialog,
            onClick = graphController::forceLocationFalse
        )

        LocationServicesNotAvailableDialog(
            event = graphController.serviceDisabledEvent,
            dialog = dialog,
            onClickDismiss = graphController::forceLocationFalse,
            onClickSettings = {
                context.openSettingsAsNewActivity(
                    action = Settings.ACTION_LOCATION_SOURCE_SETTINGS,
                    isSimpleIntent = true
                )
            }
        )

        locationNotFoundEvent.listen {
            snackbar.show(context.getString(R.string.location_not_found))
        }

        cameraPositionState?.let {
            CameraAnimation(
                coordinates = coordinates ?: previewCoordinates,
                cameraPositionState = it,
                searchState = searchState,
                pharmacies = pharmacies
            ) {
                showSearchButton = true
            }
        }

        val onBack: () -> Unit = {
            navController.popBackStack()
        }

        BackHandler { onBack() }

        PharmacySearchMapsScreenContent(
            cameraRadius = cameraRadius,
            coordinates = coordinates,
            isLoading = isLoading,
            mapHolder = mapHolder,
            mapZoomState = mapZoomState,
            positionState = positionState,
            pharmacyProperties = properties,
            pharmacies = pharmacies,
            selectedPharmacy = selectedPharmacy,
            showSearchButton = showSearchButton,
            isMapLocationEnabled = isMapLocationEnabled,
            onBack = onBack,
            onZoomStateChanged = {
                mapZoomState = it
            },
            onClickFilter = {
                navController.navigate(
                    PharmacyRoutes.PharmacyFilterSheetScreen.path(
                        showNearbyFilter = false,
                        navigateWithSearchButton = false
                    )
                )
            },
            onClickPharmacy = { selectedPharmacyUi ->
                selectedPharmacy = selectedPharmacyUi
                navController.navigate(
                    PharmacyRoutes.PharmacyDetailsFromPharmacyScreen.path(
                        pharmacy = selectedPharmacyUi.item,
                        taskId = navBackStackEntry.arguments?.getString(PharmacyRoutes.PHARMACY_NAV_TASK_ID) ?: ""
                    )
                )
            },
            onSearch = { isLocationSearch ->
                if (isLoading.isFalse()) {
                    radiusFromSearch = SphericalUtil.computeDistanceBetween(
                        cameraPositionState?.projection?.visibleRegion?.latLngBounds?.northeast,
                        cameraPositionState?.projection?.visibleRegion?.latLngBounds?.southwest
                    ) / 2.0

                    if (isLocationSearch) {
                        mapsListController.onCameraRadiusChanged(radiusFromSearch)
                        locationPermissionLauncher.launch(locationPermissions)
                    } else {
                        mapsListController.onCameraRadiusChanged(radiusFromSearch)
                        cameraPositionState?.position?.target?.let { camLatLng ->
                            graphController.forceLocationFalseWithSelectedCoordinates(camLatLng.toCoordinates())
                        }
                    }
                }
            }
        )
    }

    companion object {
        private fun Boolean.isFalse() = !this
    }
}

@Suppress("LongParameterList")
@Composable
private fun PharmacySearchMapsScreenContent(
    mapHolder: PharmacyMap,
    positionState: PositionState,
    pharmacyProperties: PharmacyProperties,
    pharmacies: List<PharmacyUseCaseData.Pharmacy>,
    selectedPharmacy: SelectedPharmacyUi?,
    showSearchButton: Boolean,
    isLoading: Boolean,
    isMapLocationEnabled: Boolean,
    cameraRadius: Double,
    mapZoomState: Float,
    coordinates: Coordinates?,
    onClickPharmacy: (SelectedPharmacyUi) -> Unit,
    onSearch: (Boolean) -> Unit,
    onClickFilter: () -> Unit,
    onZoomStateChanged: (Float) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(contentWindowInsets = WindowInsets.Companion.systemBars) { paddings ->
        Box {
            mapHolder.Map(
                modifier = Modifier
                    .padding(
                        start = paddings.calculateStartPadding(LocalLayoutDirection.current),
                        end = paddings.calculateEndPadding(LocalLayoutDirection.current)
                    )
                    .fillMaxSize(),
                isFullScreen = true,
                onZoomStateChanged = onZoomStateChanged,
                positionState = positionState,
                settings = PharmacySettings.FullScreen,
                properties = pharmacyProperties,
                contentPaddingValues = WindowInsets.Companion.systemBars.asPaddingValues(),
                onClick = null,
                content = {
                    key(pharmacies.hashCode()) {
                        MarkedPharmacies(
                            cameraRadius = cameraRadius,
                            mapZoomState = mapZoomState,
                            coordinates = coordinates,
                            pharmacyMapsResult = pharmacies,
                            onClickMarker = onClickPharmacy
                        )
                    }
                    key(selectedPharmacy) {
                        selectedPharmacy?.let { selectedPharmacy ->
                            SelectedPharmacy(
                                cameraRadius = cameraRadius,
                                mapZoomState = mapZoomState,
                                coordinates = coordinates,
                                pharmacy = selectedPharmacy,
                                onClickMarker = onClickPharmacy
                            )
                        }
                    }
                    if (isMapLocationEnabled) {
                        coordinates?.let { coordinates ->
                            MarkerInfoWindowContent {
                                Marker(
                                    state = MarkerState(coordinates.toLatLng()),
                                    icon = null
                                )
                            }
                        }
                    }
                }
            )
            pharmacyMapsOverlay(
                showSearchButton = showSearchButton,
                isLoading = isLoading,
                onSearch = onSearch,
                onClickFilter = onClickFilter,
                onBack = onBack
            )
        }
    }
}

@Composable
private fun SelectedPharmacy(
    cameraRadius: Double,
    mapZoomState: Float,
    coordinates: Coordinates?,
    pharmacy: SelectedPharmacyUi,
    onClickMarker: (SelectedPharmacyUi) -> Unit
) {
    val markerIcon = remember { BitmapDescriptorFactory.fromResource(R.drawable.maps_marker_red) }
    pharmacy.item.coordinates?.let { pharmacyCoordinates ->
        // the remember needs the keys given to adjust the markers to user interactions
        val state = remember(cameraRadius, pharmacy, mapZoomState, coordinates) {
            if (pharmacy.state != null) {
                MarkerState(pharmacy.state.position)
            } else {
                val latLng = LatLng(pharmacyCoordinates.latitude, pharmacyCoordinates.longitude)
                MarkerState(latLng)
            }
        }
        Marker(
            state = state,
            onClick = {
                onClickMarker(pharmacy.copy(state = state))
                false
            },
            icon = markerIcon,
            zIndex = 9999f
        )
    }
}

@Composable
private fun MarkedPharmacies(
    cameraRadius: Double,
    mapZoomState: Float,
    coordinates: Coordinates?,
    pharmacyMapsResult: List<PharmacyUseCaseData.Pharmacy>,
    onClickMarker: (SelectedPharmacyUi) -> Unit
) {
    val markerIcon = remember { BitmapDescriptorFactory.fromResource(R.drawable.maps_marker) }
    pharmacyMapsResult.mapNotNull { pharmacy ->
        pharmacy.coordinates?.let { pharmacyCoordinates ->
            // the remember needs the keys given to adjust the markers to user interactions
            val marker = remember(cameraRadius, pharmacyMapsResult, mapZoomState, coordinates) {
                val latLng = LatLng(pharmacyCoordinates.latitude, pharmacyCoordinates.longitude)
                MarkerState(latLng)
            }
            Marker(
                state = marker,
                icon = markerIcon,
                onClick = {
                    onClickMarker(SelectedPharmacyUi(pharmacy, marker))
                    false
                }
            )
        }
    }
}

@Composable
private fun CameraAnimation(
    coordinates: Coordinates,
    cameraPositionState: CameraPositionState,
    searchState: PharmacyUseCaseData.MapsSearchData,
    pharmacies: List<PharmacyUseCaseData.Pharmacy>,
    onShowSearchButton: () -> Unit
) {
    val context = LocalContext.current
    var lastMarkerCenter by remember { mutableStateOf(coordinates.toLatLng()) }
    val isMoving by remember(cameraPositionState.isMoving) {
        derivedStateOf { cameraPositionState.isMoving }
    }

    val moveDistance = with(LocalDensity.current) { SizeDefaults.triple.roundToPx() }
    LaunchedEffect(isMoving) {
        cameraPositionState.projection?.let { projection ->
            val latLng =
                (searchState.locationMode as? PharmacyUseCaseData.LocationMode.Enabled)?.toLatLng()
                    ?: lastMarkerCenter
            val distanceBetween = SphericalUtil.computeDistanceBetween(cameraPositionState.position.target, latLng)
            val locationLatLng = projection.fromScreenLocation(Point(0, 0))
            val movedDistanceLatLng = projection.fromScreenLocation(Point(moveDistance, 0))
            if (distanceBetween >= SphericalUtil.computeDistanceBetween(locationLatLng, movedDistanceLatLng)) {
                onShowSearchButton()
            }
        }
    }

    LaunchedEffect(pharmacies) {
        try {
            if (context.isLocationPermissionAndServiceEnabled()) {
                val latitudeAndLongitude =
                    (searchState.locationMode as? PharmacyUseCaseData.LocationMode.Enabled)
                        ?.toLatLng()
                        ?: lastMarkerCenter
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(latitudeAndLongitude, MyLocationZoomLevel),
                    durationMs = AnimationDuration
                )
            } else if (pharmacies.isNotEmpty()) {
                val bounds = LatLngBounds.builder().apply {
                    pharmacies.forEach {
                        letNotNull(it.coordinates?.latitude, it.coordinates?.longitude) { latitude, longitude ->
                            include(LatLng(latitude, longitude))
                        }
                    }
                }.build()
                val latLng = coordinates.toLatLng()
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(latLng, MyLocationZoomLevel),
                    durationMs = AnimationDuration
                )
                CameraUpdateFactory.zoomBy(MyLocationZoomLevel)
                lastMarkerCenter = bounds.center
            }
        } catch (e: SecurityException) {
            Napier.e { "Security exception: Missing permissions ${e.stackTraceToString()}" }
        }
    }
}
