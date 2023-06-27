/*
 * Copyright (c) 2023 gematik GmbH
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

import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
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
import androidx.compose.material.icons.rounded.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.analytics.TrackNavigationChanges
import de.gematik.ti.erp.app.analytics.trackPrescriptionDetailPopUps
import de.gematik.ti.erp.app.analytics.trackScreenUsingNavEntry
import de.gematik.ti.erp.app.core.LocalAnalytics
import de.gematik.ti.erp.app.core.LocalAuthenticator
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionDetailsNavigationScreens
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.ui.DirectAssignmentChip
import de.gematik.ti.erp.app.prescription.ui.FailureDetailsStatusChip
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceErrorState
import de.gematik.ti.erp.app.prescription.ui.ScannedChip
import de.gematik.ti.erp.app.prescription.ui.SubstitutionAllowedChip
import de.gematik.ti.erp.app.prescription.ui.prescriptionStateInfo
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.HealthPortalLink
import de.gematik.ti.erp.app.utils.compose.Label
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonTiny
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerShortMedium
import de.gematik.ti.erp.app.utils.compose.SpacerXLarge
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.dateWithIntroductionString
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.provideEmailIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

const val MissingValue = "---"

@Composable
fun PrescriptionDetailsScreen(
    taskId: String,
    mainNavController: NavController
) {
    val prescriptionDetailsController = rememberPrescriptionDetailsController()

    val prescription by produceState<PrescriptionData.Prescription?>(null) {
        prescriptionDetailsController.prescriptionDetailsFlow(taskId).collect {
            value = it
        }
    }

    var selectedMedication: PrescriptionData.Medication? by remember { mutableStateOf(null) }
    var selectedIngredient: SyncedTaskData.Ingredient? by remember { mutableStateOf(null) }

    val mainScope = rememberCoroutineScope { Dispatchers.Main }
    val onBack: () -> Unit = {
        mainScope.launch {
            mainNavController.popBackStack() // TODO onBack instead of NavController
        }
    }
    val navController = rememberNavController()
    var previousNavEntry by remember { mutableStateOf("prescriptionDetail") }
    TrackNavigationChanges(navController, previousNavEntry, onNavEntryChange = { previousNavEntry = it })
    prescription?.let { pres ->
        NavHost(
            navController = navController,
            startDestination = PrescriptionDetailsNavigationScreens.Overview.route
        ) {
            composable(PrescriptionDetailsNavigationScreens.Overview.route) {
                PrescriptionDetailsWithScaffold(
                    prescription = pres,
                    prescriptionDetailsController = prescriptionDetailsController,
                    navController = navController,
                    onClickMedication = {
                        selectedMedication = it
                        navController.navigate(PrescriptionDetailsNavigationScreens.Medication.path())
                    },
                    onBack = onBack
                )
            }
            composable(PrescriptionDetailsNavigationScreens.MedicationOverview.route) {
                MedicationOverviewScreen(
                    prescription = pres as PrescriptionData.Synced,
                    onClickMedication = {
                        selectedMedication = it
                        navController.navigate(PrescriptionDetailsNavigationScreens.Medication.path())
                    },
                    onBack = onBack
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

@Serializable
data class AppLinkPrescription(
    val patient: String?,
    val prescriber: String?,
    val description: String?,
    val prescribedOn: String?,
    val taskUrl: String,
    val emoji: String?
)

private val AllowedPrescriptionEmojis = listOf(
    "\uD83D\uDE23",    // 😣
    "\uD83D\uDE35",    // 😵
    "\uD83D\uDE35\u200D\uD83D\uDCAB",    // 😵‍💫
    "\uD83E\uDD22",    // 🤢
    "\uD83E\uDD2E",    // 🤮
    "\uD83E\uDD27",    // 🤧
    "\uD83D\uDE37",    // 😷
    "\uD83E\uDD12",    // 🙂
    "\uD83E\uDD15",    // 🙅
    "\uD83E\uDE7A",    // 🥺
    "\uD83D\uDC89",    // 💉
    "\uD83D\uDC8A",    // 💊
    "\uD83E\uDDA0",    // 🦠
    "\uD83C\uDF21",    // 🌡️
    "\uD83E\uDDEA",    // 🧪
    "\uD83E\uDDEB"     // 🧫
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PrescriptionDetailsWithScaffold(
    prescription: PrescriptionData.Prescription,
    prescriptionDetailsController: PrescriptionDetailsController,
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

    val analytics = LocalAnalytics.current
    val analyticsState by analytics.screenState
    LaunchedEffect(sheetState.isVisible) {
        if (sheetState.isVisible) {
            infoBottomSheetContent?.let { analytics.trackPrescriptionDetailPopUps(it) }
        } else {
            analytics.onPopUpClosed()
            val route = Uri.parse(navController.currentBackStackEntry!!.destination.route)
                .buildUpon().clearQuery().build().toString()
            trackScreenUsingNavEntry(route, analytics, analyticsState.screenNamesList)
        }
    }
    LaunchedEffect(infoBottomSheetContent) {
        if (infoBottomSheetContent != null) {
            sheetState.show()
        } else {
            sheetState.hide()
        }
    }
    ModalBottomSheetLayout(
        modifier = Modifier.testTag(TestTag.Prescriptions.Details.Screen),
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

                val uriHandler = LocalUriHandler.current

                if (prescription.accessCode != null) {
                    val taskUrl = "Task/${prescription.taskId}/\$accept?ac=${prescription.accessCode}"
                    val prescriptionBase64 = when (prescription) {
                        is PrescriptionData.Scanned ->
                            AppLinkPrescription(
                                patient = null,
                                prescriber = null,
                                description = null,
                                prescribedOn = null,
                                taskUrl = taskUrl,
                                emoji = AllowedPrescriptionEmojis.random()
                            )
                        is PrescriptionData.Synced ->
                            AppLinkPrescription(
                                patient = prescription.patient.name,
                                prescriber = prescription.practitioner.name,
                                description = prescription.name,
                                prescribedOn = prescription.authoredOn,
                                taskUrl = taskUrl,
                                emoji = AllowedPrescriptionEmojis.random()
                            )
                    }.let { Base64.encodeToString(Json.encodeToString(it).toByteArray(), Base64.URL_SAFE) }

                    IconButton(onClick = {
                        uriHandler.openUri("https://rezepte.lol/x/#eyJ2IjogMX0.${prescriptionBase64}")
                    }) {
                        Icon(Icons.Rounded.Share, null, tint = AppTheme.colors.primary700)
                    }
                }

                val context = LocalContext.current
                val authenticator = LocalAuthenticator.current
                val deletePrescriptionsHandle = remember {
                    DeletePrescriptions(
                        prescriptionDetailsController = prescriptionDetailsController,
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
                            coroutineScope.launch {
                                prescriptionDetailsController.redeemScannedTask(
                                    taskId = prescription.taskId,
                                    redeem = it
                                )
                            }
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

    val isDeletable by remember {
        derivedStateOf {
            (prescription as? PrescriptionData.Synced)?.isDeletable ?: true
        }
    }

    IconButton(
        onClick = { dropdownExpanded = true },
        modifier = Modifier.testTag(TestTag.Prescriptions.Details.MoreButton)
    ) {
        Icon(Icons.Rounded.MoreVert, null, tint = AppTheme.colors.neutral600)
    }
    DropdownMenu(
        expanded = dropdownExpanded,
        onDismissRequest = { dropdownExpanded = false },
        offset = DpOffset(24.dp, 0.dp)
    ) {
        DropdownMenuItem(
            modifier = Modifier.testTag(TestTag.Prescriptions.Details.DeleteButton),
            enabled = isDeletable,
            onClick = {
                dropdownExpanded = false
                showDeletePrescriptionDialog = true
            }
        ) {
            Text(
                text = stringResource(R.string.pres_detail_dropdown_delete),
                color = if (isDeletable) {
                    AppTheme.colors.red600
                } else {
                    AppTheme.colors.neutral400
                }
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

    Column {
        val colPadding = if (prescription.isIncomplete) {
            PaddingValues()
        } else {
            WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag(TestTag.Prescriptions.Details.Content),
            contentPadding = colPadding
        ) {
            item {
                SyncedHeader(
                    prescription = prescription,
                    onShowInfo = onShowInfo
                )
            }

            item {
                val text = additionalFeeText(prescription.medicationRequest.additionalFee) ?: noValueText

                Label(
                    text = text,
                    label = stringResource(R.string.pres_details_additional_fee),
                    onClick = onClickAdditionalFee(
                        prescription.medicationRequest.additionalFee,
                        onShowInfo
                    )
                )
            }

            prescription.medicationRequest.emergencyFee?.let { emergencyFee ->
                item {
                    val text = emergencyFeeText(emergencyFee)
                    Label(
                        text = text,
                        label = stringResource(R.string.pres_details_emergency_fee),
                        onClick = onClickEmergencyFee(emergencyFee, onShowInfo)
                    )
                }
            }

            item {
                Label(
                    modifier = Modifier.testTag(TestTag.Prescriptions.Details.MedicationButton),
                    text = prescription.name ?: noValueText,
                    label = stringResource(R.string.pres_details_medication),
                    onClick = onClickMedication(prescription, onSelectMedication, navController)
                )
            }

            item {
                Label(
                    modifier = Modifier.testTag(TestTag.Prescriptions.Details.PatientButton),
                    text = prescription.patient.name ?: noValueText,
                    label = stringResource(R.string.pres_detail_patient_header),
                    onClick = {
                        navController.navigate(PrescriptionDetailsNavigationScreens.Patient.path())
                    }
                )
            }

            item {
                Label(
                    modifier = Modifier.testTag(TestTag.Prescriptions.Details.PrescriberButton),
                    text = prescription.practitioner.name ?: noValueText,
                    label = stringResource(R.string.pres_detail_practitioner_header),
                    onClick = {
                        navController.navigate(PrescriptionDetailsNavigationScreens.Prescriber.path())
                    }
                )
            }

            item {
                Label(
                    modifier = Modifier.testTag(TestTag.Prescriptions.Details.OrganizationButton),
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
                    modifier = Modifier.testTag(TestTag.Prescriptions.Details.TechnicalInformationButton),
                    text = stringResource(R.string.pres_detail_technical_information),
                    onClick = {
                        navController.navigate(PrescriptionDetailsNavigationScreens.TechnicalInformation.path())
                    }
                )
            }

            item {
                HealthPortalLink(
                    Modifier.padding(
                        horizontal = PaddingDefaults.Medium,
                        vertical = PaddingDefaults.XXLarge
                    )
                )
            }
        }

        if (prescription.isIncomplete) {
            FailureBanner(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                prescription
            )
        }
    }
}

@Composable
private fun onClickMedication(
    prescription: PrescriptionData.Synced,
    onSelectMedication: (PrescriptionData.Medication) -> Unit,
    navController: NavController
): () -> Unit = {
    if (!prescription.isDispensed) {
        onSelectMedication(PrescriptionData.Medication.Request(prescription.medicationRequest))
    } else {
        navController.navigate(PrescriptionDetailsNavigationScreens.MedicationOverview.path())
    }
}

@Composable
private fun onClickEmergencyFee(
    emergencyFee: Boolean,
    onShowInfo: (PrescriptionDetailBottomSheetContent) -> Unit
): () -> Unit = {
    if (emergencyFee) {
        onShowInfo(
            PrescriptionDetailBottomSheetContent.EmergencyFeeNotExempt()
        )
    } else {
        onShowInfo(
            PrescriptionDetailBottomSheetContent.EmergencyFee()
        )
    }
}

@Composable
private fun emergencyFeeText(emergencyFee: Boolean) = if (emergencyFee) {
    // false - emergencyFee fee is to be paid by the insured (default value)
    // true - emergencyFee fee is not to be paid by the insured but by the payer
    stringResource(R.string.pres_detail_no)
} else {
    stringResource(R.string.pres_detail_yes)
}

@Composable
private fun onClickAdditionalFee(
    additionalFee: SyncedTaskData.AdditionalFee,
    onShowInfo: (PrescriptionDetailBottomSheetContent) -> Unit
): () -> Unit = {
    when (additionalFee) {
        SyncedTaskData.AdditionalFee.NotExempt -> {
            onShowInfo(
                PrescriptionDetailBottomSheetContent.AdditionalFeeNotExempt()
            )
        }
        SyncedTaskData.AdditionalFee.Exempt -> {
            onShowInfo(
                PrescriptionDetailBottomSheetContent.AdditionalFeeExempt()
            )
        }
        else -> {}
    }
}

@Composable
private fun additionalFeeText(additionalFee: SyncedTaskData.AdditionalFee): String? = when (additionalFee) {
    SyncedTaskData.AdditionalFee.Exempt ->
        stringResource(R.string.pres_detail_no)
    SyncedTaskData.AdditionalFee.NotExempt ->
        stringResource(R.string.pres_detail_yes)
    else -> null
}

@Composable
private fun FailureBanner(
    modifier: Modifier,
    prescription: PrescriptionData.Synced
) {
    val mailAddress = stringResource(R.string.settings_contact_mail_address)
    val subject = stringResource(R.string.settings_feedback_mail_subject)

    val context = LocalContext.current
    Row(
        modifier
            .fillMaxWidth()
            .background(AppTheme.colors.neutral050)
            .padding(PaddingDefaults.Medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.prescription_failure_info),
            style = AppTheme.typography.body2,
            modifier = Modifier.weight(1f)
        )
        SpacerMedium()
        PrimaryButtonTiny(
            onClick = {
                val body = """
                            PVS ID: ${prescription.task.pvsIdentifier}
                            
                            ${prescription.failureToReport}
                """.trimIndent()

                context.handleIntent(
                    provideEmailIntent(
                        address = mailAddress,
                        body = body,
                        subject = subject
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = AppTheme.colors.red600,
                contentColor = AppTheme.colors.neutral000
            )
        ) {
            Text(stringResource(R.string.report_prescription_failure))
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
            prescription.name ?: stringResource(R.string.prescription_medication_default_name),
            style = AppTheme.typography.h5,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        when {
            prescription.isIncomplete -> {
                SpacerShortMedium()
                FailureDetailsStatusChip(
                    onClick = {
                        onShowInfo(PrescriptionDetailBottomSheetContent.Failure())
                    }
                )
            }

            prescription.isDirectAssignment -> {
                SpacerShortMedium()
                DirectAssignmentChip(
                    onClick = {
                        onShowInfo(
                            PrescriptionDetailBottomSheetContent.DirectAssignment()
                        )
                    }
                )
            }

            prescription.isSubstitutionAllowed -> {
                SpacerShortMedium()
                SubstitutionAllowedChip(
                    onClick = {
                        onShowInfo(
                            PrescriptionDetailBottomSheetContent.SubstitutionAllowed()
                        )
                    }
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
                {
                    onShowInfo(
                        PrescriptionDetailBottomSheetContent.HowLongValid(
                            prescription
                        )
                    )
                }
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
            .then(clickableModifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (prescription.isDirectAssignment) {
            val text = if (prescription.isDispensed) {
                stringResource(R.string.pres_details_direct_assignment_received_state)
            } else {
                stringResource(R.string.pres_details_direct_assignment_state)
            }
            Text(
                text,
                style = AppTheme.typography.body2l,
                textAlign = TextAlign.Center
            )
        } else {
            prescriptionStateInfo(prescription.state, textAlign = TextAlign.Center)
        }
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
                ScannedChip(onClick = {
                    onShowInfo(PrescriptionDetailBottomSheetContent.Scanned())
                })
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
