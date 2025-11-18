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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.ListItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.authentication.observer.ChooseAuthenticationNavigationEventsListener
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.datetime.rememberErpTimeFormatter
import de.gematik.ti.erp.app.error.ErrorScreenComponent
import de.gematik.ti.erp.app.eurezept.domain.model.EuPrescription
import de.gematik.ti.erp.app.eurezept.domain.model.EuPrescriptionType
import de.gematik.ti.erp.app.eurezept.presentation.EuSharedViewModel
import de.gematik.ti.erp.app.eurezept.presentation.rememberEuPrescriptionSelectionController
import de.gematik.ti.erp.app.eurezept.ui.component.EuAvatar
import de.gematik.ti.erp.app.eurezept.ui.component.MarkAsEuRedeemableByPatientAuthorizationDialog
import de.gematik.ti.erp.app.eurezept.ui.preview.EuPrescriptionSelectionPreviewData
import de.gematik.ti.erp.app.eurezept.ui.preview.EuPrescriptionSelectionPreviewParameterProvider
import de.gematik.ti.erp.app.listitem.GemListItemDefaults
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.EmptyScreenComponent
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.fullscreen.FullScreenLoadingIndicator
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.uistate.UiState

internal class EuPrescriptionSelectionScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: EuSharedViewModel
) : Screen() {
    @Composable
    override fun Content() {
        val controller = rememberEuPrescriptionSelectionController()
        val lazyListState = rememberLazyListState()
        val snackbarHostState = remember { SnackbarHostState() }
        val profileData by graphController.activeProfile.collectAsStateWithLifecycle()
        val dialog = LocalDialog.current
        val uiState by controller.uiState.collectAsStateWithLifecycle()
        val markedEuPrescriptions by controller.markedEuPrescriptions.collectAsStateWithLifecycle()
        val onBack by rememberUpdatedState {
            graphController.setSelectedPrescriptions(markedEuPrescriptions)
            navController.popBackStack()
        }
        BackHandler { onBack() }
        ChooseAuthenticationNavigationEventsListener(
            controller = controller,
            navController = navController,
            dialogScaffold = dialog
        )
        with(controller) {
            onBiometricAuthenticationSuccessEvent.listen {
                graphController.setSelectedPrescriptions(markedEuPrescriptions)
            }
            markAsEuRedeemableByPatientAuthorizationErrorEvent.listen { euRedeemError ->
                dialog.show {
                    MarkAsEuRedeemableByPatientAuthorizationDialog(
                        euRedeemError = euRedeemError,
                        onClick = it::dismiss
                    )
                }
            }
        }
        EuPrescriptionSelectionScaffold(
            listState = lazyListState,
            uiState = uiState,
            profileData = profileData.data,
            snackbarHostState = snackbarHostState,
            onBack = { onBack() },
            onRetry = { controller.getEuPrescriptions() },
            onPrescriptionToggle = { euPrescription ->
                controller.togglePrescriptionSelection(euPrescription)
            }
        )
    }
}

@Composable
fun EuPrescriptionSelectionScaffold(
    listState: LazyListState,
    uiState: UiState<List<EuPrescription>>,
    profileData: ProfilesUseCaseData.Profile?,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onPrescriptionToggle: (EuPrescription) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = SnackbarHostState()
) {
    AnimatedElevationScaffold(
        modifier = modifier,
        topBarTitle = stringResource(R.string.eu_prescription_selection_title),
        navigationMode = NavigationBarMode.Back,
        listState = listState,
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        onBack = onBack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {}
    ) { paddingValues ->
        UiStateMachine(
            state = uiState,
            onLoading = {
                Center {
                    FullScreenLoadingIndicator()
                }
            },
            onEmpty = {
                EmptyScreenComponent(
                    modifier = Modifier.padding(paddingValues),
                    title = stringResource(R.string.eu_redeem_prescription_selection_empty_title),
                    body = stringResource(R.string.eu_redeem_prescription_selection_empty_subtitle),
                    image = {
                        Image(
                            painter = painterResource(id = R.drawable.girl_red_oh_no),
                            contentDescription = null,
                            modifier = Modifier.size(SizeDefaults.twentyfold)
                        )
                    },
                    button = {}
                )
            },
            onError = { error ->
                ErrorScreenComponent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    titleText = stringResource(R.string.generic_error_title),
                    bodyText = error.message ?: stringResource(R.string.generic_error_info),
                    tryAgainText = stringResource(R.string.generic_error_retry),
                    onClickRetry = onRetry
                )
            },
            onContent = { prescriptions ->
                EuPrescriptionSelectionContent(
                    paddingValues = paddingValues,
                    listState = listState,
                    prescriptions = prescriptions,
                    profileData = profileData,
                    onPrescriptionToggle = onPrescriptionToggle
                )
            }
        )
    }
}

@Composable
fun EuPrescriptionSelectionContent(
    paddingValues: PaddingValues,
    listState: LazyListState,
    prescriptions: List<EuPrescription>,
    profileData: ProfilesUseCaseData.Profile?,
    onPrescriptionToggle: (EuPrescription) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
    ) {
        item {
            ProfileSection(profileData = profileData)
        }

        item {
            PrescriptionSelectionCard(
                prescriptions = prescriptions,
                onPrescriptionToggle = onPrescriptionToggle
            )
        }
    }
}

@Composable
fun PrescriptionSelectionCard(
    prescriptions: List<EuPrescription>,
    onPrescriptionToggle: (EuPrescription) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium),
        backgroundColor = Color.Transparent,
        border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral300),
        shape = RoundedCornerShape(SizeDefaults.double),
        elevation = SizeDefaults.zero
    ) {
        Column {
            prescriptions.forEach { prescription ->
                PrescriptionListItemInContainer(
                    prescription = prescription,
                    onToggle = { onPrescriptionToggle(prescription) }
                )
            }
        }
    }
}

@Composable
private fun PrescriptionListItemInContainer(
    prescription: EuPrescription,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = rememberErpTimeFormatter()
    val selectedText = stringResource(R.string.a11y_prescription_selected)
    val notSelectedText = stringResource(R.string.a11y_prescription_not_selected)
    val notAvailableText = stringResource(R.string.a11y_prescription_not_available)

    ListItem(
        modifier = modifier
            .clickable(enabled = prescription.type == EuPrescriptionType.EuRedeemable, role = Role.Button) { onToggle() }
            .semantics(mergeDescendants = true) {
                stateDescription = if (prescription.type == EuPrescriptionType.EuRedeemable) {
                    if (prescription.isMarkedAsEuRedeemableByPatientAuthorization) selectedText else notSelectedText
                } else {
                    notAvailableText
                }
            },
        colors = GemListItemDefaults.gemListItemColors(),
        leadingContent = {
            key(prescription.isLoading) {
                if (prescription.type == EuPrescriptionType.EuRedeemable && prescription.isLoading) {
                    CircularProgressIndicator(modifier.size(SizeDefaults.triple))
                } else {
                    PrescriptionIcon(prescription)
                }
            }
        },
        headlineContent = {
            Text(
                text = prescription.name,
                style = AppTheme.typography.body1,
                color = AppTheme.colors.neutral900,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            Text(
                text = when (prescription.type) {
                    EuPrescriptionType.EuRedeemable -> prescription.expiryDate?.let {
                        stringResource(
                            R.string.eu_prescription_selection_available_until,
                            remember { formatter.date(prescription.expiryDate) }
                        )
                    } ?: stringResource(R.string.eu_prescription_selection_available)

                    EuPrescriptionType.Scanned -> stringResource(R.string.eu_prescription_selection_scanned_not_available)
                    EuPrescriptionType.FreeText -> stringResource(R.string.eu_prescription_selection_freetext_not_available)
                    EuPrescriptionType.BTM -> stringResource(R.string.eu_prescription_selection_narcotic_not_available)
                    EuPrescriptionType.Ingredient -> stringResource(R.string.eu_prescription_selection_ingredient_not_available)
                    EuPrescriptionType.Unknown -> stringResource(R.string.eu_prescription_selection_not_available)
                },
                style = AppTheme.typography.body2,
                color = AppTheme.colors.neutral600
            )
        }
    )
}

@Composable
private fun PrescriptionIcon(prescription: EuPrescription) {
    when {
        prescription.type == EuPrescriptionType.EuRedeemable && prescription.isMarkedAsError -> {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = AppTheme.colors.yellow600,
                modifier = Modifier.size(SizeDefaults.triple)
            )
        }

        prescription.type == EuPrescriptionType.EuRedeemable && prescription.isMarkedAsEuRedeemableByPatientAuthorization -> {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = AppTheme.colors.primary700,
                modifier = Modifier.size(SizeDefaults.triple)
            )
        }

        prescription.type == EuPrescriptionType.EuRedeemable && !prescription.isMarkedAsEuRedeemableByPatientAuthorization -> {
            Icon(
                imageVector = Icons.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint = AppTheme.colors.neutral400,
                modifier = Modifier.size(SizeDefaults.triple)
            )
        }

        else -> {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = AppTheme.colors.red600,
                modifier = Modifier.size(SizeDefaults.triple)
            )
        }
    }
}

@Composable
fun ProfileSection(modifier: Modifier = Modifier, profileData: ProfilesUseCaseData.Profile?) {
    profileData?.let {
        ListItem(
            modifier = modifier,
            colors = GemListItemDefaults.gemListItemColors(),
            leadingContent = {
                EuAvatar(
                    profile = profileData,
                    size = SizeDefaults.sixfold,
                    emptyIcon = Icons.Rounded.PersonOutline
                )
            },
            headlineContent = {
                Text(
                    text = profileData.name,
                    style = AppTheme.typography.subtitle2,
                    color = AppTheme.colors.neutral900,
                    fontWeight = FontWeight.Bold
                )
            }
        )
    }
}

@LightDarkPreview
@Composable
fun EuPrescriptionSelectionContentPreview(
    @PreviewParameter(EuPrescriptionSelectionPreviewParameterProvider::class)
    previewData: EuPrescriptionSelectionPreviewData
) {
    PreviewTheme {
        EuPrescriptionSelectionContent(
            paddingValues = PaddingValues(SizeDefaults.zero),
            listState = rememberLazyListState(),
            prescriptions = previewData.prescriptions,
            profileData = previewData.profileData,
            onPrescriptionToggle = { }
        )
    }
}
