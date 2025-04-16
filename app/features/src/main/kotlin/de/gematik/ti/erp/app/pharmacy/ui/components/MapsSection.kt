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

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Coordinates
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults

@Suppress("FunctionName")
internal fun LazyListScope.MapsTitle() {
    item {
        Text(
            stringResource(R.string.pharmacy_maps_header),
            style = AppTheme.typography.subtitle1,
            modifier = Modifier
                .padding(top = PaddingDefaults.XXLarge, bottom = PaddingDefaults.Medium)
                .padding(horizontal = PaddingDefaults.Medium)
                .semanticsHeading()
        )
    }
}

@Suppress("FunctionName")
internal fun LazyListScope.MapsTile(
    previewCoordinates: Coordinates,
    previewMap: PharmacyMap,
    onClick: () -> Unit
) {
    item {
        val positionState = remember(previewCoordinates) {
            PositionState(
                position = previewCoordinates,
                zoom = DefaultZoomLevel
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SizeDefaults.twentythreefold)
                .padding(horizontal = PaddingDefaults.Medium)
        ) {
            previewMap.Map(
                modifier = Modifier,
                isFullScreen = false,
                onClick = onClick,
                positionState = positionState,
                settings = PharmacySettings.Default,
                properties = PharmacyProperties.Default,
                contentPaddingValues = PaddingValues(),
                onZoomStateChanged = {
                    // zoom state cannot be changed on preview
                },
                content = null
            )
        }
    }
}
