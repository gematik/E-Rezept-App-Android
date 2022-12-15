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

import android.content.Context
import android.graphics.Point
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.SwipeableState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.fhir.model.Location
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.prescription.ui.GenerellErrorState
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.ModalBottomSheet
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val Berlin = LatLng(52.51947562977698, 13.404335795642881)
private const val DefaultZoomLevel = 12.2f
private const val MyLocationZoomLevel = 15f

private fun Location.toLatLng() =
    LatLng(latitude, longitude)

private fun PharmacyUseCaseData.LocationMode.Enabled.toLatLng() =
    location.toLatLng()

@Composable
fun MapsOverviewSmall(
    modifier: Modifier,
    onClick: () -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(Berlin, DefaultZoomLevel)
    }
    Box(modifier) {
        GoogleMap(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp)),
            cameraPositionState = cameraPositionState,
            uiSettings = remember {
                MapUiSettings(
                    compassEnabled = false,
                    indoorLevelPickerEnabled = false,
                    mapToolbarEnabled = false,
                    myLocationButtonEnabled = false,
                    rotationGesturesEnabled = false,
                    scrollGesturesEnabled = false,
                    scrollGesturesEnabledDuringRotateOrZoom = false,
                    tiltGesturesEnabled = false,
                    zoomControlsEnabled = false,
                    zoomGesturesEnabled = false
                )
            }
        )
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
        )
    }
}

@Stable
sealed interface PharmacySearchSheetContentState {

    @Stable
    data class PharmacySelected(val pharmacy: PharmacyUseCaseData.Pharmacy) : PharmacySearchSheetContentState

    @Stable
    object FilterSelected : PharmacySearchSheetContentState
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MapsOverview(
    searchController: PharmacySearchController,
    orderState: PharmacyOrderState,
    onSelectPharmacy: (PharmacyUseCaseData.Pharmacy, PharmacyScreenData.OrderOption) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()

    val cameraPositionState = rememberCameraPositionState {
        val latLng =
            (searchController.searchState.locationMode as? PharmacyUseCaseData.LocationMode.Enabled)?.toLatLng()
                ?: Berlin
        position = CameraPosition.fromLatLngZoom(latLng, DefaultZoomLevel)
    }

    var pharmacies by remember { mutableStateOf<List<PharmacyUseCaseData.Pharmacy>>(emptyList()) }
    LaunchedEffect(Unit) {
        searchController
            .pharmacyMapsFlow
            .collect { result ->
                when (result) {
                    is PharmacySearchController.State.Pharmacies ->
                        pharmacies = result.pharmacies

                    is GenerellErrorState ->
                        mapsErrorMessage(context, result)?.let {
                            scaffoldState.snackbarHostState.showSnackbar(it)
                        }
                }
            }
    }

    var showSearchButton by remember { mutableStateOf(false) }
    CameraAnimation(
        cameraPositionState = cameraPositionState,
        pharmacySearchController = searchController,
        pharmacies = pharmacies,
        onShowSearchButton = {
            showSearchButton = true
        }
    )

    val scope = rememberCoroutineScope()

    val sheetState = rememberPharmacySheetState(
        orderState.selectedPharmacy?.let {
            PharmacySearchSheetContentState.PharmacySelected(it)
        }
    )

    Box {
        ScaffoldWithMap(
            scaffoldState = scaffoldState,
            orderState = orderState,
            cameraPositionState = cameraPositionState,
            pharmacySearchController = searchController,
            pharmacies = pharmacies,
            showSearchButton = showSearchButton,
            onShowSearchButton = {
                showSearchButton = it
            },
            onShowBottomSheet = {
                sheetState.show(it)
            },
            onBack = onBack
        )

        ModalBottomSheet(
            sheetState = sheetState,
            sheetContent = {
                when (sheetState.content) {
                    PharmacySearchSheetContentState.FilterSelected ->
                        FilterSheetContent(
                            modifier = Modifier.navigationBarsPadding(),
                            filter = searchController.searchState.filter,
                            onClickChip = { filter ->
                                scope.launch {
                                    val l = cameraPositionState.position.target
                                    val radius = SphericalUtil.computeDistanceBetween(
                                        cameraPositionState.projection?.visibleRegion?.latLngBounds?.northeast,
                                        cameraPositionState.projection?.visibleRegion?.latLngBounds?.southwest
                                    ) / 2.0
                                    searchController.search(
                                        searchController.searchState.name,
                                        filter.copy(nearBy = false),
                                        Location(latitude = l.latitude, longitude = l.longitude),
                                        radius
                                    )
                                }
                            },
                            onClickClose = { scope.launch { sheetState.animateTo(ModalBottomSheetValue.Hidden) } },
                            showNearByFilter = false
                        )

                    is PharmacySearchSheetContentState.PharmacySelected ->
                        PharmacyDetailsSheetContent(
                            orderState = orderState,
                            pharmacy =
                            (sheetState.content as PharmacySearchSheetContentState.PharmacySelected).pharmacy,
                            onClickOrder = { pharmacy, orderOption ->
                                onSelectPharmacy(pharmacy, orderOption)
                            }
                        )
                }
            },
            sheetShape = remember { RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp) }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Stable
class PharmacySheetState(
    private val scope: CoroutineScope
) : SwipeableState<ModalBottomSheetValue>(
    initialValue = ModalBottomSheetValue.Hidden,
    animationSpec = SwipeableDefaults.AnimationSpec,
    confirmStateChange = { true }
) {
    var content: PharmacySearchSheetContentState by mutableStateOf(PharmacySearchSheetContentState.FilterSelected)
        private set

    fun show(content: PharmacySearchSheetContentState, snap: Boolean = false) {
        this.content = content
        scope.launch {
            val state = when (content) {
                PharmacySearchSheetContentState.FilterSelected -> ModalBottomSheetValue.Expanded
                is PharmacySearchSheetContentState.PharmacySelected -> ModalBottomSheetValue.HalfExpanded
            }
            if (snap) {
                snapTo(state)
            } else {
                animateTo(state)
            }
        }
    }

    fun hide() {
        scope.launch {
            animateTo(ModalBottomSheetValue.Hidden)
        }
    }
}

@Composable
fun rememberPharmacySheetState(
    content: PharmacySearchSheetContentState? = null
): PharmacySheetState {
    val scope = rememberCoroutineScope()
    val state = remember {
        PharmacySheetState(scope)
    }
    LaunchedEffect(content) {
        content?.let { state.show(content, snap = true) }
    }
    return state
}

@Composable
private fun ScaffoldWithMap(
    scaffoldState: ScaffoldState,
    orderState: PharmacyOrderState,
    cameraPositionState: CameraPositionState,
    pharmacySearchController: PharmacySearchController,
    pharmacies: List<PharmacyUseCaseData.Pharmacy>,
    showSearchButton: Boolean,
    onShowSearchButton: (Boolean) -> Unit,
    onShowBottomSheet: (PharmacySearchSheetContentState) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var showNoLocationDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.values.any { it }) {
                scope.launch {
                    val radius = SphericalUtil.computeDistanceBetween(
                        cameraPositionState.projection?.visibleRegion?.latLngBounds?.northeast,
                        cameraPositionState.projection?.visibleRegion?.latLngBounds?.southwest
                    ) / 2.0

                    pharmacySearchController.search(
                        pharmacySearchController.searchState.name,
                        pharmacySearchController.searchState.filter.copy(nearBy = true),
                        radiusInMeter = radius
                    )
                }
            } else {
                showNoLocationDialog = true
            }
        }

    if (showNoLocationDialog) {
        NoLocationDialog(
            onAccept = {
                showNoLocationDialog = false
            }
        )
    }

    var showNoLocationServicesDialog by remember { mutableStateOf(false) }
    if (showNoLocationServicesDialog) {
        NoLocationServicesDialog(
            onClose = {
                showNoLocationServicesDialog = false
            }
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(it, modifier = Modifier.systemBarsPadding())
        }
    ) { innerPadding ->
        Box {
            FullscreenMap(
                orderState = orderState,
                cameraPositionState = cameraPositionState,
                innerPadding = innerPadding,
                pharmacies = pharmacies,
                onClickMarker = {
                    onShowBottomSheet(PharmacySearchSheetContentState.PharmacySelected(it))
                }
            )

            MapOverlay(
                showSearchButton = showSearchButton,
                isLoading = pharmacySearchController.isLoading,
                onSearch = {
                    if (!pharmacySearchController.isLoading) {
                        scope.launch {
                            onShowSearchButton(false)

                            val radius = SphericalUtil.computeDistanceBetween(
                                cameraPositionState.projection?.visibleRegion?.latLngBounds?.northeast,
                                cameraPositionState.projection?.visibleRegion?.latLngBounds?.southwest
                            ) / 2.0

                            if (it) {
                                // search here button
                                val l = cameraPositionState.position.target
                                pharmacySearchController.search(
                                    pharmacySearchController.searchState.name,
                                    pharmacySearchController.searchState.filter.copy(nearBy = false),
                                    Location(latitude = l.latitude, longitude = l.longitude),
                                    radius
                                )
                            } else {
                                // find me button
                                pharmacySearchController.search(
                                    pharmacySearchController.searchState.name,
                                    pharmacySearchController.searchState.filter.copy(nearBy = true),
                                    radiusInMeter = radius
                                )
                            }.also {
                                when (it) {
                                    PharmacySearchController.SearchQueryResult.NoLocationPermission -> {
                                        locationPermissionLauncher.launch(locationPermissions)
                                    }

                                    PharmacySearchController.SearchQueryResult.NoLocationFound -> {
                                        showNoLocationDialog = true
                                        onShowSearchButton(true)
                                    }

                                    PharmacySearchController.SearchQueryResult.NoLocationServicesEnabled -> {
                                        showNoLocationServicesDialog = true
                                        onShowSearchButton(true)
                                    }

                                    else -> {
                                    }
                                }
                            }
                        }
                    }
                },
                onClickFilter = {
                    onShowBottomSheet(PharmacySearchSheetContentState.FilterSelected)
                },
                onBack = onBack
            )
        }
    }
}

@Composable
private fun CameraAnimation(
    cameraPositionState: CameraPositionState,
    pharmacySearchController: PharmacySearchController,
    pharmacies: List<PharmacyUseCaseData.Pharmacy>,
    onShowSearchButton: () -> Unit
) {
    var lastMarkerCenter by remember { mutableStateOf(Berlin) }
    val isMoving by derivedStateOf { cameraPositionState.isMoving }

    val moveDistance = with(LocalDensity.current) { 24.dp.roundToPx() }
    LaunchedEffect(isMoving) {
        if (!isMoving) {
            cameraPositionState.projection?.let { projection ->
                val latLng =
                    (
                        pharmacySearchController.searchState.locationMode as?
                            PharmacyUseCaseData.LocationMode.Enabled
                        )?.toLatLng()
                        ?: lastMarkerCenter
                val d = SphericalUtil.computeDistanceBetween(cameraPositionState.position.target, latLng)
                val a = projection.fromScreenLocation(Point(0, 0))
                val b = projection.fromScreenLocation(Point(moveDistance, 0))
                if (d >= SphericalUtil.computeDistanceBetween(a, b)) {
                    onShowSearchButton()
                }
            }
        }
    }

    val padding = with(LocalDensity.current) { PaddingDefaults.XXLarge.roundToPx() }
    LaunchedEffect(pharmacies) {
        if (pharmacySearchController.searchState.filter.nearBy) {
            val latLng =
                (pharmacySearchController.searchState.locationMode as? PharmacyUseCaseData.LocationMode.Enabled)
                    ?.toLatLng()
                    ?: lastMarkerCenter

            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(latLng, MyLocationZoomLevel),
                durationMs = 730
            )
        } else if (pharmacies.isNotEmpty()) {
            val bounds = LatLngBounds.builder().apply {
                pharmacies.forEach {
                    include(it.location!!.toLatLng())
                }
            }.build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, padding), durationMs = 730)
            lastMarkerCenter = bounds.center
        }
    }
}

@Composable
private fun FullscreenMap(
    orderState: PharmacyOrderState,
    cameraPositionState: CameraPositionState,
    innerPadding: PaddingValues,
    pharmacies: List<PharmacyUseCaseData.Pharmacy>,
    onClickMarker: (PharmacyUseCaseData.Pharmacy) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedPharmacy by remember(orderState.selectedPharmacy) {
        mutableStateOf(orderState.selectedPharmacy)
    }

    GoogleMap(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        locationSource = remember { locationSourceOnce(context, scope) },
        cameraPositionState = cameraPositionState,
        uiSettings = remember {
            MapUiSettings(
                compassEnabled = false,
                indoorLevelPickerEnabled = false,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = false,
                rotationGesturesEnabled = false,
                scrollGesturesEnabled = true,
                scrollGesturesEnabledDuringRotateOrZoom = true,
                tiltGesturesEnabled = false,
                zoomControlsEnabled = false,
                zoomGesturesEnabled = true
            )
        },
        properties = remember {
            MapProperties(
                isMyLocationEnabled = true
            )
        },
        contentPadding = WindowInsets.Companion.systemBars.asPaddingValues(),
        content = {
            key(pharmacies) {
                MapsContent(
                    pharmacyMapsResult = pharmacies,
                    onClick = {
                        selectedPharmacy = it
                        onClickMarker(it)
                    }
                )
            }
            key(selectedPharmacy) {
                val markerIcon = remember { BitmapDescriptorFactory.fromResource(R.drawable.maps_marker_red) }
                selectedPharmacy?.let { pharmacy ->
                    pharmacy.location?.let { location ->
                        val latLng = LatLng(location.latitude, location.longitude)
                        Marker(
                            state = rememberMarkerState(
                                position = latLng
                            ),
                            onClick = {
                                selectedPharmacy = pharmacy
                                onClickMarker(pharmacy)
                                false
                            },
                            icon = markerIcon,
                            zIndex = 9999f
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun MapsContent(
    pharmacyMapsResult: List<PharmacyUseCaseData.Pharmacy>,
    onClick: (PharmacyUseCaseData.Pharmacy) -> Unit
) {
    val markerIcon = remember { BitmapDescriptorFactory.fromResource(R.drawable.maps_marker) }
    val markerIconGrey = remember { BitmapDescriptorFactory.fromResource(R.drawable.maps_marker_grey) }
    pharmacyMapsResult.mapNotNull { pharmacy ->
        pharmacy.location?.let { location ->
            val latLng = LatLng(location.latitude, location.longitude)
            Marker(
                state = rememberMarkerState(
                    position = latLng
                ),
                icon = if (pharmacy.ready) markerIcon else markerIconGrey,
                onClick = {
                    onClick(pharmacy)
                    false
                }
            )
        }
    }
}

@Composable
private fun BoxScope.MapOverlay(
    showSearchButton: Boolean,
    isLoading: Boolean,
    onSearch: (Boolean) -> Unit,
    onClickFilter: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = PaddingDefaults.Small, end = PaddingDefaults.Medium)
            .systemBarsPadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBack
        ) {
            Box(
                Modifier
                    .size(32.dp)
                    .shadow(2.dp, CircleShape)
                    .background(AppTheme.colors.neutral100, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = null,
                    tint = AppTheme.colors.primary600,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        IconButton(
            modifier = Modifier
                .size(48.dp)
                .shadow(2.dp, CircleShape)
                .border(1.dp, AppTheme.colors.neutral300, CircleShape)
                .background(AppTheme.colors.neutral100, CircleShape),
            onClick = onClickFilter
        ) {
            Icon(
                Icons.Outlined.Tune,
                contentDescription = null,
                tint = AppTheme.colors.primary600,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    Column(
        Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .systemBarsPadding()
    ) {
        IconButton(
            modifier = Modifier
                .align(Alignment.End)
                .padding(horizontal = PaddingDefaults.Medium)
                .padding(bottom = 80.dp)
                .size(56.dp)
                .shadow(2.dp, CircleShape)
                .border(1.dp, AppTheme.colors.neutral300, CircleShape)
                .background(AppTheme.colors.neutral100, CircleShape),
            onClick = {
                onSearch(false)
            }
        ) {
            Crossfade(
                targetState = isLoading
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (it) {
                        CircularProgressIndicator(
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            Icons.Rounded.MyLocation,
                            contentDescription = null,
                            tint = AppTheme.colors.primary600,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            visible = showSearchButton,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top) + slideInVertically(),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top) + slideOutVertically()
        ) {
            PrimaryButtonSmall(
                onClick = {
                    onSearch(true)
                },
                modifier = Modifier
                    .padding(bottom = PaddingDefaults.Large)
            ) {
                Icon(Icons.Rounded.Search, null)
                SpacerSmall()
                Text(stringResource(R.string.pharmacy_maps_search_here_button))
            }
        }
    }
}

fun locationSourceOnce(context: Context, coroutineScope: CoroutineScope) = object : LocationSource {
    private var currentListener = MutableStateFlow<LocationSource.OnLocationChangedListener?>(null)

    init {
        coroutineScope.launch {
            currentListener.collectLatest { listener ->
                if (listener != null) {
                    queryNativeLocation(context)?.let {
                        listener.onLocationChanged(it)
                    }
                }
            }
        }
    }

    override fun activate(listener: LocationSource.OnLocationChangedListener) {
        currentListener.value = listener
    }

    override fun deactivate() {
        currentListener.value = null
    }
}
