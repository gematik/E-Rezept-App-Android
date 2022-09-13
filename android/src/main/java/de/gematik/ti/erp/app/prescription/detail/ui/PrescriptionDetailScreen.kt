/*
 * Copyright (c) 2022 gematik GmbH
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

@file:OptIn(ExperimentalMaterialApi::class)

package de.gematik.ti.erp.app.prescription.detail.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.cardwall.ui.PrimaryButtonSmall
import de.gematik.ti.erp.app.core.LocalAuthenticator
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionDetailsNavigationScreens
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.ui.DirectAssignmentChip
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceErrorState
import de.gematik.ti.erp.app.prescription.ui.SubstitutionAllowedChip
import de.gematik.ti.erp.app.prescription.ui.expiryOrAcceptString
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerShortMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerXLarge
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.annotatedLinkStringLight
import de.gematik.ti.erp.app.utils.compose.dateWithIntroductionString
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberViewModel

const val MissingValue = "---"

@Composable
fun PrescriptionDetailsScreen(
    taskId: String,
    mainNavController: NavController
) {
    val viewModel: PrescriptionDetailsViewModel by rememberViewModel()

    val prescription by produceState<PrescriptionData.Prescription?>(null) {
        viewModel.screenState(taskId).collect {
            value = it
        }
    }

    var selectedMedication: PrescriptionData.Medication? by remember { mutableStateOf(null) }
    var selectedIngredient: SyncedTaskData.Ingredient? by remember { mutableStateOf(null) }

    prescription?.let { pres ->

        val navController = rememberNotSaveableNavController()
        NavHost(
            navController = navController,
            startDestination = PrescriptionDetailsNavigationScreens.Overview.route
        ) {
            composable(PrescriptionDetailsNavigationScreens.Overview.route) {
                PrescriptionDetailsWithScaffold(
                    prescription = pres,
                    viewModel = viewModel,
                    navController = navController,
                    onClickMedication = {
                        selectedMedication = it
                        navController.navigate(PrescriptionDetailsNavigationScreens.Medication.path())
                    },
                    onBack = { mainNavController.popBackStack() }
                )
            }
            composable(PrescriptionDetailsNavigationScreens.MedicationOverview.route) {
                MedicationOverviewScreen(
                    prescription = pres as PrescriptionData.Synced,
                    onClickMedication = {
                        selectedMedication = it
                        navController.navigate(PrescriptionDetailsNavigationScreens.Medication.path())
                    },
                    onBack = { mainNavController.popBackStack() }
                )
            }
            composable(PrescriptionDetailsNavigationScreens.Medication.route) {
                SyncedMedicationDetailScreen(
                    prescription = pres as PrescriptionData.Synced,
                    medication = requireNotNull(selectedMedication),
                    onClickIngredient = {
                        selectedIngredient = it
                        navController.navigate(PrescriptionDetailsNavigationScreens.Ingredient.path())
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(PrescriptionDetailsNavigationScreens.Ingredient.route) {
                IngredientScreen(
                    ingredient = requireNotNull(selectedIngredient),
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(PrescriptionDetailsNavigationScreens.Patient.route) {
                PatientScreen(
                    prescription = pres as PrescriptionData.Synced,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(PrescriptionDetailsNavigationScreens.Prescriber.route) {
                PrescriberScreen(
                    prescription = pres as PrescriptionData.Synced,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(PrescriptionDetailsNavigationScreens.Accident.route) {
                AccidentInformation(
                    prescription = pres as PrescriptionData.Synced,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(PrescriptionDetailsNavigationScreens.Organization.route) {
                OrganizationScreen(
                    prescription = pres as PrescriptionData.Synced,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(PrescriptionDetailsNavigationScreens.TechnicalInformation.route) {
                TechnicalInformation(
                    prescription = pres,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PrescriptionDetailsWithScaffold(
    prescription: PrescriptionData.Prescription,
    viewModel: PrescriptionDetailsViewModel,
    navController: NavHostController,
    onClickMedication: (PrescriptionData.Medication) -> Unit,
    onBack: () -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val listState = rememberLazyListState()
    // val shareHandler = rememberSharePrescriptionController()

    val sheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        confirmStateChange = { it != ModalBottomSheetValue.HalfExpanded }
    )

    val coroutineScope = rememberCoroutineScope()

    var infoBottomSheetContent: PrescriptionDetailBottomSheetContent? by remember { mutableStateOf(null) }

    LaunchedEffect(infoBottomSheetContent) {
        if (infoBottomSheetContent != null) {
            sheetState.show()
        } else {
            sheetState.hide()
        }
    }
    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            Box(
                Modifier
                    .heightIn(min = 56.dp)
                    .navigationBarsPadding()
            ) {
                infoBottomSheetContent?.let {
                    PrescriptionDetailInfoSheetContent(infoContent = it)
                }
            }
        },
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        AnimatedElevationScaffold(
            scaffoldState = scaffoldState,
            listState = listState,
            onBack = onBack,
            topBarTitle = stringResource(R.string.prescription_details),
            navigationMode = NavigationBarMode.Close,
            snackbarHost = { SnackbarHost(it, modifier = Modifier.navigationBarsPadding()) },
            actions = {
                // if (prescription.accessCode != null) {
                //     IconButton(onClick = {
                //         shareHandler.share(taskId = prescription.taskId, prescription.accessCode!!)
                //     }) {
                //         Icon(Icons.Rounded.Share, null, tint = AppTheme.colors.primary700)
                //     }
                // }

                val context = LocalContext.current
                val authenticator = LocalAuthenticator.current
                val deletePrescriptionsHandle = remember {
                    DeletePrescriptions(
                        bridge = viewModel,
                        authenticator = authenticator
                    )
                }

                DeleteAction(prescription) {
                    val deleteState = deletePrescriptionsHandle.deletePrescription(
                        profileId = prescription.profileId,
                        taskId = prescription.taskId
                    )

                    when (deleteState) {
                        is PrescriptionServiceErrorState -> {
                            coroutineScope.launch {
                                deleteErrorMessage(context, deleteState)?.let {
                                    scaffoldState.snackbarHostState.showSnackbar(it)
                                }
                            }
                        }

                        is DeletePrescriptions.State.Deleted -> onBack()
                    }
                }
            }
        ) { innerPadding ->
            when (prescription) {
                is PrescriptionData.Synced ->
                    SyncedPrescriptionOverview(
                        navController = navController,
                        listState = listState,
                        prescription = prescription,
                        onSelectMedication = onClickMedication,
                        onShowInfo = {
                            infoBottomSheetContent = it
                            coroutineScope.launch {
                                sheetState.show()
                            }
                        }
                    )

                is PrescriptionData.Scanned ->
                    ScannedPrescriptionOverview(
                        navController = navController,
                        listState = listState,
                        prescription = prescription,
                        onSwitchRedeemed = {
                            viewModel.redeemScannedTask(taskId = prescription.taskId, redeem = it)
                        },
                        onShowInfo = {
                            infoBottomSheetContent = it
                            coroutineScope.launch {
                                sheetState.show()
                            }
                        }
                    )
            }
        }
    }
}

@Composable
private fun DeleteAction(
    prescription: PrescriptionData.Prescription,
    onClickDelete: suspend () -> Unit
) {
    var showDeletePrescriptionDialog by remember { mutableStateOf(false) }
    var deletionInProgress by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val mutex = MutatorMutex()

    var dropdownExpanded by remember { mutableStateOf(false) }

    val isDeletable by derivedStateOf {
        (prescription as? PrescriptionData.Synced)?.isDeletable ?: true
    }

    IconButton(
        onClick = { dropdownExpanded = true },
        modifier = Modifier
    ) {
        Icon(Icons.Rounded.MoreVert, null, tint = AppTheme.colors.neutral600)
    }
    DropdownMenu(
        expanded = dropdownExpanded,
        onDismissRequest = { dropdownExpanded = false },
        offset = DpOffset(24.dp, 0.dp)
    ) {
        DropdownMenuItem(
            modifier = Modifier,
            enabled = isDeletable,
            onClick = {
                dropdownExpanded = false
                showDeletePrescriptionDialog = true
            }
        ) {
            Text(
                text = stringResource(R.string.pres_detail_dropdown_delete),
                color = AppTheme.colors.red600
            )
        }
    }

    if (showDeletePrescriptionDialog) {
        val info = stringResource(R.string.pres_detail_delete_msg)
        val cancelText = stringResource(R.string.pres_detail_delete_no)
        val actionText = stringResource(R.string.pres_detail_delete_yes)

        CommonAlertDialog(
            header = null,
            info = info,
            cancelText = cancelText,
            actionText = actionText,
            enabled = !deletionInProgress,
            onCancel = {
                showDeletePrescriptionDialog = false
            },
            onClickAction = {
                coroutineScope.launch {
                    mutex.mutate {
                        try {
                            deletionInProgress = true
                            onClickDelete()
                        } finally {
                            showDeletePrescriptionDialog = false
                            deletionInProgress = false
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun SyncedPrescriptionOverview(
    navController: NavController,
    listState: LazyListState,
    prescription: PrescriptionData.Synced,
    onSelectMedication: (PrescriptionData.Medication) -> Unit,
    onShowInfo: (PrescriptionDetailBottomSheetContent) -> Unit
) {
    val noValueText = stringResource(R.string.pres_details_no_value)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
    ) {
        // prescription name
        // prescription kind
        // prescription state
        item {
            SyncedHeader(
                prescription = prescription,
                onShowInfo = onShowInfo
            )
        }

        item {
            val text = when {
                prescription.medicationRequest.additionalFee == SyncedTaskData.AdditionalFee.Exempt ->
                    stringResource(R.string.pres_detail_no)

                prescription.medicationRequest.additionalFee == SyncedTaskData.AdditionalFee.NotExempt ->
                    stringResource(R.string.pres_detail_yes)

                else -> noValueText
            }
            Label(
                text = text,
                label = stringResource(R.string.pres_details_additional_fee),
                onClick = {
                    when {
                        prescription.medicationRequest.additionalFee == SyncedTaskData.AdditionalFee.NotExempt ->
                            onShowInfo(PrescriptionDetailBottomSheetContent.EmergencyFreeNotExempt)

                        prescription.medicationRequest.additionalFee == SyncedTaskData.AdditionalFee.Exempt ->
                            onShowInfo(PrescriptionDetailBottomSheetContent.EmergencyFreeExempt)

                        else -> {}
                    }
                }
            )
        }

        if (prescription.medicationRequest.emergencyFee != null && prescription.medicationRequest.emergencyFee) {
            item {
                Label(
                    text = stringResource(R.string.pres_detail_no),
                    label = stringResource(R.string.pres_details_emergency_fee),
                    onClick = {
                        onShowInfo(PrescriptionDetailBottomSheetContent.EmergencyFree)
                    }
                )
            }
        }

        item {
            Label(
                text = prescription.name,
                label = stringResource(R.string.pres_details_medication),
                onClick = {
                    if (!prescription.isDispensed) {
                        onSelectMedication(PrescriptionData.Medication.Request(prescription.medicationRequest))
                    } else {
                        navController.navigate(PrescriptionDetailsNavigationScreens.MedicationOverview.path())
                    }
                }
            )
        }

        item {
            Label(
                text = prescription.patient.name ?: noValueText,
                label = stringResource(R.string.pres_detail_patient_header),
                onClick = {
                    navController.navigate(PrescriptionDetailsNavigationScreens.Patient.path())
                }
            )
        }

        item {
            Label(
                text = prescription.practitioner.name ?: noValueText,
                label = stringResource(R.string.pres_detail_practitioner_header),
                onClick = {
                    navController.navigate(PrescriptionDetailsNavigationScreens.Prescriber.path())
                }
            )
        }

        item {
            Label(
                text = prescription.organization.name ?: noValueText,
                label = stringResource(R.string.pres_detail_organization_header),
                onClick = {
                    navController.navigate(PrescriptionDetailsNavigationScreens.Organization.path())
                }
            )
        }

        item {
            Label(
                text = stringResource(R.string.pres_detail_accident_header),
                onClick = {
                    navController.navigate(PrescriptionDetailsNavigationScreens.Accident.path())
                }
            )
        }

        item {
            Label(
                text = stringResource(R.string.pres_detail_technical_information),
                onClick = {
                    navController.navigate(PrescriptionDetailsNavigationScreens.TechnicalInformation.path())
                }
            )
        }

        item {
            HealthPortalLink(Modifier.padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.XXLarge))
        }
    }
}

@Composable
fun SyncedHeader(
    prescription: PrescriptionData.Synced,
    onShowInfo: (PrescriptionDetailBottomSheetContent) -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            prescription.name,
            style = AppTheme.typography.h5,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        when {
            prescription.isDirectAssignment -> {
                SpacerShortMedium()
                DirectAssignmentChip(
                    onClick = { onShowInfo(PrescriptionDetailBottomSheetContent.DirectAssignment) }
                )
            }

            prescription.isSubstitutionAllowed -> {
                SpacerShortMedium()
                SubstitutionAllowedChip(
                    onClick = { onShowInfo(PrescriptionDetailBottomSheetContent.SubstitutionAllowed) }
                )
            }
        }

        SpacerShortMedium()

        val onClick = when {
            !prescription.isDirectAssignment &&
                (
                    prescription.state is SyncedTaskData.SyncedTask.Ready ||
                        prescription.state is SyncedTaskData.SyncedTask.LaterRedeemable
                    ) -> {
                { onShowInfo(PrescriptionDetailBottomSheetContent.HowLongValid(prescription)) }
            }

            else -> null
        }
        SyncedStatus(
            prescription = prescription,
            onClick = onClick
        )
        SpacerXLarge()
    }
}

@Composable
fun SyncedStatus(
    modifier: Modifier = Modifier,
    prescription: PrescriptionData.Synced,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = PaddingDefaults.Tiny)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .clip(CircleShape)
            .then(clickableModifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val text = if (prescription.isDirectAssignment) {
            stringResource(R.string.pres_details_direct_assignment_state)
        } else {
            expiryOrAcceptString(state = prescription.state)
        }
        Text(text, style = AppTheme.typography.body2l, textAlign = TextAlign.Center)
        if (onClick != null) {
            Spacer(Modifier.padding(2.dp))
            Icon(
                Icons.Rounded.KeyboardArrowRight,
                null,
                modifier = Modifier.size(16.dp),
                tint = AppTheme.colors.primary600
            )
        }
    }
}

@Composable
private fun ScannedPrescriptionOverview(
    navController: NavController,
    listState: LazyListState,
    prescription: PrescriptionData.Scanned,
    onSwitchRedeemed: (redeemed: Boolean) -> Unit,
    onShowInfo: (PrescriptionDetailBottomSheetContent) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
    ) {
        // prescription name
        // prescription kind
        // prescription state
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(PaddingDefaults.Medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.pres_details_scanned_prescription),
                    style = AppTheme.typography.h5,
                    textAlign = TextAlign.Center
                )
                SpacerShortMedium()
                DirectAssignmentChip(onClick = { onShowInfo(PrescriptionDetailBottomSheetContent.DirectAssignment) })
                SpacerShortMedium()
                val date = dateWithIntroductionString(R.string.prs_low_detail_scanned_on, prescription.scannedOn)
                Text(date, style = AppTheme.typography.body2l)
            }
        }

        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PaddingDefaults.Medium)
            ) {
                SpacerXLarge()
                RedeemedButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    redeemed = prescription.isRedeemed,
                    onSwitchRedeemed = onSwitchRedeemed
                )
                SpacerXXLarge()
            }
        }

        item {
            Label(
                text = stringResource(R.string.pres_detail_technical_information),
                onClick = {
                    navController.navigate(PrescriptionDetailsNavigationScreens.TechnicalInformation.path())
                }
            )
        }

        item {
            HealthPortalLink(Modifier.padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.XXLarge))
        }
    }
}

@Composable
private fun RedeemedButton(
    modifier: Modifier,
    redeemed: Boolean,
    onSwitchRedeemed: (redeemed: Boolean) -> Unit
) {
    val buttonText = if (redeemed) {
        stringResource(R.string.scanned_prescription_details_mark_as_unredeemed)
    } else {
        stringResource(R.string.scanned_prescription_details_mark_as_redeemed)
    }

    PrimaryButtonSmall(
        onClick = {
            onSwitchRedeemed(!redeemed)
        },
        modifier = modifier
    ) {
        Text(buttonText)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Label(
    text: String,
    label: String? = null,
    onClick: (() -> Unit)? = null
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val verticalPadding = if (label != null) {
        PaddingDefaults.ShortMedium
    } else {
        PaddingDefaults.Medium
    }

    Row(
        modifier = Modifier
            .combinedClickable(
                onClick = {
                    onClick?.invoke()
                },
                onLongClick = {
                    clipboardManager.setText(AnnotatedString(text))
                    Toast
                        .makeText(context, "$label $text", Toast.LENGTH_SHORT)
                        .show()
                },
                role = Role.Button
            )
            .padding(horizontal = PaddingDefaults.Medium, vertical = verticalPadding)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = text,
                style = AppTheme.typography.body1
            )
            if (label != null) {
                Text(
                    text = label,
                    style = AppTheme.typography.body2l
                )
            }
        }
        if (onClick != null) {
            SpacerMedium()
            Icon(Icons.Rounded.KeyboardArrowRight, null, tint = AppTheme.colors.neutral400)
        }
    }
}

@Composable
fun HealthPortalLink(
    modifier: Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.pres_detail_health_portal_description),
            style = AppTheme.typography.body2l
        )

        val linkInfo = stringResource(R.string.pres_detail_health_portal_description_url_info)
        val link = stringResource(R.string.pres_detail_health_portal_description_url)
        val uriHandler = LocalUriHandler.current
        val annotatedLink = annotatedLinkStringLight(link, linkInfo)

        SpacerSmall()
        ClickableText(
            text = annotatedLink,
            onClick = {
                annotatedLink
                    .getStringAnnotations("URL", it, it)
                    .firstOrNull()?.let { stringAnnotation ->
                        uriHandler.openUri(stringAnnotation.item)
                    }
            },
            modifier = Modifier.align(Alignment.End)
        )
    }
}
