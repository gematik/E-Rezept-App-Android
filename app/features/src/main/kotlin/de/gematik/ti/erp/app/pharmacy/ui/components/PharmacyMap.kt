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

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.fhir.model.Coordinates
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Calendar

internal const val DefaultZoomLevel = 10f

/**
 * This class is a wrapper for the GoogleMap composable.
 * It is used to abstract the GoogleMap composable and make it easier to test the PharmacyStartScreen.
 * It also provides a way to change the implementation of the GoogleMap
 */
interface PharmacyMap {
    @Composable
    fun Map(
        modifier: Modifier,
        isFullScreen: Boolean,
        positionState: PositionState,
        settings: PharmacySettings,
        properties: PharmacyProperties,
        contentPaddingValues: PaddingValues,
        onZoomStateChanged: (Float) -> Unit,
        onClick: (() -> Unit)?,
        content: (@Composable () -> Unit)?
    )
}

data class PositionState(val position: Coordinates, val zoom: Float)

data class PharmacyProperties(
    val isBuildingEnabled: Boolean = true,
    val isIndoorEnabled: Boolean = false,
    val isMyLocationEnabled: Boolean = false,
    val isTrafficEnabled: Boolean = false,
    val mapType: MapType = MapType.NORMAL,
    val maxZoomPreference: Float = 21.0f,
    val minZoomPreference: Float = 3.0f
) {
    companion object {
        val Default = PharmacyProperties()
    }
}

data class PharmacySettings(
    val compassEnabled: Boolean = false,
    val indoorLevelPickerEnabled: Boolean = false,
    val mapToolbarEnabled: Boolean = false,
    val myLocationButtonEnabled: Boolean = false,
    val rotationGesturesEnabled: Boolean = false,
    val scrollGesturesEnabled: Boolean = false,
    val scrollGesturesEnabledDuringRotateOrZoom: Boolean = false,
    val tiltGesturesEnabled: Boolean = false,
    val zoomControlsEnabled: Boolean = false,
    val zoomGesturesEnabled: Boolean = false
) {
    companion object {
        val Default = PharmacySettings()
        val FullScreen = PharmacySettings().copy(
            zoomControlsEnabled = false,
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true,
            scrollGesturesEnabledDuringRotateOrZoom = true
        )
    }
}

// This is a mock implementation of the PharmacyMap for previews
class MockMap : PharmacyMap {
    @Composable
    override fun Map(
        modifier: Modifier,
        isFullScreen: Boolean,
        positionState: PositionState,
        settings: PharmacySettings,
        properties: PharmacyProperties,
        contentPaddingValues: PaddingValues,
        onZoomStateChanged: (Float) -> Unit,
        onClick: (() -> Unit)?,
        content: (@Composable () -> Unit)?
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.green600),
            color = AppTheme.colors.neutral600
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    color = AppTheme.colors.neutral100,
                    text = "A map will be shown here"
                )
            }
        }
    }
}

class GooglePharmacyMap : PharmacyMap {

    private val cameraPositionStateFlow = MutableStateFlow<CameraPositionState?>(null)

    val cameraPositionState
        @Composable
        get() = cameraPositionStateFlow.collectAsStateWithLifecycle()

    @Composable
    private fun rememberCameraPositionState(
        positionState: PositionState
    ) = remember(positionState) {
        val state = CameraPositionState(
            position = CameraPosition.fromLatLngZoom(positionState.position.toLatLng(), positionState.zoom)
        )
        cameraPositionStateFlow.value = state
        state
    }

    @Composable
    override fun Map(
        modifier: Modifier,
        isFullScreen: Boolean,
        positionState: PositionState,
        settings: PharmacySettings,
        properties: PharmacyProperties,
        contentPaddingValues: PaddingValues,
        onZoomStateChanged: (Float) -> Unit,
        onClick: (() -> Unit)?,
        content: (@Composable () -> Unit)?
    ) {
        val context = LocalContext.current
        val cameraPositionState = rememberCameraPositionState(positionState = positionState)

        // Remember the last zoom level to detect changes
        var lastZoomLevel by remember(cameraPositionState.position.zoom) {
            mutableFloatStateOf(cameraPositionState.position.zoom)
        }

        LaunchedEffect(lastZoomLevel) {
            if (cameraPositionState.position.zoom != lastZoomLevel) {
                // Zoom level has changed due to user interaction
                lastZoomLevel = cameraPositionState.position.zoom
                onZoomStateChanged(lastZoomLevel)
            }
        }

        val mapStyleOptions = when {
            // isTodayEaster() -> MapStyleOptions.loadRawResourceStyle(context, R.raw.maps_easter_style)
            // isTodayChristmas() -> MapStyleOptions.loadRawResourceStyle(context, R.raw.maps_christmas_style)
            isSystemInDarkTheme() -> MapStyleOptions.loadRawResourceStyle(context, R.raw.maps_dark_style)
            else -> null
        }

        val mapProperties = remember(properties) {
            MapProperties(
                isBuildingEnabled = properties.isBuildingEnabled,
                isIndoorEnabled = properties.isIndoorEnabled,
                isMyLocationEnabled = properties.isMyLocationEnabled,
                isTrafficEnabled = properties.isTrafficEnabled,
                latLngBoundsForCameraTarget = null,
                mapStyleOptions = mapStyleOptions,
                mapType = properties.mapType,
                maxZoomPreference = properties.maxZoomPreference,
                minZoomPreference = properties.minZoomPreference
            )
        }

        val mapSettings = remember(settings) {
            MapUiSettings(
                compassEnabled = settings.compassEnabled,
                indoorLevelPickerEnabled = settings.indoorLevelPickerEnabled,
                mapToolbarEnabled = settings.mapToolbarEnabled,
                myLocationButtonEnabled = settings.myLocationButtonEnabled,
                rotationGesturesEnabled = settings.rotationGesturesEnabled,
                scrollGesturesEnabled = settings.scrollGesturesEnabled,
                scrollGesturesEnabledDuringRotateOrZoom = settings.scrollGesturesEnabledDuringRotateOrZoom,
                tiltGesturesEnabled = settings.tiltGesturesEnabled,
                zoomControlsEnabled = settings.zoomControlsEnabled,
                zoomGesturesEnabled = settings.zoomGesturesEnabled
            )
        }

        GoogleMap(
            modifier = modifier.let {
                when {
                    isFullScreen -> it
                    else -> it.clip(RoundedCornerShape(SizeDefaults.double))
                }
            },
            onMapClick = { onClick?.invoke() },
            onMyLocationClick = { onClick?.invoke() },
            onPOIClick = { onClick?.invoke() },
            cameraPositionState = cameraPositionState,
            uiSettings = mapSettings,
            properties = mapProperties,
            contentPadding = contentPaddingValues,
            content = {
                content?.invoke()
            }
        )
    }

    companion object {
        fun Coordinates.toLatLng() = LatLng(latitude, longitude)

        @Suppress("MagicNumber", "UnusedPrivateMember")
        private fun isTodayEaster(): Boolean {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)

            val a = year % 19
            val b = year / 100
            val c = year % 100
            val d = b / 4
            val e = b % 4
            val f = (b + 8) / 25
            val g = (b - f + 1) / 3
            val h = (19 * a + b - d - g + 15) % 30
            val i = c / 4
            val k = c % 4
            val l = (32 + 2 * e + 2 * i - h - k) % 7
            val m = (a + 11 * h + 22 * l) / 451
            val month = (h + l - 7 * m + 114) / 31
            val day = ((h + l - 7 * m + 114) % 31) + 1

            val todayMonth = calendar.get(Calendar.MONTH) + 1 // Note: +1 because Calendar.MONTH is zero-based
            val todayDay = calendar.get(Calendar.DAY_OF_MONTH)

            return todayMonth == month && todayDay == day
        }

        @Suppress("MagicNumber", "UnusedPrivateMember")
        private fun isTodayChristmas(): Boolean {
            val calendar = Calendar.getInstance()
            // Calendar.MONTH is zero-based, so December is 11
            val todayMonth = calendar.get(Calendar.MONTH)
            val todayDay = calendar.get(Calendar.DAY_OF_MONTH)

            // Check if it's December 25th
            return todayMonth == Calendar.DECEMBER && todayDay == 25
        }
    }
}
