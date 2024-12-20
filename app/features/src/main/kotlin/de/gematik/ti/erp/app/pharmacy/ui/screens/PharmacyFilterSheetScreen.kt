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

import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.flowlayout.FlowRow
import de.gematik.ti.erp.app.base.openSettingsAsNewActivity
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.BottomSheetScreen
import de.gematik.ti.erp.app.permissions.getLocationPermissionLauncher
import de.gematik.ti.erp.app.permissions.locationPermissions
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes.EMPTY_TASK_ID
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes.PHARMACY_NAV_NEARBY_FILTER
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes.PHARMACY_NAV_WITH_START_BUTTON
import de.gematik.ti.erp.app.pharmacy.presentation.FilterType
import de.gematik.ti.erp.app.pharmacy.presentation.FilterType.NEARBY
import de.gematik.ti.erp.app.pharmacy.presentation.PharmacyGraphController
import de.gematik.ti.erp.app.pharmacy.ui.components.LocationPermissionDeniedDialog
import de.gematik.ti.erp.app.pharmacy.ui.components.LocationServicesNotAvailableDialog
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.Chip
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbar

class PharmacyFilterSheetScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    val graphController: PharmacyGraphController
) : BottomSheetScreen(forceToMaxHeight = false) {
    @Composable
    override fun Content() {
        val context = LocalContext.current

        val dialog = LocalDialog.current

        val snackbar = LocalSnackbar.current

        val filter by graphController.filter()

        val locationNotFoundEvent = graphController.locationNotFoundEvent

        val askLocationPermissionEvent = graphController.askLocationPermissionEvent

        val isNearbyFilter = remember { navBackStackEntry.getNearbyFilter() }

        val navWithStartButton = remember { navBackStackEntry.getNavWithSearchButton() }

        val locationPermissionLauncher = getLocationPermissionLauncher(
            onPermissionResult = {
                graphController.onLocationPermissionResult(it)
            }
        )

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
            snackbar.show("Location not found")
        }

        askLocationPermissionEvent.listen {
            locationPermissionLauncher.launch(locationPermissions)
        }

        PharmacyFilterSheetScreenContent(
            filter = filter,
            isNearbyFilter = isNearbyFilter,
            navWithStartButton = navWithStartButton,
            onClickFilter = { isFilterChecked, filterType ->
                when (filterType) {
                    NEARBY -> {
                        when {
                            isFilterChecked -> graphController.checkLocationServiceAndPermission(context)
                            else -> graphController.updateFilter(type = filterType, clearLocation = true)
                        }
                    }

                    else -> graphController.updateFilter(type = filterType)
                }
            },
            onClickStartSearch = {
                navController.navigate(PharmacyRoutes.PharmacySearchListScreen.path(taskId = EMPTY_TASK_ID))
            },
            onBack = {
                navController.popBackStack()
            }
        )
    }
}

@Composable
private fun PharmacyFilterSheetScreenContent(
    filter: PharmacyUseCaseData.Filter,
    isNearbyFilter: Boolean,
    navWithStartButton: Boolean,
    onClickFilter: (Boolean, FilterType) -> Unit,
    onClickStartSearch: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = PaddingDefaults.Medium)
            .padding(bottom = PaddingDefaults.Medium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.search_pharmacies_filter_header),
                style = AppTheme.typography.h5
            )
        }
        SpacerMedium()
        Column(modifier = Modifier.verticalScroll(rememberScrollState(), true)) {
            FlowRow(
                mainAxisSpacing = PaddingDefaults.Small,
                crossAxisSpacing = PaddingDefaults.Small
            ) {
                if (isNearbyFilter) {
                    Chip(
                        stringResource(R.string.search_pharmacies_filter_nearby),
                        closable = false,
                        checked = filter.nearBy
                    ) {
                        onClickFilter(it, NEARBY)
                    }
                }
                Chip(
                    stringResource(R.string.search_pharmacies_filter_open_now),
                    closable = false,
                    checked = filter.openNow
                ) {
                    onClickFilter(it, FilterType.OPEN_NOW)
                }
                Chip(
                    stringResource(R.string.search_pharmacies_filter_delivery_service),
                    closable = false,
                    checked = filter.deliveryService
                ) {
                    onClickFilter(it, FilterType.DELIVERY_SERVICE)
                    if (it) {
                        onClickFilter(it, NEARBY)
                    }
                }
                Chip(
                    stringResource(R.string.search_pharmacies_filter_online_service),
                    closable = false,
                    checked = filter.onlineService
                ) {
                    onClickFilter(it, FilterType.ONLINE_SERVICE)
                }
            }
            SpacerMedium()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                PrimaryButtonSmall(
                    onClick = {
                        onBack()
                        if (navWithStartButton) {
                            onClickStartSearch()
                        }
                    }
                ) {
                    Text(stringResource(R.string.search_pharmacies_start_search))
                }
            }
            SpacerLarge()
        }
    }
}

private fun NavBackStackEntry.getNearbyFilter(): Boolean =
    arguments?.getBoolean(PHARMACY_NAV_NEARBY_FILTER) ?: false

private fun NavBackStackEntry.getNavWithSearchButton(): Boolean =
    arguments?.getBoolean(PHARMACY_NAV_WITH_START_BUTTON) ?: false

@LightDarkPreview
@Composable
fun PharmacyFilterSheetScreenPreview() {
    PreviewAppTheme {
        PharmacyFilterSheetScreenContent(
            filter = PharmacyUseCaseData.Filter(
                nearBy = true,
                openNow = false,
                deliveryService = false,
                onlineService = true
            ),
            isNearbyFilter = true,
            navWithStartButton = true,
            onClickFilter = { _, _ ->
            },
            onClickStartSearch = {},
            onBack = {}
        )
    }
}
