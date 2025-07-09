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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
fun BoxScope.pharmacyMapsOverlay(
    showSearchButton: Boolean,
    isLoading: Boolean,
    onSearch: (Boolean) -> Unit,
    onClickFilter: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(
                top = PaddingDefaults.Medium,
                start = PaddingDefaults.Small,
                end = PaddingDefaults.Medium
            )
            .systemBarsPadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBack
        ) {
            Box(
                Modifier
                    .size(SizeDefaults.fourfold)
                    .shadow(SizeDefaults.quarter, CircleShape)
                    .background(AppTheme.colors.neutral100, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = null,
                    tint = AppTheme.colors.primary700,
                    modifier = Modifier.size(SizeDefaults.triple)
                )
            }
        }

        IconButton(
            modifier = Modifier
                .size(SizeDefaults.sixfold)
                .shadow(SizeDefaults.quarter, CircleShape)
                .border(SizeDefaults.eighth, AppTheme.colors.neutral300, CircleShape)
                .background(AppTheme.colors.neutral100, CircleShape),
            onClick = onClickFilter
        ) {
            Icon(
                Icons.Outlined.Tune,
                contentDescription = null,
                tint = AppTheme.colors.primary700,
                modifier = Modifier.size(SizeDefaults.triple)
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
                .padding(bottom = SizeDefaults.tenfold)
                .size(SizeDefaults.sevenfold)
                .shadow(SizeDefaults.quarter, CircleShape)
                .border(SizeDefaults.eighth, AppTheme.colors.neutral300, CircleShape)
                .background(AppTheme.colors.neutral100, CircleShape),
            onClick = {
                onSearch(true)
            }
        ) {
            Crossfade(
                targetState = isLoading,
                label = "isLoading"
            ) { isLoadingState ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    when {
                        isLoadingState -> CircularProgressIndicator(strokeWidth = SizeDefaults.half)

                        else -> Icon(
                            Icons.Rounded.MyLocation,
                            contentDescription = null,
                            tint = AppTheme.colors.primary700,
                            modifier = Modifier.size(SizeDefaults.triple)
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
                    onSearch(false)
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

@LightDarkPreview
@Composable
internal fun PharmacyMapsOverlaySearchPreview() {
    PreviewAppTheme {
        Box(Modifier.fillMaxSize()) {
            pharmacyMapsOverlay(
                showSearchButton = true,
                isLoading = false,
                onSearch = {},
                onClickFilter = {},
                onBack = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
internal fun PharmacyMapsOverlayLoadingPreview() {
    PreviewAppTheme {
        Box(Modifier.fillMaxSize()) {
            pharmacyMapsOverlay(
                showSearchButton = false,
                isLoading = true,
                onSearch = {},
                onClickFilter = {},
                onBack = {}
            )
        }
    }
}
