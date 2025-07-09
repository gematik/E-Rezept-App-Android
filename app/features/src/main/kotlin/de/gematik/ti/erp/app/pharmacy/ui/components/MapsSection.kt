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

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Coordinates
import de.gematik.ti.erp.app.semantics.semanticsMergeDescendants
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults

internal fun LazyListScope.MapsSection(
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
        val description = stringResource(R.string.maps_unaccessible_description)
        Column(
            modifier = Modifier
                .semanticsMergeDescendants {}
                .clearAndSetSemantics {
                    contentDescription = description
                    heading()
                }
                .padding(horizontal = PaddingDefaults.Medium)
        ) {
            Text(
                stringResource(R.string.pharmacy_maps_header),
                style = AppTheme.typography.subtitle1,
                modifier = Modifier
                    .padding(top = PaddingDefaults.XXLarge, bottom = PaddingDefaults.Medium)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SizeDefaults.twentythreefold)
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
}
