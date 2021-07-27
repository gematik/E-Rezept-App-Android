/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.prescription.detail.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.api.Result
import de.gematik.ti.erp.app.core.AppModel
import de.gematik.ti.erp.app.core.ComposeBaseFragment
import de.gematik.ti.erp.app.core.LocalClipBoardManager
import de.gematik.ti.erp.app.db.entities.AuditEventSimple
import de.gematik.ti.erp.app.db.entities.LowDetailEventSimple
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionDetailsNavigationScreens
import de.gematik.ti.erp.app.prescription.detail.ui.model.UIPrescriptionDetail
import de.gematik.ti.erp.app.prescription.detail.ui.model.UIPrescriptionDetailScanned
import de.gematik.ti.erp.app.prescription.detail.ui.model.UIPrescriptionDetailSynced
import de.gematik.ti.erp.app.prescription.repository.InsuranceCompanyDetail
import de.gematik.ti.erp.app.prescription.repository.MedicationRequestDetail
import de.gematik.ti.erp.app.prescription.repository.OrganizationDetail
import de.gematik.ti.erp.app.prescription.repository.PatientDetail
import de.gematik.ti.erp.app.prescription.repository.PractitionerDetail
import de.gematik.ti.erp.app.prescription.repository.codeToDosageFormMapping
import de.gematik.ti.erp.app.redeem.ui.DataMatrixCode
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.Spacer8
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintCardDefaults
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.HintTextActionButton
import de.gematik.ti.erp.app.utils.compose.HintTextLearnMoreButton
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationMode
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import de.gematik.ti.erp.app.utils.phrasedDateString
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.internal.immutableListOf
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val framePadding = 16.dp
private const val MISSING_VALUE = "---"
private val dtFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
private const val maxAuditEventsToShow = 3

@AndroidEntryPoint
class PrescriptionDetailFragment : ComposeBaseFragment() {

    private val args: PrescriptionDetailFragmentArgs by navArgs()
    private val viewModel by viewModels<PrescriptionDetailsViewModel>()
    private lateinit var clipboardManager: ClipboardManager
    private val handleClipboard = { label: String, text: String ->
        val clipData = ClipData.newPlainText(label, text)
        clipboardManager.setPrimaryClip(clipData)
    }

    private val clipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
        val toastText = resources.getString(R.string.copied_clipboard)

        val desc = clipboardManager.primaryClip
            ?.description
            ?.label
            ?.let { "$it $toastText" }
            ?: toastText

        Toast.makeText(requireContext(), desc, Toast.LENGTH_SHORT).show()
    }

    override val content
        get() = @Composable {
            CompositionLocalProvider(
                LocalClipBoardManager provides handleClipboard
            ) {
                PrescriptionDetailsScreen(viewModel, args.taskId)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener(clipChangedListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        clipboardManager.removePrimaryClipChangedListener(clipChangedListener)
    }
}

@Composable
fun PrescriptionDetailsScreen(
    viewModel: PrescriptionDetailsViewModel,
    taskId: String
) {
    val frNavController = AppModel.frNavController
    val navController = rememberNavController()

    val navigationMode by navController.navigationModeState(PrescriptionDetailsNavigationScreens.PrescriptionDetails.route)
    NavHost(
        navController,
        startDestination = PrescriptionDetailsNavigationScreens.PrescriptionDetails.route
    ) {
        composable(PrescriptionDetailsNavigationScreens.PrescriptionDetails.route) {
            PrescriptionDetailsWithScaffold(
                viewModel,
                taskId,
                navigationMode,
                onShowAllAuditEvents = { navController.navigate(PrescriptionDetailsNavigationScreens.AuditProtocol.route) },
                onCancel = { frNavController.popBackStack() }
            )
        }
        composable(PrescriptionDetailsNavigationScreens.AuditProtocol.route) {
            NavigationAnimation(navigationMode) {
                AuditProtocolWithScaffold(
                    viewModel,
                    taskId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AuditProtocolWithScaffold(
    viewModel: PrescriptionDetailsViewModel,
    taskId: String,
    onBack: () -> Unit
) {
    val auditEvents by produceState<List<AuditEventSimple>>(immutableListOf()) {
        viewModel.auditEvents(taskId).collect {
            value = it
        }
    }

    Scaffold(
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Back,
                headline = stringResource(R.string.pres_detail_protocol_header),
                onClick = onBack
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(auditEvents) {
                Label(
                    text = if (it.text.isNullOrEmpty()) {
                        stringResource(id = R.string.pres_detail_protocol_empty_text)
                    } else {
                        it.text
                    },
                    label = phrasedDateString(it.timestamp)
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PrescriptionDetailsWithScaffold(
    viewModel: PrescriptionDetailsViewModel,
    taskId: String,
    navigationMode: NavigationMode,
    onShowAllAuditEvents: () -> Unit,
    onCancel: () -> Unit
) {
    val state by produceState<UIPrescriptionDetail?>(null) {
        value = viewModel.detailedPrescription(taskId)
    }

    val auditEvents by produceState<List<AuditEventSimple>>(immutableListOf()) {
        viewModel.auditEvents(taskId).collect {
            value = it
        }
    }

    val lowDetailRedeemEvents by produceState<List<LowDetailEventSimple>>(mutableListOf()) {
        viewModel.loadLowDetailEvents(taskId).collect {
            value = it
        }
    }

    val info = stringResource(R.string.pres_detail_redeem_info)
    val unRedeem = stringResource(R.string.pres_detail_un_redeem)

    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    val header = stringResource(id = R.string.prescription_details)
    Scaffold(
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Close,
                headline = header,
                onClick = onCancel
            )
        },
        snackbarHost = {
            RedeemSnackBarHost(
                hostState = snackBarHostState
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val unRedeemProtocolText = stringResource(R.string.un_redeem_protocol_text)

            state?.let {
                NavigationAnimation(navigationMode) {
                    PrescriptionDetails(
                        viewModel = viewModel,
                        state = it,
                        auditEvents = auditEvents,
                        lowDetailRedeemEvents = lowDetailRedeemEvents,
                        onShowAllAuditEvents = onShowAllAuditEvents,
                        onClickRedeem = {
                            coroutineScope.launch {
                                val result = snackBarHostState.showSnackbar(
                                    info,
                                    unRedeem,
                                    SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.onSwitchRedeemed(
                                        taskId,
                                        false,
                                        all = false,
                                        protocolText = unRedeemProtocolText
                                    )
                                }
                            }
                        },
                        onCancel = onCancel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PrescriptionDetails(
    modifier: Modifier = Modifier,
    viewModel: PrescriptionDetailsViewModel,
    state: UIPrescriptionDetail,
    auditEvents: List<AuditEventSimple>,
    lowDetailRedeemEvents: List<LowDetailEventSimple>,
    onShowAllAuditEvents: () -> Unit,
    onClickRedeem: () -> Unit,
    onCancel: () -> Unit
) {
    val frNavCtr = AppModel.frNavController

    var showMore by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val hintPadding = Modifier.padding(start = PaddingDefaults.Medium, end = PaddingDefaults.Medium)

    val isSubstituted = (state as? UIPrescriptionDetailSynced)?.let {
        it.medicationDispense != null && it.medication.uniqueIdentifier != it.medicationDispense.uniqueIdentifier
    } ?: false

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
    ) {
        if ((state as? UIPrescriptionDetailSynced)?.medicationRequest?.emergencyFee == true && state.redeemedOn == null) {
            item {
                EmergencyServiceCard()
            }
        }

        // low detail redeemed information
        if (state is UIPrescriptionDetailScanned && (state as? UIPrescriptionDetailScanned)?.redeemedOn != null) {
            item {
                Spacer16()
                HintCard(
                    modifier = hintPadding,
                    image = {
                        HintSmallImage(
                            painterResource(R.drawable.health_card_hint_blue),
                            innerPadding = it
                        )
                    },
                    title = { Text(stringResource(R.string.scanned_prescription_detail_redeemed_hint_header)) },
                    body = { Text(stringResource(R.string.scanned_prescription_detail_redeemed_hint_info)) },
                    action = {
                        HintTextActionButton(stringResource(R.string.scanned_prescription_detail_redeemed_hint_connect)) {
                            frNavCtr.navigate(PrescriptionDetailFragmentDirections.actionPrescriptionDetailFragmentToCardWallFragment())
                        }
                    }
                )
            }
        }

        // low detail information
        if (state is UIPrescriptionDetailScanned && (state as? UIPrescriptionDetailScanned)?.redeemedOn == null) {
            item {
                Spacer16()
                HintCard(
                    modifier = hintPadding,
                    image = {
                        HintSmallImage(
                            painterResource(R.drawable.information),
                            innerPadding = it
                        )
                    },
                    title = { Text(stringResource(R.string.scanned_prescription_detail_info_hint_header)) },
                    body = { Text(stringResource(R.string.scanned_prescription_detail_info_hint_info)) },
                    action = {
                        HintTextLearnMoreButton()
                    }
                )
            }
        }

        if (state.redeemedOn == null) {
            item {
                DataMatrixCode(state)
            }
        }

        item {
            val prescriptionName = when (state) {
                is UIPrescriptionDetailScanned -> stringResource(
                    id = R.string.scanned_prescription_placeholder_name,
                    state.number
                )
                is UIPrescriptionDetailSynced -> when (isSubstituted) {
                    true -> state.medicationDispense?.text ?: MISSING_VALUE
                    false -> state.medication.text ?: MISSING_VALUE
                    null -> MISSING_VALUE
                }
                else -> MISSING_VALUE
            }

            Header(
                text = prescriptionName
            )
        }

        val taskId = state.taskId
        when (state) {
            is UIPrescriptionDetailSynced -> item {
                FullDetailSecondHeader(state) {
                    frNavCtr.navigate(
                        PrescriptionDetailFragmentDirections.actionPrescriptionDetailFragmentToPharmacySearchFragment(
                            taskId
                        )
                    )
                }
            }
            is UIPrescriptionDetailScanned -> item {
                LowDetailRedeemHeader(state) { redeem, all, protocolText ->
                    viewModel.onSwitchRedeemed(state.taskId, redeem, all, protocolText)
                    if (redeem) {
                        onClickRedeem()
                    }
                }
            }
        }

        if ((state as? UIPrescriptionDetailSynced)?.medicationRequest?.substitutionAllowed == true && state.redeemedOn == null) {
            item {
                SubstitutionAllowed()
            }
        }

        item {
            Column {
                if (state is UIPrescriptionDetailScanned) {
                    ProtocolScanned(state, lowDetailRedeemEvents)
                }
                if (state is UIPrescriptionDetailSynced) {
                    MedicationInformation(state, isSubstituted)
                    if (isSubstituted) {
                        WasSubstitutedHint()
                    }
                    DosageInformation(state, isSubstituted)
                    PatientInformation(state.patient, state.insurance)
                }
            }
        }

        item {
            Column(modifier = Modifier.fillMaxSize()) {
                Button(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 40.dp, bottom = 40.dp)
                        .toggleable(
                            value = showMore,
                            onValueChange = { showMore = it },
                            role = Role.Checkbox,
                        ),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = AppTheme.colors.neutral050,
                        contentColor = AppTheme.colors.primary700
                    ),
                    onClick = { showMore = !showMore }
                ) {
                    Text(
                        stringResource(
                            when (showMore) {
                                true -> R.string.pres_detail_show_less
                                false -> R.string.pres_detail_show_more
                            }
                        ).uppercase(Locale.getDefault())
                    )
                    Icon(
                        imageVector = when (showMore) {
                            true -> Icons.Rounded.KeyboardArrowUp
                            false -> Icons.Rounded.KeyboardArrowDown
                        },
                        contentDescription = null
                    )
                }

                AnimatedVisibility(
                    visibleState = remember { MutableTransitionState(false) }.apply {
                        targetState = showMore
                    },
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column {
                        if (state is UIPrescriptionDetailSynced) {
                            PractitionerInformation(state.practitioner)
                            OrganizationInformation(state.organization)
                            AccidentInformation(state.medicationRequest)
                            AuditProtocol(
                                auditEvents,
                                state.lastSyncDate,
                                state.hasSyncError,
                                onShowAllAuditEvents = onShowAllAuditEvents
                            )
                        }
                        TechnicalPrescriptionInformation(
                            accessCode = state.accessCode,
                            taskId = state.taskId
                        )

                        val context = LocalContext.current
                        val accessInfoText = stringResource(R.string.logout_delete_no_access)

                        DeleteButton(state is UIPrescriptionDetailSynced) {
                            viewModel.deletePrescription(state.taskId, state is UIPrescriptionDetailSynced).apply {
                                if (this is Result.Error) {
                                    Toast.makeText(context, accessInfoText, Toast.LENGTH_SHORT).show()
                                } else {
                                    onCancel()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val scrollOffset = with(LocalDensity.current) {
        200.dp.toPx()
    }

    LaunchedEffect(showMore) {
        if (showMore) {
            delay(100)
            listState.animateScrollBy(scrollOffset)
        }
    }
}

@Composable
private fun DataMatrixCode(prescriptionDetail: UIPrescriptionDetail) {
    Surface(
        shape = RoundedCornerShape(framePadding / 2),
        border = BorderStroke(1.dp, AppTheme.colors.neutral300),
        modifier = Modifier.padding(16.dp)
    ) {
        DataMatrixCode(
            prescriptionDetail.bitmapMatrix,
            modifier = Modifier
                .aspectRatio(1.0f)
        )
    }
}

@Composable
private fun DeleteButton(isSyncedPrescription: Boolean, onClickDelete: () -> Unit) {
    var showDeletePrescriptionDialog by remember { mutableStateOf(false) }

    val deleteText = when (isSyncedPrescription) {
        true -> stringResource(R.string.pres_detail_delete)
        false -> stringResource(R.string.scanned_prescription_delete)
    }

    Button(
        onClick = { showDeletePrescriptionDialog = true },
        modifier = Modifier
            .padding(
                start = framePadding,
                end = framePadding,
                top = framePadding * 2,
                bottom = framePadding
            )
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = AppTheme.colors.red600,
            contentColor = AppTheme.colors.neutral000
        )
    ) {
        Text(
            deleteText.uppercase(Locale.getDefault()),
            modifier = Modifier.padding(
                start = framePadding,
                end = framePadding,
                top = framePadding / 2,
                bottom = framePadding / 2
            )
        )
    }

    if (showDeletePrescriptionDialog) {
        val info = stringResource(R.string.pres_detail_delete_msg)
        val cancelText = stringResource(R.string.pres_detail_delete_no)
        val actionText = stringResource(R.string.pres_detail_delete_yes)

        CommonAlertDialog(
            header = "",
            info = info,
            cancelText = cancelText,
            actionText = actionText,
            onCancel = {
                showDeletePrescriptionDialog = false
            },
            onClickAction = {
                onClickDelete()
                showDeletePrescriptionDialog = false
            }
        )
    }
}

@Composable
private fun FullDetailSecondHeader(
    prescriptionDetail: UIPrescriptionDetailSynced,
    onClickRedeem: () -> Unit
) {

    val text = if (prescriptionDetail.medicationDispense != null) {
        stringResource(
            id = R.string.pres_detail_medication_redeemed_on,
            prescriptionDetail.medicationDispense.whenHandedOver.format(dtFormatter)
        )
    } else {
        stringResource(
            id = R.string.pres_detail_medication_valid_until,
            prescriptionDetail.redeemUntil?.format(dtFormatter) ?: MISSING_VALUE
        )
    }

    Spacer4()
    Text(
        text = text,
        style = AppTheme.typography.body2l,
        modifier = Modifier.padding(start = framePadding, end = framePadding)
    )
    if (prescriptionDetail.redeemedOn == null) {
        Button(
            onClick = { onClickRedeem() },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 46.dp)
                .padding(
                    start = PaddingDefaults.Medium,
                    end = PaddingDefaults.Medium,
                    top = 24.dp,
                    bottom = PaddingDefaults.Medium
                )
        ) {
            Text(
                stringResource(R.string.pres_detail_medication_redeem_button_text).uppercase(Locale.getDefault()),
                modifier = Modifier.padding(
                    horizontal = PaddingDefaults.Medium,
                    vertical = PaddingDefaults.Small
                )
            )
        }
    }
}

@Composable
private fun LowDetailRedeemHeader(
    prescriptionDetail: UIPrescriptionDetailScanned,
    onSwitchRedeemed: (redeem: Boolean, all: Boolean, protocolText: String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        RedeemedButton(
            prescriptionDetail.redeemedOn != null,
            prescriptionDetail.unRedeemMorePossible,
            onSwitchRedeemed
        )
    }

    // mark as redeemed information hint
    if (prescriptionDetail.redeemedOn == null) {
        Spacer16()
        HintCard(
            image = {
                HintSmallImage(
                    painterResource(R.drawable.pharmacist_hint),
                    innerPadding = it
                )
            },
            title = { Text(stringResource(R.string.scanned_prescription_detail_hint_header)) },
            body = { Text(stringResource(R.string.scanned_prescription_detail_hint_info)) },
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
        )
    }
}

@Composable
private fun RedeemedButton(
    redeemed: Boolean,
    unRedeemMorePossible: Boolean,
    onSwitchRedeemed: (redeem: Boolean, all: Boolean, protocolText: String) -> Unit
) {
    var showUnRedeemDialog by remember { mutableStateOf(false) }
    val redeemProtocolText = stringResource(R.string.redeem_protocol_text)
    val unRedeemProtocolText = stringResource(R.string.un_redeem_protocol_text)

    if (showUnRedeemDialog) {
        UnRedeemPrescriptionDialog(
            onSwitchRedeemed = { redeem, all, protocolText ->
                onSwitchRedeemed(redeem, all, protocolText)
                showUnRedeemDialog = false
            },
        )
    }

    val buttonColors = if (redeemed) {
        ButtonDefaults.buttonColors(
            backgroundColor = AppTheme.colors.neutral050,
            contentColor = AppTheme.colors.primary700
        )
    } else {
        ButtonDefaults.buttonColors(
            backgroundColor = AppTheme.colors.primary600,
            contentColor = AppTheme.colors.neutral000
        )
    }

    val buttonText = if (redeemed) {
        stringResource(R.string.scanned_prescription_details_mark_as_unredeemed)
    } else {
        stringResource(R.string.scanned_prescription_details_mark_as_redeemed)
    }

    val protocolText = if (redeemed) {
        unRedeemProtocolText
    } else {
        redeemProtocolText
    }

    Button(
        onClick = {
            if (redeemed && unRedeemMorePossible) {
                showUnRedeemDialog = true
            } else {
                onSwitchRedeemed(!redeemed, false, protocolText)
            }
        },
        colors = buttonColors,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp)
    ) {
        Text(
            buttonText.uppercase(Locale.getDefault())
        )
    }
}

@Composable
private fun RedeemSnackBarHost(
    hostState: SnackbarHostState,
) {

    SnackbarHost(
        hostState = hostState,
        snackbar = {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp),
                backgroundColor = AppTheme.colors.neutral900,
                action = {
                    TextButton(
                        onClick = {
                            hostState.currentSnackbarData?.performAction()
                        }
                    ) {
                        Text(
                            text = hostState.currentSnackbarData?.actionLabel?.uppercase(Locale.getDefault())
                                ?: "",
                            color = AppTheme.colors.primary400
                        )
                    }
                },
                actionOnNewLine = false
            ) {
                Text(text = hostState.currentSnackbarData?.message ?: "")
            }
        }
    )
}

@Composable
private fun UnRedeemPrescriptionDialog(
    onSwitchRedeemed: (redeem: Boolean, all: Boolean, protocol: String) -> Unit,

) {

    val unRedeemProtocolText = stringResource(R.string.un_redeem_protocol_text)

    AlertDialog(
        onDismissRequest = { onSwitchRedeemed(false, false, unRedeemProtocolText) },
        text = {
            Text(
                stringResource(R.string.pres_detail_un_redeem_msg),
                style = AppTheme.typography.body2l
            )
        },
        buttons = {
            Row {
                Spacer(modifier = Modifier.weight(1.0f))
                TextButton(
                    onClick = {
                        onSwitchRedeemed(
                            false,
                            false,
                            unRedeemProtocolText
                        )
                    }
                ) {
                    Text(
                        stringResource(R.string.pres_detail_un_redeem_selected).uppercase(
                            Locale.getDefault()
                        )
                    )
                }
                TextButton(
                    onClick = {
                        onSwitchRedeemed(
                            false,
                            true,
                            unRedeemProtocolText
                        )
                    }
                ) {
                    Text(
                        stringResource(R.string.pres_detail_un_redeem_all).uppercase(
                            Locale.getDefault()
                        )
                    )
                }
            }
        },
        properties = DialogProperties(dismissOnBackPress = false, false)
    )
}

@Composable
private fun MedicationInformation(
    state: UIPrescriptionDetailSynced,
    isSubstituted: Boolean
) {

    val medicationType = if (isSubstituted) {
        state.medicationDispense?.type?.let { codeToDosageFormMapping[it] }?.let { stringResource(it) } ?: MISSING_VALUE
    } else {
        state.medication.type?.let { stringResource(it) } ?: MISSING_VALUE
    }

    val uniqueIdentifier = if (isSubstituted) {
        state.medicationDispense?.uniqueIdentifier ?: MISSING_VALUE
    } else {
        state.medication.uniqueIdentifier ?: MISSING_VALUE
    }

    SubHeader(
        text = stringResource(id = R.string.pres_detail_medication_header)
    )

    Label(
        text = medicationType,
        label = stringResource(id = R.string.pres_detail_medication_label_dosage_form)
    )

    Label(
        text = if (state.medication.normSize != null) {
            if (state.medication.normSize.text != null) {
                "${state.medication.normSize.code} - ${stringResource(state.medication.normSize.text)}"
            } else {
                state.medication.normSize.code
            }
        } else {
            MISSING_VALUE
        },
        label = stringResource(id = R.string.pres_detail_medication_label_normsize)
    )

    Label(
        text = uniqueIdentifier,
        label = stringResource(id = R.string.pres_detail_medication_label_id)
    )
}

@Composable
private fun WasSubstitutedHint() {

    HintCard(
        modifier = Modifier.padding(framePadding),
        properties = HintCardDefaults.properties(
            backgroundColor = AppTheme.colors.red100,
            contentColor = AppTheme.colors.neutral999,
            border = BorderStroke(0.0.dp, AppTheme.colors.neutral300),
            elevation = 0.dp
        ),
        image = {
            HintSmallImage(
                painterResource(R.drawable.medical_hand_out_circle_red),
                innerPadding = it
            )
        },
        title = { Text(stringResource(R.string.pres_detail_substituted_header)) },
        body = { Text(stringResource(R.string.pres_detail_substituted_info)) }
    )
}

@Composable
private fun ColumnScope.DosageInformation(
    state: UIPrescriptionDetailSynced,
    isSubstituted: Boolean
) {
    val infoText = if (isSubstituted) {
        state.medicationDispense?.dosageInstruction
            ?: stringResource(id = R.string.pres_detail_dosage_default_info)
    } else {
        state.medicationRequest.dosageInstruction
            ?: stringResource(id = R.string.pres_detail_dosage_default_info)
    }

    SubHeader(
        text = stringResource(id = R.string.pres_detail_dosage_header)
    )
    HintCard(
        modifier = Modifier.padding(start = PaddingDefaults.Medium, end = PaddingDefaults.Medium),
        image = { HintSmallImage(painterResource(R.drawable.doctor_circle), innerPadding = it) },
        title = null,
        body = { Text(infoText) },
    )
}

@Composable
private fun PatientInformation(
    patient: PatientDetail,
    insurance: InsuranceCompanyDetail
) {
    SubHeader(
        text = stringResource(id = R.string.pres_detail_patient_header)
    )

    Label(
        text = patient.name ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_patient_label_name)
    )

    Label(
        text = patient.address ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_patient_label_address)
    )

    Label(
        text = patient.birthdate?.format(dtFormatter) ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_patient_label_birthdate)
    )

    Label(
        text = insurance.name ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_patient_label_insurance)
    )

    Label(
        text = insurance.status?.let { stringResource(it) } ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_patient_label_member_status)
    )

    Label(
        text = patient.insuranceIdentifier ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_patient_label_insurance_id)
    )
}

@Composable
private fun PractitionerInformation(
    practitioner: PractitionerDetail
) {
    SubHeader(
        text = stringResource(id = R.string.pres_detail_practitioner_header)
    )

    Label(
        text = practitioner.name ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_practitioner_label_name)
    )

    Label(
        text = practitioner.qualification ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_practitioner_label_qualification)
    )

    Label(
        text = practitioner.practitionerIdentifier ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_practitioner_label_id)
    )
}

@Composable
private fun OrganizationInformation(
    organization: OrganizationDetail
) {
    SubHeader(
        text = stringResource(id = R.string.pres_detail_organization_header)
    )

    Label(
        text = organization.name ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_organization_label_name)
    )

    Label(
        text = organization.address ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_organization_label_address)
    )

    Label(
        text = organization.uniqueIdentifier ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_organization_label_id)
    )

    Label(
        text = organization.phone ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_organization_label_telephone)
    )

    Label(
        text = organization.mail ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_organization_label_email)
    )
}

@Composable
private fun AccidentInformation(
    medicationRequest: MedicationRequestDetail
) {
    SubHeader(
        text = stringResource(id = R.string.pres_detail_accident_header)
    )

    Label(
        text = medicationRequest.dateOfAccident?.format(dtFormatter)
            ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_accident_label_date)
    )

    Label(
        text = medicationRequest.location ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_accident_label_location)
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AuditProtocol(
    auditEvents: List<AuditEventSimple>,
    lastUpdate: LocalDateTime?,
    hasSyncError: Boolean,
    onShowAllAuditEvents: () -> Unit
) {
    SubHeaderWithNavigation(
        text = stringResource(R.string.pres_detail_protocol_header),
        buttonText = stringResource(R.string.pres_detail_protocol_show_all),
        onClickButton = onShowAllAuditEvents
    )

    Column {
        if (hasSyncError) {
            ErrorCard(
                errorText = stringResource(id = R.string.audit_protocol_sync_failed),
                modifier = Modifier.padding(PaddingDefaults.Medium)
            )
        }

        if (auditEvents.size <= maxAuditEventsToShow) {
            auditEvents
        } else {
            auditEvents.subList(
                0,
                maxAuditEventsToShow
            )
        }.forEach {
            Label(
                text = if (it.text.isNullOrEmpty()) {
                    stringResource(id = R.string.pres_detail_protocol_empty_text)
                } else {
                    it.text
                },
                label = phrasedDateString(it.timestamp)
            )
        }
        lastUpdate?.let {
            Text(
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                text = stringResource(
                    id = R.string.audit_protocol_last_update_info,
                    phrasedDateString(lastUpdate)
                ),
                style = AppTheme.typography.body2l
            )
            Spacer8()
        }
    }
}

@Composable
private fun ProtocolScanned(
    uiPrescriptionDetail: UIPrescriptionDetailScanned,
    lowDetailRedeemEvents: List<LowDetailEventSimple>
) {
    val firstScan = stringResource(
        R.string.scanned_prescription_detail_protocol_scanned_at,
        uiPrescriptionDetail.formattedScannedInfo(stringResource(R.string.at))
    )

    SubHeader(
        text = stringResource(id = R.string.scanned_prescription_detail_protocol_header)
    )

    Label(
        text = firstScan,
        label = stringResource(id = R.string.scanned_prescription_detail_protocol_scanned_label)
    )

    for (i in lowDetailRedeemEvents.indices) {
        Label(
            text = phrasedDateString(lowDetailRedeemEvents[i].timestamp.toLocalDateTime()),
            label = lowDetailRedeemEvents[i].text
        )
    }
}

@Composable
private fun TechnicalPrescriptionInformation(accessCode: String, taskId: String) {
    SubHeader(stringResource(R.string.pres_detail_technical_information))

    Label(
        text = accessCode,
        label = stringResource(id = R.string.access_code)
    )

    Label(
        text = taskId,
        label = stringResource(id = R.string.task_id)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Label(
    text: String,
    label: String
) {
    val clipboardHandler = LocalClipBoardManager.current

    Column(
        modifier = Modifier
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    clipboardHandler(label, text)
                }
            )
            .padding(framePadding)
            .fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.body1
        )
        Spacer4()
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
            color = AppTheme.typographyColors.body2l
        )
    }
}

@Composable
private fun Header(
    text: String
) = Text(
    text = text,
    style = MaterialTheme.typography.h6,
    fontWeight = FontWeight(500),
    modifier = Modifier.padding(
        start = framePadding,
        end = framePadding,
        top = framePadding * 1.5f
    )
)

@Composable
private fun SubHeader(
    text: String
) =
    Text(
        text = text,
        style = MaterialTheme.typography.subtitle1,
        fontWeight = FontWeight(500),
        modifier = Modifier.padding(
            top = 40.dp,
            end = framePadding,
            start = framePadding,
            bottom = framePadding
        )
    )

@Composable
private fun SubHeaderWithNavigation(
    text: String,
    buttonText: String,
    onClickButton: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(
                top = 40.dp,
                end = framePadding,
                start = framePadding,
                bottom = framePadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight(500),
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onClickButton) {
            Text(buttonText)
            Icon(Icons.Rounded.KeyboardArrowRight, null)
        }
    }
}

@Composable
private fun EmergencyServiceCard() {
    Card(
        modifier = Modifier
            .padding(start = framePadding, top = framePadding, end = framePadding)
            .fillMaxWidth()
    ) {
        Row {
            Image(
                painterResource(R.drawable.pharmacist),
                null,
                alignment = Alignment.BottomStart
            )
            Column {
                Text(
                    stringResource(R.string.pres_detail_noctu_header),
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    stringResource(R.string.pres_detail_noctu_info),
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}

@Composable
fun SubstitutionAllowed() {
    HintCard(
        modifier = Modifier.padding(framePadding),
        properties = HintCardDefaults.properties(
            backgroundColor = AppTheme.colors.primary100,
            border = BorderStroke(0.0.dp, AppTheme.colors.neutral300),
            elevation = 0.dp
        ),
        image = {
            HintSmallImage(
                painterResource(R.drawable.pharmacist_circle),
                innerPadding = it
            )
        },
        title = { Text(stringResource(R.string.pres_detail_aut_idem_header)) },
        body = { Text(stringResource(R.string.pres_detail_aut_idem_info)) },
        action = {
            HintTextLearnMoreButton()
        }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ErrorCard(
    errorText: String,
    modifier: Modifier = Modifier
) =
    HintCard(
        modifier = modifier,
        properties = HintCardDefaults.properties(
            backgroundColor = AppTheme.colors.red100,
            contentColor = AppTheme.colors.red900
        ),
        image = {
            Icon(Icons.Rounded.Warning, null, modifier = Modifier.size(40.dp))
        },
        title = null,
        body = { Text(errorText) }
    )
