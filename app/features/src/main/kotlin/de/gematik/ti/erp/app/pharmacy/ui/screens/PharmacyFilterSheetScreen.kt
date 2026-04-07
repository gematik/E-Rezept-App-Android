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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.animated.LoadingIndicatorLine
import de.gematik.ti.erp.app.base.openSettingsAsNewActivity
import de.gematik.ti.erp.app.core.R
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
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacyFilterServiceOption
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacyFilterServiceSection
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacyOnSiteFeatureOption
import de.gematik.ti.erp.app.pharmacy.ui.preview.PharmacyFilterSheetScreenPreviewData
import de.gematik.ti.erp.app.pharmacy.ui.preview.PharmacyFilterSheetScreenPreviewParameterProvider
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.FilterSheetChip
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.LocationPermissionDeniedDialog
import de.gematik.ti.erp.app.utils.compose.LocationServicesNotAvailableDialog
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.compose.TextButton
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbarScaffold
import de.gematik.ti.erp.app.utils.extensions.show

class PharmacyFilterSheetScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    val graphController: PharmacyGraphController
) : BottomSheetScreen(forceToMaxHeight = true) {
    @Composable
    override fun Content() {
        val context = LocalContext.current

        val dialog = LocalDialog.current

        val snackbar = LocalSnackbarScaffold.current

        val uiScope = uiScope

        val filter by graphController.filter()

        var isLoading by remember { mutableStateOf(false) }

        val locationNotFoundEvent = graphController.locationNotFoundEvent

        val locationLoadingEvent = graphController.locationLoadingEvent

        val askLocationPermissionEvent = graphController.askLocationPermissionEvent

        // this is shown only from search list screen
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
            if (BuildConfigExtension.isInternalDebug) {
                snackbar.show(message = "Location not found", scope = uiScope)
            }
        }

        locationLoadingEvent.listen { isLoadingEvent ->
            if (BuildConfigExtension.isInternalDebug) {
                isLoading = isLoadingEvent
            }
        }

        askLocationPermissionEvent.listen {
            locationPermissionLauncher.launch(locationPermissions)
        }

        val showServiceDescriptions by graphController.showServiceDescriptions.collectAsStateWithLifecycle(false)
        val selectedServiceCodes by graphController.selectedServiceCodes.collectAsStateWithLifecycle(emptySet())

        PharmacyFilterSheetScreenContent(
            filter = filter,
            isNearbyFilter = isNearbyFilter,
            isLoading = isLoading,
            navWithStartButton = navWithStartButton,
            showDescriptions = showServiceDescriptions,
            selectedServiceCodes = selectedServiceCodes,
            onToggleDescriptions = { graphController.toggleShowServiceDescriptions() },
            onToggleServiceOption = { option ->
                graphController.toggleAvailableService(option)
            },
            onToggleOnSiteFeature = { graphController.toggleOnSiteFeature(it) },
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
            onClickResetFilter = {
                graphController.reset()
            },
            onBack = {
                navController.popBackStack()
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PharmacyFilterSheetScreenContent(
    filter: PharmacyUseCaseData.Filter,
    isNearbyFilter: Boolean,
    navWithStartButton: Boolean,
    isLoading: Boolean,
    showDescriptions: Boolean,
    selectedServiceCodes: Set<String>,
    onToggleDescriptions: () -> Unit,
    onToggleServiceOption: (PharmacyFilterServiceOption) -> Unit,
    onToggleOnSiteFeature: (PharmacyOnSiteFeatureOption) -> Unit,
    onClickFilter: (Boolean, FilterType) -> Unit,
    onClickStartSearch: () -> Unit,
    onClickResetFilter: () -> Unit,
    onBack: () -> Unit
) {
    AnimatedVisibility(isLoading) {
        LoadingIndicatorLine(isLoading)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium)
            .padding(top = PaddingDefaults.Medium)
    ) {
        Text(
            stringResource(R.string.search_pharmacies_filter_header),
            color = AppTheme.colors.neutral900,
            style = AppTheme.typography.h5
        )
        SpacerMedium()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Preference section
            FilterSectionHeader(stringResource(R.string.search_pharmacies_section_preferences))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.ShortMedium),
                verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Tiny)
            ) {
                FilterSheetChip(
                    text = stringResource(R.string.search_pharmacies_filter_open_now),
                    checked = filter.openNow
                ) { isChecked -> onClickFilter(isChecked, FilterType.OPEN_NOW) }

                if (isNearbyFilter) {
                    FilterSheetChip(
                        text = stringResource(R.string.search_pharmacies_filter_nearby),
                        checked = filter.nearBy
                    ) { isChecked ->
                        onClickFilter(isChecked, NEARBY)
                    }
                }

                FilterSheetChip(
                    text = stringResource(R.string.search_pharmacies_filter_recently_used),
                    checked = filter.recentlyUsed
                ) { isChecked -> onClickFilter(isChecked, FilterType.RECENTLY_USED) }
            }

            SpacerXXLarge()

            // Redemption section
            FilterSectionHeader(stringResource(R.string.search_pharmacies_section_redeem_path))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.ShortMedium),
                verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Tiny)
            ) {
                // Abholung == pickup
                FilterSheetChip(
                    text = stringResource(R.string.search_pharmacies_filter_pickup),
                    checked = filter.pickup
                ) { isChecked ->
                    onClickFilter(isChecked, FilterType.PICKUP)
                }

                // Versand == online service
                FilterSheetChip(
                    text = stringResource(R.string.search_pharmacies_filter_online_service),
                    checked = filter.onlineService
                ) { isChecked -> onClickFilter(isChecked, FilterType.ONLINE_SERVICE) }

                // Botendienst == delivery service
                FilterSheetChip(
                    text = stringResource(R.string.search_pharmacies_filter_delivery_service),
                    checked = filter.deliveryService
                ) { isChecked ->
                    onClickFilter(isChecked, FilterType.DELIVERY_SERVICE)
                    if (isChecked && !isNearbyFilter) onClickFilter(true, NEARBY)
                }
            }
            if (filter.deliveryService) {
                SpacerSmall()
                Text(
                    text = stringResource(R.string.search_pharmacies_filter_delivery_hint),
                    style = AppTheme.typography.caption1,
                    color = AppTheme.colors.neutral700
                )
            }

            SpacerXXLarge()

            FilterSectionHeader(stringResource(R.string.search_pharmacies_section_local))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.ShortMedium),
                verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Tiny)
            ) {
                PharmacyOnSiteFeatureOption.entries.forEach { option ->
                    FilterSheetChip(
                        text = stringResource(option.label),
                        checked = option.code in filter.onSiteFeatures
                    ) { onToggleOnSiteFeature(option) }
                }
            }

            SpacerXXLarge()

            PharmacyFilterServiceSection(
                showDescriptions = showDescriptions,
                selectedServiceCodes = selectedServiceCodes,
                onToggleDescriptions = onToggleDescriptions,
                onToggleServiceOption = onToggleServiceOption
            )
            SpacerLarge()
        }

        SpacerMedium()

        // Bottom buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
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
            SpacerSmall()
            TextButton(
                leadingIcon = Icons.AutoMirrored.Default.RotateLeft,
                onClick = onClickResetFilter,
                buttonText = stringResource(R.string.search_pharmacies_reset_filter)
            )
        }
        SpacerLarge()
    }
}

@Composable
private fun FilterSectionHeader(title: String) {
    Text(
        text = title,
        style = AppTheme.typography.h6,
        color = AppTheme.colors.neutral900,
        modifier = Modifier.semanticsHeading()
    )
}

private fun NavBackStackEntry.getNearbyFilter(): Boolean =
    arguments?.getBoolean(PHARMACY_NAV_NEARBY_FILTER) ?: false

private fun NavBackStackEntry.getNavWithSearchButton(): Boolean =
    arguments?.getBoolean(PHARMACY_NAV_WITH_START_BUTTON) ?: false

@LightDarkPreview
@Composable
fun PharmacyFilterSheetScreenPreview(
    @PreviewParameter(
        PharmacyFilterSheetScreenPreviewParameterProvider::class
    ) previewData: PharmacyFilterSheetScreenPreviewData
) {
    PreviewAppTheme {
        PharmacyFilterSheetScreenContent(
            filter = previewData.filter,
            isLoading = previewData.isLoading,
            isNearbyFilter = previewData.isNearbyFilter,
            navWithStartButton = previewData.navWithStartButton,
            showDescriptions = previewData.showDescriptions,
            selectedServiceCodes = previewData.selectedServiceCodes,
            onToggleDescriptions = {},
            onToggleServiceOption = {},
            onToggleOnSiteFeature = {},
            onClickFilter = { _, _ -> },
            onClickStartSearch = {},
            onClickResetFilter = {},
            onBack = {}
        )
    }
}
