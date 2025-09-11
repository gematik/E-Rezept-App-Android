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

package de.gematik.ti.erp.app.eurezept.ui.screens

import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.base.openSettingsAsNewActivity
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.eurezept.domin.model.Country
import de.gematik.ti.erp.app.eurezept.navigation.EuRoutes
import de.gematik.ti.erp.app.eurezept.presentation.EuSharedViewModel
import de.gematik.ti.erp.app.eurezept.presentation.rememberEuCountrySelectionController
import de.gematik.ti.erp.app.eurezept.ui.component.CountryListLoadingSection
import de.gematik.ti.erp.app.eurezept.ui.preview.EuCountrySelectionPreviewData
import de.gematik.ti.erp.app.eurezept.ui.preview.EuCountrySelectionPreviewParameterProvider
import de.gematik.ti.erp.app.eurezept.util.EuLocationPermissionDeniedDialog
import de.gematik.ti.erp.app.eurezept.util.EuLocationServicesNotAvailableDialog
import de.gematik.ti.erp.app.eurezept.util.EuRedeemScaffold
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.permissions.getLocationPermissionLauncher
import de.gematik.ti.erp.app.permissions.locationPermissions
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.SpacerXXLargeMedium
import de.gematik.ti.erp.app.utils.compose.EmptyScreenComponent
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.animatedElevationStickySearchField
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbar
import de.gematik.ti.erp.app.utils.uistate.UiState

internal class EuCountrySelectionScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: EuSharedViewModel
) : Screen() {
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val snackbar = LocalSnackbar.current
        val focusManager = LocalFocusManager.current
        val dialog = LocalDialog.current
        val screenController = rememberEuCountrySelectionController()
        val lazyListState = rememberLazyListState()

        val uiState by screenController.uiState.collectAsStateWithLifecycle()
        val searchQuery by screenController.searchQuery.collectAsStateWithLifecycle()
        val detectedCountrySupported by screenController.isDetectedCountrySupported.collectAsStateWithLifecycle()

        val locationPermissionLauncher = getLocationPermissionLauncher { isLocationEnabled ->
            screenController.onLocationPermissionResult(isLocationEnabled) { country ->
                if (country != null) {
                    graphController.setSelectedCountry(country)
                    navController.popBackStack()
                }
            }
        }

        EuLocationPermissionDeniedDialog(
            event = screenController.permissionDeniedEvent,
            dialog = dialog,
            onClick = {}
        )

        EuLocationServicesNotAvailableDialog(
            event = screenController.serviceDisabledEvent,
            dialog = dialog,
            onClickDismiss = {},
            onClickSettings = {
                context.openSettingsAsNewActivity(
                    action = Settings.ACTION_LOCATION_SOURCE_SETTINGS,
                    isSimpleIntent = true
                )
            }
        )

        screenController.locationNotFoundEvent.listen {
            snackbar.show(context.getString(R.string.location_not_found))
        }

        screenController.countryNotFoundEvent.listen {
            snackbar.show(context.getString(R.string.eu_country_not_found_message))
        }

        screenController.geocoderNotAvailableEvent.listen {
            snackbar.show(context.getString(R.string.eu_geocoder_not_available_message))
        }

        screenController.noCountriesAvailableEvent.listen {
            navController.navigate(EuRoutes.EUAvailabilityScreen.route)
        }

        EuCountrySelectionScreenSection(
            listState = lazyListState,
            uiState = uiState,
            detectedCountrySupported = detectedCountrySupported,
            searchQuery = searchQuery,
            focusManager = focusManager,
            onBack = { navController.popBackStack() },
            onSearchQueryChange = { screenController.updateSearchQuery(it) },
            onClearSearch = { screenController.updateSearchQuery() },
            onUseLocation = {
                locationPermissionLauncher.launch(locationPermissions)
            },
            onRetry = { screenController.onRetry() },
            onResetToContent = { screenController.onResetToContent() },
            onCountrySelect = { country ->
                graphController.setSelectedCountry(country)
                navController.popBackStack()
            }
        )
    }
}

@Composable
private fun EuCountrySelectionHeader() {
    Column(modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)) {
        SpacerMedium()
        Text(
            text = stringResource(R.string.eu_country_selection_country_select_heading),
            style = AppTheme.typography.h6,
            color = AppTheme.colors.neutral900,
            fontWeight = FontWeight.Bold
        )
        SpacerMedium()
        Text(
            text = stringResource(R.string.eu_country_selection_select_country_desc),
            style = AppTheme.typography.body2,
            fontWeight = FontWeight.Medium,
            color = AppTheme.colors.neutral600
        )
        SpacerMedium()
    }
}

@Composable
private fun EuCountrySelectionEmptyState() {
    EmptyScreenComponent(
        modifier = Modifier.padding(top = PaddingDefaults.XXLarge),
        title = stringResource(R.string.eu_prescription_search_empty_title),
        body = stringResource(R.string.eu_prescription_search_empty_text),
        image = {
            Image(
                painter = painterResource(id = R.drawable.girl_red_oh_no),
                contentDescription = null,
                modifier = Modifier.size(SizeDefaults.twentyfold)
            )
        },
        button = {}
    )
}

@Composable
fun EuCountrySelectionScreenSection(
    listState: LazyListState,
    uiState: UiState<List<Country>>,
    detectedCountrySupported: Boolean,
    searchQuery: String,
    onBack: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onUseLocation: () -> Unit,
    onCountrySelect: (Country) -> Unit,
    onRetry: () -> Unit,
    onResetToContent: () -> Unit,
    focusManager: FocusManager
) {
    EuRedeemScaffold(
        listState = listState,
        onBack = onBack,
        onCancel = {},
        cancelButtonText = "",
        topBarTitle = ""

    ) { contentPadding ->
        val description = stringResource(id = R.string.eu_search_countries)
        val contentDescriptionText = stringResource(id = R.string.a11y_deleted_text)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag(TestTag.Settings.InsuranceCompanyList.InsuranceSelectionContent),
            state = listState,
            contentPadding = contentPadding
        ) {
            item { EuCountrySelectionHeader() }

            animatedElevationStickySearchField(
                lazyListState = listState,
                indexOfPreviousItemInList = 0,
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                onRemoveValue = onClearSearch,
                focusManager = focusManager,
                description = description,
                placeholderText = description,
                contentDescriptionText = contentDescriptionText
            )

            item {
                UiStateMachine(
                    state = uiState,
                    onLoading = {
                        EuCountrySelectionLoadingContent(paddingValues = contentPadding)
                    },
                    onEmpty = { EuCountrySelectionEmptyState() },
                    onError = {
                        EuCountrySelectionErrorContent(
                            paddingValues = contentPadding,
                            onRetry = onRetry
                        )
                    },
                    onContent = { state ->
                        if (!detectedCountrySupported) {
                            EuCountrySelectionDetectedCountryNotSupportedContent(
                                paddingValues = contentPadding,
                                onResetToContent = onResetToContent
                            )
                        } else {
                            EuCountrySelectionContent(
                                paddingValues = contentPadding,
                                countries = state,
                                onUseLocation = onUseLocation,
                                onCountrySelect = onCountrySelect
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EuCountrySelectionErrorContent(
    paddingValues: PaddingValues,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = PaddingDefaults.Medium),
        verticalArrangement = Arrangement.spacedBy(SizeDefaults.zero)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = SizeDefaults.double),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.eu_country_server_not_responding_title),
                style = AppTheme.typography.subtitle1,
                color = AppTheme.colors.neutral900,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            SpacerTiny()
            Text(
                text = stringResource(R.string.eu_country_server_not_responding_message),
                style = AppTheme.typography.body2,
                color = AppTheme.colors.neutral600,
                textAlign = TextAlign.Center
            )

            SpacerMedium()

            TextButton(
                onClick = onRetry,
                colors = androidx.compose.material.ButtonDefaults.textButtonColors(
                    contentColor = AppTheme.colors.primary700
                )
            ) {
                Icon(Icons.Rounded.Refresh, null)
                SpacerSmall()
                Text(
                    text = stringResource(R.string.eu_country_retry_button),
                    style = AppTheme.typography.button,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EuCountrySelectionLoadingContent(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = PaddingDefaults.Medium)
    ) {
        CountryListLoadingSection()
    }
}

@Composable
fun EuCountrySelectionDetectedCountryNotSupportedContent(
    paddingValues: PaddingValues,
    onResetToContent: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = PaddingDefaults.Medium)
    ) {
        UseLocationButton(
            onClick = onResetToContent,
            isDetectedCountrySupported = false,
            modifier = Modifier.fillMaxWidth()
        )
        SpacerMedium()

        SpacerXXLargeMedium()
        Text(
            text = stringResource(R.string.eu_country_detected_not_supported_message),
            style = AppTheme.typography.body2,
            textAlign = TextAlign.Center,
            color = AppTheme.colors.neutral600,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun UseLocationButton(
    onClick: () -> Unit,
    isDetectedCountrySupported: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = PaddingDefaults.ShortMedium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.Right
    ) {
        if (!isDetectedCountrySupported) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = AppTheme.colors.primary700,
                modifier = Modifier.size(SizeDefaults.triple)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = null,
                tint = AppTheme.colors.primary700,
                modifier = Modifier.size(SizeDefaults.triple)
            )
        }
        SpacerTiny()
        Text(
            text = if (!isDetectedCountrySupported) {
                stringResource(R.string.eu_country_location_stop_using)
            } else {
                stringResource(R.string.eu_country_selection_country_current_location)
            },
            style = AppTheme.typography.body1,
            color = AppTheme.colors.primary700,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EuCountrySelectionContent(
    paddingValues: PaddingValues,
    countries: List<Country>,
    onUseLocation: () -> Unit,
    onCountrySelect: (Country) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = PaddingDefaults.Medium),
        verticalArrangement = Arrangement.spacedBy(SizeDefaults.zero)
    ) {
        UseLocationButton(
            onClick = onUseLocation,
            modifier = Modifier.fillMaxWidth()
        )
        SpacerMedium()

        countries.forEach { country ->
            CountryListItem(
                country = country,
                onClick = { onCountrySelect(country) }
            )
        }
    }
}

@Composable
fun CountryListItem(
    country: Country,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = PaddingDefaults.Medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
    ) {
        Box(
            modifier = Modifier
                .size(SizeDefaults.fourfold)
                .background(
                    color = AppTheme.colors.neutral000,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = country.flagEmoji,
                fontSize = SizeDefaults.triple.value.sp,
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = country.name,
            style = AppTheme.typography.body1,
            color = AppTheme.colors.neutral900,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@LightDarkPreview
@Composable
fun EuCountrySelectionScreenPreview(
    @PreviewParameter(EuCountrySelectionPreviewParameterProvider::class)
    previewData: EuCountrySelectionPreviewData
) {
    val lazyListState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    PreviewTheme {
        EuCountrySelectionScreenSection(
            listState = lazyListState,
            uiState = previewData.uiState,
            searchQuery = previewData.searchQuery,
            detectedCountrySupported = !previewData.detectedCountryNotInSupportedList,
            onBack = {},
            onSearchQueryChange = {},
            onClearSearch = {},
            onUseLocation = {},
            onRetry = {},
            onResetToContent = {},
            onCountrySelect = {},
            focusManager = focusManager
        )
    }
}
