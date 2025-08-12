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

package de.gematik.ti.erp.app.cardwall.ui.screens

import android.app.Dialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.valentinilk.shimmer.shimmer
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes.processGidEventData
import de.gematik.ti.erp.app.cardwall.navigation.CardWallScreen
import de.gematik.ti.erp.app.cardwall.presentation.CardWallSharedViewModel
import de.gematik.ti.erp.app.cardwall.presentation.rememberExternalAuthenticatorListController
import de.gematik.ti.erp.app.cardwall.ui.components.GematikErrorDialog
import de.gematik.ti.erp.app.cardwall.ui.preview.HealthInsuranceDataPreviewParameterProvider
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.idp.model.HealthInsuranceData
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.shimmer.RowTextShimmer
import de.gematik.ti.erp.app.shimmer.SquareShapeShimmer
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.topbar.AnimatedTitleContent
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.EmptyScreenComponent
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.LoadingDialog
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.animatedElevationStickySearchField
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty
import de.gematik.ti.erp.app.utils.letNotNullOnCondition
import de.gematik.ti.erp.app.utils.uistate.UiState

class CardWallGidListScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val sharedViewModel: CardWallSharedViewModel
) : CardWallScreen() {
    @Composable
    override fun Content() {
        val navProfileId = navBackStackEntry.arguments?.getString(CardWallRoutes.CARD_WALL_NAV_PROFILE_ID)
        letNotNullOnCondition(
            first = navProfileId,
            condition = {
                navProfileId.isNotNullOrEmpty()
            }
        ) { id ->
            sharedViewModel.setProfileId(id)
        }

        val profileId by sharedViewModel.profileId.collectAsStateWithLifecycle()
        val profileIsPKV by sharedViewModel.profileIsPkv.collectAsStateWithLifecycle()
        val intentHandler = LocalIntentHandler.current
        val dialog = LocalDialog.current
        val focusManager = LocalFocusManager.current
        var loadingDialog: Dialog? = remember { null }

        val listState = rememberLazyListState()

        val controller = rememberExternalAuthenticatorListController(profileId)
        val healthInsuranceList by controller.healthInsuranceDataList.collectAsStateWithLifecycle()
        val searchValue by controller.searchValue.collectAsStateWithLifecycle()

        val gidEventData = navBackStackEntry.processGidEventData()
        letNotNullOnCondition(
            first = gidEventData,
            condition = { gidEventData?.authenticatorName.isNotNullOrEmpty() }
        ) { gidData ->
            controller.onFilterList(gidData.authenticatorName)
        }

        val onBack by rememberUpdatedState { navController.popBackStack() }

        val authorizationWithExternalAppEvent = controller.authorizationWithExternalAppInBackgroundEvent
        val redirectUriEvent = controller.redirectUriEvent
        val redirectErrorEvent = controller.redirectUriErrorEvent
        val redirectGematikErrorEvent = controller.redirectUriGematikErrorEvent

        authorizationWithExternalAppEvent.listen { isStarted ->
            if (isStarted) {
                dialog.show {
                    loadingDialog = it
                    LoadingDialog { it.dismiss() }
                }
            } else {
                loadingDialog?.dismiss()
            }
        }

        redirectUriEvent.listen { (redirectUri) ->
            intentHandler.tryStartingExternalHealthInsuranceAuthenticationApp(
                redirect = redirectUri,
                onSuccess = {
                    navController.popBackStack(CardWallRoutes.subGraphName(), inclusive = true)
                },
                onFailure = {
                    dialog.show {
                        ErezeptAlertDialog(
                            title = stringResource(R.string.gid_external_app_missing_title),
                            body = stringResource(R.string.gid_external_app_missing_description),
                            okText = stringResource(R.string.ok),
                            onDismissRequest = { it.dismiss() }
                        )
                    }
                }
            )
        }

        redirectErrorEvent.listen {
            dialog.show {
                ErezeptAlertDialog(
                    title = stringResource(R.string.main_fasttrack_error_title),
                    body = stringResource(R.string.main_fasttrack_error_info),
                    okText = stringResource(R.string.ok),
                    onDismissRequest = it::dismiss
                )
            }
        }

        redirectGematikErrorEvent.listen { responseError ->
            dialog.show {
                GematikErrorDialog(error = responseError) {
                    it.dismiss()
                }
            }
        }

        BackHandler {
            onBack()
        }
        CardWallGidListScreenScaffold(
            profileId = profileId,
            listState = listState,
            focusManager = focusManager,
            healthInsuranceAppIdps = healthInsuranceList,
            onFilterList = controller::onFilterList,
            onRemoveFilterList = controller::onRemoveFilterList,
            searchValue = searchValue,
            reloadHealthInsuranceAppList = { controller.getHealthInsuranceAppList(profileIsPKV) },
            startAuthorizationWithExternal = controller::startAuthorizationWithExternal,
            onCancel = {
                navController.popBackStack(CardWallRoutes.subGraphName(), inclusive = true)
            },
            onBack = { onBack() },
            onNavigateToHelp = {
                navController.navigate(CardWallRoutes.CardWallGidHelpScreen.route)
            }
        )
    }
}

@Composable
private fun CardWallGidListScreenScaffold(
    profileId: ProfileIdentifier,
    listState: LazyListState,
    focusManager: FocusManager,
    healthInsuranceAppIdps: UiState<List<HealthInsuranceData>>,
    searchValue: String,
    onFilterList: (String) -> Unit,
    onRemoveFilterList: () -> Unit,
    reloadHealthInsuranceAppList: () -> Unit,
    startAuthorizationWithExternal: (ProfileIdentifier, HealthInsuranceData) -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            NavigationTopAppBar(
                modifier = Modifier,
                navigationMode = NavigationBarMode.Back,
                title = {
                    AnimatedTitleContent(
                        listState = listState,
                        indexOfTitleItemInList = 0,
                        title = stringResource(R.string.cardwall_gid_list_header)
                    )
                },
                elevation = 0.dp,
                backLabel = stringResource(R.string.back),
                closeLabel = stringResource(R.string.cancel),
                onBack = onBack,
                actions = {
                    TextButton(onClick = onCancel) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    ) { innerPadding ->
        val searchBarDescription = stringResource(R.string.cardwall_gid_list_insurance_searchbar)
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            state = listState
        ) {
            cardWallGidListScreenTitleSection { onNavigateToHelp() }
            cardWallGidListScreenSearchBar(
                focusManager = focusManager,
                value = searchValue,
                onValueChange = onFilterList,
                onRemoveValue = onRemoveFilterList,
                listState = listState,
                indexOfPreviousItemInList = 1,
                searchBarDescription = searchBarDescription
            )
            gidListScreenContent(
                profileId = profileId,
                reloadHealthInsuranceAppList = reloadHealthInsuranceAppList,
                healthInsuranceAppIdps = healthInsuranceAppIdps,
                onClickHealthInsuranceIdp = startAuthorizationWithExternal
            )
        }
    }
}

private fun LazyListScope.gidListScreenContent(
    profileId: ProfileIdentifier,
    reloadHealthInsuranceAppList: () -> Unit,
    healthInsuranceAppIdps: UiState<List<HealthInsuranceData>>,
    @Requirement(
        "O.Auth_4#4",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "The user selects the insurance company and the authentication process starts"
    )
    onClickHealthInsuranceIdp: (ProfileIdentifier, HealthInsuranceData) -> Unit
) {
    item {
        val onClickLabel = stringResource(R.string.cardwall_gid_list_insurance_click_description)
        UiStateMachine(
            state = healthInsuranceAppIdps,
            onLoading = {
                CardWallGidListScreenInsuranceListLoadingComponent()
            },
            onEmpty = {
                EmptyScreenComponent(
                    modifier = Modifier.padding(top = PaddingDefaults.XXLarge),
                    title = stringResource(R.string.cardwall_gid_list_insurance_search_empty_title),
                    body = stringResource(R.string.cardwall_gid_list_insurance_search_empty_body),
                    button = {}
                )
            },
            onError = {
                ErrorScreenComponent(
                    modifier = Modifier.padding(top = PaddingDefaults.XXLarge),
                    title = stringResource(R.string.cdw_fasttrack_error_title),
                    body = stringResource(R.string.cdw_fasttrack_error_info),
                    onClickRetry = reloadHealthInsuranceAppList
                )
            },
            onContent = {
                CardWallGidListScreenInsuranceListContentComponent(
                    healthInsuranceAppIdps = healthInsuranceAppIdps,
                    profileId = profileId,
                    onClickHealthInsuranceIdp = onClickHealthInsuranceIdp,
                    onClickLabel = onClickLabel
                )
            }
        )
    }
}

private fun LazyListScope.cardWallGidListScreenTitleSection(
    onNavigateToHelp: () -> Unit
) {
    item {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.surface)
                .padding(top = PaddingDefaults.Medium)
                .padding(horizontal = PaddingDefaults.Medium)
        ) {
            Text(
                modifier = Modifier.semanticsHeading(),
                text = stringResource(R.string.cardwall_gid_list_header),
                style = AppTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
        }
    }
    item { // two items to allow the functionality of the animated title and searchbar
        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.surface)
                .padding(
                    start = PaddingDefaults.Medium,
                    end = PaddingDefaults.Medium,
                    top = PaddingDefaults.Small
                )
        ) {
            Text(
                stringResource(R.string.cardwall_gid_list_body),
                style = AppTheme.typography.body2,
                color = AppTheme.colors.neutral600
            )
            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = onNavigateToHelp,
                content = {
                    Text(
                        stringResource(R.string.cardwall_gid_help_button),
                        style = AppTheme.typography.body1
                    )
                    SpacerTiny()
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = AppTheme.colors.primary700
                    )
                }
            )
        }
    }
}

private fun LazyListScope.cardWallGidListScreenSearchBar(
    listState: LazyListState,
    focusManager: FocusManager,
    value: String,
    indexOfPreviousItemInList: Int = 0,
    onValueChange: (String) -> Unit,
    onRemoveValue: () -> Unit,
    searchBarDescription: String
) {
    animatedElevationStickySearchField(
        lazyListState = listState,
        focusManager = focusManager,
        value = value,
        onValueChange = onValueChange,
        onRemoveValue = onRemoveValue,
        description = searchBarDescription,
        indexOfPreviousItemInList = indexOfPreviousItemInList
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CardWallGidListScreenInsuranceListContentComponent(
    healthInsuranceAppIdps: UiState<List<HealthInsuranceData>>,
    profileId: ProfileIdentifier,
    onClickHealthInsuranceIdp: (ProfileIdentifier, HealthInsuranceData) -> Unit,
    onClickLabel: String
) {
    healthInsuranceAppIdps.data?.let {
        val placerHolderPainter = painterResource(R.drawable.ic_insurance_placeholder)
        val loadingInsurancePainter = painterResource(R.drawable.loading_insurance)
        Column {
            it.forEach { healthInsuranceCompany ->
                ListItem(
                    modifier = Modifier
                        .clickable(
                            role = Role.Button,
                            onClickLabel = onClickLabel
                        ) {
                            onClickHealthInsuranceIdp(profileId, healthInsuranceCompany)
                        }
                        .padding(vertical = PaddingDefaults.Small),
                    icon = {
                        AsyncImage(
                            modifier = Modifier
                                .size(SizeDefaults.fivefold),
                            model = ImageRequest
                                .Builder(LocalContext.current)
                                .data(healthInsuranceCompany.logo)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            placeholder = loadingInsurancePainter,
                            error = placerHolderPainter
                        )
                    },
                    text = {
                        Text(
                            text = healthInsuranceCompany.name,
                            style = AppTheme.typography.body1
                        )
                    },
                    trailing = {
                        Icon(
                            Icons.Rounded.ChevronRight,
                            null,
                            tint = AppTheme.colors.neutral600
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun CardWallGidListScreenInsuranceListLoadingComponent() {
    val loadingItemList = List(10) { it }
    Column {
        loadingItemList.forEach {
            ListItem(
                modifier = Modifier
                    .shimmer()
                    .padding(vertical = PaddingDefaults.Small),
                icon = {
                    SquareShapeShimmer()
                },
                text = {
                    RowTextShimmer()
                },
                trailing = {
                    Icon(
                        Icons.Rounded.ChevronRight,
                        null,
                        tint = AppTheme.colors.neutral600
                    )
                }
            )
        }
    }
}

@LightDarkPreview
@Composable
fun GidListScreenScaffoldPreview(
    @PreviewParameter(HealthInsuranceDataPreviewParameterProvider::class) healthInsuranceAppIds:
        UiState<List<HealthInsuranceData>>
) {
    PreviewAppTheme {
        val focusManager = LocalFocusManager.current
        CardWallGidListScreenScaffold(
            profileId = "123",
            listState = rememberLazyListState(),
            healthInsuranceAppIdps = healthInsuranceAppIds,
            searchValue = "",
            onFilterList = {},
            focusManager = focusManager,
            onRemoveFilterList = {},
            reloadHealthInsuranceAppList = {},
            startAuthorizationWithExternal = { _, _ -> },
            onCancel = {},
            onBack = {},
            onNavigateToHelp = {}
        )
    }
}
