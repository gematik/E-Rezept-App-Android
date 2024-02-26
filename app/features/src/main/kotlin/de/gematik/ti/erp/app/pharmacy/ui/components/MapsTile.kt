/*
 * Copyright (c) 2024 gematik GmbH
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

@file:Suppress("FunctionName")

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults

private val Berlin = LatLng(52.51947562977698, 13.404335795642881)
private const val DefaultZoomLevel = 12.2f

internal fun LazyListScope.MapsTitle() {
    item {
        Text(
            stringResource(R.string.pharmacy_maps_header),
            style = AppTheme.typography.subtitle1,
            modifier = Modifier
                .padding(top = PaddingDefaults.XXLarge, bottom = PaddingDefaults.Medium)
                .padding(horizontal = PaddingDefaults.Medium)
        )
    }
}

internal fun LazyListScope.MapsTile(
    onClick: () -> Unit
) {
    item {
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(Berlin, DefaultZoomLevel)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SizeDefaults.twentythreefold)
                .padding(horizontal = PaddingDefaults.Medium)
        ) {
            GoogleMap(
                modifier = Modifier
                    .clip(RoundedCornerShape(SizeDefaults.double)),
                onMapClick = { onClick() },
                onMyLocationClick = { onClick() },
                onPOIClick = { onClick() },
                cameraPositionState = cameraPositionState,
                uiSettings = remember { switchedOffMapUiSettings }
            )
        }
    }
}

private val switchedOffMapUiSettings = MapUiSettings(
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
