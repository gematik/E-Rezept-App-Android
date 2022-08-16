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

package de.gematik.ti.erp.app.prescription.detail.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.core.LocalAuthenticator
import de.gematik.ti.erp.app.prescription.detail.ui.model.UIPrescriptionDetail
import de.gematik.ti.erp.app.prescription.detail.ui.model.UIPrescriptionDetailScanned
import de.gematik.ti.erp.app.prescription.detail.ui.model.UIPrescriptionDetailSynced
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.repository.codeToFormMapping
import de.gematik.ti.erp.app.prescription.repository.normSizeMapping
import de.gematik.ti.erp.app.prescription.repository.statusMapping
import de.gematik.ti.erp.app.prescription.ui.CompletedStatusChip
import de.gematik.ti.erp.app.prescription.ui.InProgressStatusChip
import de.gematik.ti.erp.app.prescription.ui.PendingStatusChip
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceErrorState
import de.gematik.ti.erp.app.prescription.ui.ReadyStatusChip
import de.gematik.ti.erp.app.prescription.ui.UnknownStatusChip
import de.gematik.ti.erp.app.prescription.ui.expiryOrAcceptString
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.redeem.ui.DataMatrixCode
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintCardDefaults
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.HintTextLearnMoreButton
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.Spacer8
import de.gematik.ti.erp.app.utils.compose.annotatedLinkStringLight
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.dateTimeShortText
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private const val MISSING_VALUE = "---"

@Composable
fun PrescriptionDetailsScreen(
    taskId: String,
    mainNavController: NavController
) {
    val viewModel: PrescriptionDetailsViewModel by rememberViewModel()

    PrescriptionDetailsWithScaffold(
        viewModel = viewModel,
        taskId = taskId,
        onCancel = { mainNavController.popBackStack() }
    )
}

@Composable
private fun PrescriptionDetailsWithScaffold(
    viewModel: PrescriptionDetailsViewModel,
    taskId: String,
    onCancel: () -> Unit
) {
    val state by produceState<UIPrescriptionDetail?>(null) {
        viewModel.screenState(taskId).collect {
            value = it
        }
    }

    val scaffoldState = rememberScaffoldState()
    val listState = rememberLazyListState()

    AnimatedElevationScaffold(
        scaffoldState = scaffoldState,
        listState = listState,
        onBack = onCancel,
        topBarTitle = stringResource(R.string.prescription_details),
        navigationMode = NavigationBarMode.Close,
        snackbarHost = { SnackbarHost(it, modifier = Modifier.navigationBarsPadding()) }
    ) { innerPadding ->
        state?.let {
            PrescriptionDetails(
                viewModel = viewModel,
                listState = listState,
                scaffoldState = scaffoldState,
                profileId = it.profileId,
                state = it,
                onCancel = onCancel
            )
        }
    }
}

@Composable
private fun PrescriptionDetails(
    modifier: Modifier = Modifier,
    profileId: ProfileIdentifier,
    listState: LazyListState,
    scaffoldState: ScaffoldState,
    viewModel: PrescriptionDetailsViewModel,
    state: UIPrescriptionDetail,
    onCancel: () -> Unit
) {
    var showMore by remember { mutableStateOf(false) }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize(),
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
    ) {
        if ((state as? UIPrescriptionDetailSynced)?.medicationRequest?.emergencyFee == true && state.redeemedOn == null) {
            item {
                EmergencyServiceCard()
            }
        }

        if (state.matrixPayload != null && state.redeemedOn == null) {
            item {
                DataMatrixCode(state.matrixPayload!!)
            }
        }

        item {
            when (state) {
                is UIPrescriptionDetailScanned -> MedicationDetailScanned(state) { redeem ->
                    viewModel.redeemScannedTask(state.taskId, redeem)
                }
                is UIPrescriptionDetailSynced -> MedicationDetailSynced(state)
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
                            role = Role.Checkbox
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
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        if (state is UIPrescriptionDetailSynced) {
                            PractitionerInformation(state.practitioner)
                            OrganizationInformation(state.organization)
                            AccidentInformation(state.medicationRequest)
                        }
                        TechnicalPrescriptionInformation(
                            accessCode = state.accessCode,
                            taskId = state.taskId
                        )

                        val context = LocalContext.current
                        val authenticator = LocalAuthenticator.current
                        val deletePrescriptionsHandle = remember {
                            DeletePrescriptions(
                                bridge = viewModel,
                                authenticator = authenticator
                            )
                        }

                        val coroutineScope = rememberCoroutineScope()
                        DeleteButton(state is UIPrescriptionDetailSynced) {
                            val deleteState = deletePrescriptionsHandle.deletePrescription(
                                profileId = profileId,
                                taskId = state.taskId
                            )

                            when (deleteState) {
                                is PrescriptionServiceErrorState -> {
                                    coroutineScope.launch {
                                        deleteErrorMessage(context, deleteState)?.let {
                                            scaffoldState.snackbarHostState.showSnackbar(it)
                                        }
                                    }
                                }
                                is DeletePrescriptions.State.Deleted -> onCancel()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicationDetailSynced(prescription: UIPrescriptionDetailSynced) {
    if (prescription.medicationRequest.medication is SyncedTaskData.MedicationPZN ||
        prescription.medicationRequest.medication is SyncedTaskData.MedicationFreeText
    ) {
        if (prescription.medicationRequest.substitutionAllowed && prescription.redeemedOn == null) {
            SubstitutionAllowed()
        }
    }

    PrescriptionStatusChip(prescription.state)

    if (prescription.medicationDispenses.isNotEmpty()) {
        Header(
            annotatedPluralsResource(
                R.plurals.medication_detail_dispensed_medications_header,
                prescription.medicationDispenses.size
            ).text
        )

        if (prescription.medicationDispenses.first().wasSubstituted) {
            WasSubstitutedHint()
        }

        prescription.medicationDispenses.forEach {
            it.medication?.let { medication ->
                MedicationInformation(medication = medication)
            }
        }

        DosageInformation(prescription, prescription.medicationDispenses.first().wasSubstituted)
    }

    Header(stringResource(R.string.prescription_detail_requested_medication))
    FullDetailSecondHeader(prescription)
    prescription.medicationRequest.medication?.let { medication ->
        MedicationInformation(medication)
    }

    DosageInformation(prescription, false)

    Column {
        HealthPortalLink()
    }

    PatientInformation(prescription.patient, prescription.insurance)
}

@Composable
private fun PrescriptionStatusChip(
    state: SyncedTaskData.SyncedTask.TaskState
) {
    Column(
        modifier = Modifier.padding(
            start = PaddingDefaults.Medium,
            end = PaddingDefaults.Medium,
            top = PaddingDefaults.XXLarge
        )
    ) {
        when (state) {
            is SyncedTaskData.SyncedTask.Other -> {
                when (state.state) {
                    SyncedTaskData.TaskStatus.InProgress -> InProgressStatusChip()
                    SyncedTaskData.TaskStatus.Completed -> CompletedStatusChip()
                    else -> UnknownStatusChip()
                }
            }
            is SyncedTaskData.SyncedTask.Pending -> PendingStatusChip()
            is SyncedTaskData.SyncedTask.Ready -> ReadyStatusChip()
            else -> {}
        }
    }
}

@Composable
fun MedicationInformation(medication: SyncedTaskData.Medication) {
    when (medication) {
        is SyncedTaskData.MedicationPZN -> PZNMedicationInformation(medication)
        is SyncedTaskData.MedicationIngredient -> IngredientMedicationInformation(medication)
        is SyncedTaskData.MedicationCompounding -> CompoundingMedicationInformation(medication)
        is SyncedTaskData.MedicationFreeText -> FreeTextMedicationInformation(medication)
    }
    Divider()
}

@Composable
fun MedicationDetailScanned(state: UIPrescriptionDetailScanned, onSwitchRedeem: (Boolean) -> Unit) {
    Header(
        text = stringResource(
            id = R.string.scanned_prescription_placeholder_name,
            state.number
        )
    )
    LowDetailRedeemHeader(state) {
        onSwitchRedeem(it)
    }
}

@Composable
private fun DataMatrixCode(payload: String) {
    Surface(
        shape = RoundedCornerShape(PaddingDefaults.Medium / 2),
        border = BorderStroke(1.dp, AppTheme.colors.neutral300),
        modifier = Modifier.padding(16.dp)
    ) {
        DataMatrixCode(
            payload = payload,
            modifier = Modifier
                .aspectRatio(1.0f)
        )
    }
}

@Composable
private fun DeleteButton(isSyncedPrescription: Boolean, onClickDelete: suspend () -> Unit) {
    var showDeletePrescriptionDialog by remember { mutableStateOf(false) }
    var deletionInPogress by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val mutex = MutatorMutex()

    val deleteText = when (isSyncedPrescription) {
        true -> stringResource(R.string.pres_detail_delete)
        false -> stringResource(R.string.scanned_prescription_delete)
    }

    Button(
        onClick = { showDeletePrescriptionDialog = true },
        modifier = Modifier
            .padding(
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                top = PaddingDefaults.Medium * 2,
                bottom = PaddingDefaults.Medium
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
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                top = PaddingDefaults.Medium / 2,
                bottom = PaddingDefaults.Medium / 2
            )
        )
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
            enabled = !deletionInPogress,
            onCancel = {
                showDeletePrescriptionDialog = false
            },
            onClickAction = {
                coroutineScope.launch {
                    mutex.mutate {
                        try {
                            deletionInPogress = true
                            onClickDelete()
                        } finally {
                            showDeletePrescriptionDialog = false
                            deletionInPogress = false
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun FullDetailSecondHeader(
    prescriptionDetail: UIPrescriptionDetailSynced
) {
    val text =
        if (prescriptionDetail.medicationDispenses.isNotEmpty()) {
            val timestamp = remember(LocalConfiguration.current, prescriptionDetail) {
                val dtFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                LocalDateTime.ofInstant(
                    prescriptionDetail.medicationDispenses.first().whenHandedOver,
                    ZoneId.systemDefault()
                ).format(dtFormatter)
            }
            stringResource(R.string.pres_detail_medication_redeemed_on, timestamp)
        } else if (prescriptionDetail.taskStatus == SyncedTaskData.TaskStatus.InProgress) {
            stringResource(R.string.pres_detail_medication_in_progress)
        } else {
            expiryOrAcceptString(prescriptionDetail.state)
        }
    Text(
        text = text,
        style = AppTheme.typography.body2l,
        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
    )
}

@Composable
private fun LowDetailRedeemHeader(
    prescriptionDetail: UIPrescriptionDetailScanned,
    onSwitchRedeemed: (redeem: Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RedeemedButton(
            prescriptionDetail.redeemedOn != null,
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
    onSwitchRedeemed: (redeem: Boolean) -> Unit
) {
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

    Button(
        onClick = {
            onSwitchRedeemed(!redeemed)
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
fun PZNMedicationInformation(medication: SyncedTaskData.MedicationPZN) {
    Column {
        SubHeader(
            text = medication.text
        )

        CategoryLabel(medication.category)
        VaccineLabel(medication.vaccine)

        FormLabel(medication.form)

        NormSizeLabel(medication.normSizeCode)

        medication.amount?.let { AmountLabel(it) }

        PZNLabel(medication.uniqueIdentifier)

        LotNumberLabel(medication.lotNumber)

        ExpirationDateLabel(medication.expirationDate)
    }
}

@Composable
fun IngredientMedicationInformation(medication: SyncedTaskData.MedicationIngredient) {
    Column {
        IngredientInformation(medication.ingredients[0])

        CategoryLabel(medication.category)

        VaccineLabel(medication.vaccine)

        FormLabel(medication.form)

        NormSizeLabel(medication.normSizeCode)

        medication.amount?.let { AmountLabel(it) }

        LotNumberLabel(medication.lotNumber)

        ExpirationDateLabel(medication.expirationDate)
    }
}

@Composable
fun CompoundingMedicationInformation(medication: SyncedTaskData.MedicationCompounding) {
    Column {
        SubHeader(
            text = medication.form ?: stringResource(R.string.pres_detail_medication_compounding)
        )

        CategoryLabel(medication.category)

        VaccineLabel(medication.vaccine)

        medication.amount?.let { AmountLabel(it) }

        SubHeader(stringResource(R.string.pres_detail_medication_ingredents_header))

        medication.ingredients.forEach {
            IngredientInformation(it)
        }

        LotNumberLabel(medication.lotNumber)

        ExpirationDateLabel(medication.expirationDate)
    }
}

@Composable
fun FreeTextMedicationInformation(medication: SyncedTaskData.MedicationFreeText) {
    Column {
        SubHeader(
            text = medication.text
        )

        CategoryLabel(medication.category)
        VaccineLabel(medication.vaccine)

        FormLabel(medication.form)

        LotNumberLabel(medication.lotNumber)

        ExpirationDateLabel(medication.expirationDate)
    }
}

@Composable
fun PZNLabel(uniqueIdentifier: String) {
    Label(
        text = uniqueIdentifier,
        label = stringResource(id = R.string.pres_detail_medication_label_id)
    )
}

@Composable
fun ExpirationDateLabel(expirationDate: Instant?) {
    expirationDate?.let {
        Label(
            text = dateTimeShortText(expirationDate),
            label = stringResource(id = R.string.pres_detail_medication_label_expiration_date)
        )
    }
}

@Composable
fun CategoryLabel(category: SyncedTaskData.MedicationCategory) {
    val text = when (category) {
        SyncedTaskData.MedicationCategory.ARZNEI_UND_VERBAND_MITTEL -> stringResource(R.string.medicines_bandages)
        SyncedTaskData.MedicationCategory.BTM -> stringResource(R.string.narcotics)
        SyncedTaskData.MedicationCategory.AMVV -> stringResource(R.string.amvv)
    }

    Label(
        text = text,
        label = stringResource(id = R.string.pres_detail_medication_label_category)
    )
}

@Composable
fun VaccineLabel(isVaccine: Boolean) {
    if (isVaccine) {
        Text(
            text = stringResource(id = R.string.pres_detail_medication_vaccine),
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun LotNumberLabel(lotNumber: String?) {
    lotNumber?.let { number ->
        Label(
            text = number,
            label = stringResource(id = R.string.pres_detail_medication_label_lot_number)
        )
    }
}

@Composable
fun AmountLabel(amount: SyncedTaskData.Ratio) {
    Label(
        text = amount.numerator?.value + " " + amount.numerator?.unit,
        label = stringResource(id = R.string.pres_detail_medication_label_amount)
    )
}

@Composable
fun IngredientAmountLabel(amount: String?) {
    amount?.let {
        Label(
            text = it,
            label = stringResource(id = R.string.pres_detail_medication_label_ingredient_amount)
        )
    }
}

@Composable
fun FormLabel(form: String?) {
    codeToFormMapping[form]?.let { resourceId ->
        stringResource(resourceId)
    } ?: form?.let {
        Label(
            text = it,
            label = stringResource(id = R.string.pres_detail_medication_label_dosage_form)
        )
    }
}

@Composable
fun IngredientInformation(ingredient: SyncedTaskData.Ingredient) {
    IngredientNameLabel(ingredient.text)
    IngredientAmountLabel(ingredient.amount)
    FormLabel(ingredient.form)
    ingredient.strength?.let { StrengtLabel(it) }
}

@Composable
fun IngredientNameLabel(text: String) {
    Label(
        text = text,
        label = stringResource(id = R.string.pres_detail_medication_label_ingredient_name)
    )
}

@Composable
fun StrengtLabel(strength: SyncedTaskData.Ratio) {
    if (strength.numerator != null) {
        Label(
            text = strength.numerator.value + " " + strength.numerator.unit,
            label = stringResource(id = R.string.pres_detail_medication_label_ingredient_strength)
        )
    }
}

@Composable
fun NormSizeLabel(normSizeCode: String?) {
    normSizeCode?.let { code ->
        normSizeMapping[code]?.let { resourceId ->
            Label(
                text = "$code - ${stringResource(resourceId)}",
                label = stringResource(id = R.string.pres_detail_medication_label_normsize)
            )
        }
    }
}

@Composable
private fun WasSubstitutedHint() {
    HintCard(
        modifier = Modifier.padding(PaddingDefaults.Medium),
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
private fun DosageInformation(
    state: UIPrescriptionDetailSynced,
    isSubstituted: Boolean
) {
    val infoText = if (isSubstituted) {
        state.medicationDispenses.firstOrNull()?.dosageInstruction
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
        body = { Text(infoText) }
    )
}

@Composable
fun ColumnScope.HealthPortalLink() {
    Spacer16()
    Text(
        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
        text = stringResource(id = R.string.pres_detail_health_portal_description),
        style = AppTheme.typography.body2,
        color = AppTheme.typographyColors.body2l
    )
    Spacer8()
    val linkInfo = stringResource(id = R.string.pres_detail_health_portal_description_url_info)
    val link = stringResource(id = R.string.pres_detail_health_portal_description_url)
    val uriHandler = LocalUriHandler.current
    val annotatedLink =
        annotatedLinkStringLight(link, linkInfo)

    ClickableText(
        text = annotatedLink,
        onClick = {
            annotatedLink
                .getStringAnnotations("URL", it, it)
                .firstOrNull()?.let { stringAnnotation ->
                    uriHandler.openUri(stringAnnotation.item)
                }
        },
        modifier = Modifier
            .align(Alignment.End)
            .padding(end = PaddingDefaults.Medium)
    )
}

@Composable
private fun PatientInformation(
    patient: SyncedTaskData.Patient,
    insurance: SyncedTaskData.InsuranceInformation
) {
    SubHeader(
        text = stringResource(id = R.string.pres_detail_patient_header)
    )

    Label(
        text = patient.name ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_patient_label_name)
    )

    Label(
        text = patient.address?.joinToString() ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_patient_label_address)
    )

    Label(
        text = remember(LocalConfiguration.current, patient) {
            val dtFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

            patient.birthdate?.let {
                LocalDateTime.ofInstant(it, ZoneId.systemDefault())
                    .toLocalDate()
                    .format(dtFormatter)
            } ?: MISSING_VALUE
        },
        label = stringResource(id = R.string.pres_detail_patient_label_birthdate)
    )

    Label(
        text = insurance.name ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_patient_label_insurance)
    )

    Label(
        text = insurance.status?.let { statusMapping[it]?.let { stringResource(it) } } ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_patient_label_member_status)
    )

    Label(
        text = patient.insuranceIdentifier ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_patient_label_insurance_id)
    )
}

@Composable
private fun PractitionerInformation(
    practitioner: SyncedTaskData.Practitioner
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
    organization: SyncedTaskData.Organization
) {
    SubHeader(
        text = stringResource(id = R.string.pres_detail_organization_header)
    )

    Label(
        text = organization.name ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_organization_label_name)
    )

    Label(
        text = organization.address?.joinToString() ?: MISSING_VALUE,
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
    medicationRequest: SyncedTaskData.MedicationRequest
) {
    SubHeader(
        text = stringResource(id = R.string.pres_detail_accident_header)
    )

    Label(
        text = remember(LocalConfiguration.current, medicationRequest.dateOfAccident) {
            val dtFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            medicationRequest.dateOfAccident?.let {
                LocalDateTime.ofInstant(it, ZoneId.systemDefault())
                    .toLocalDate()
                    .format(dtFormatter)
            } ?: MISSING_VALUE
        },
        label = stringResource(id = R.string.pres_detail_accident_label_date)
    )

    Label(
        text = medicationRequest.location ?: MISSING_VALUE,
        label = stringResource(id = R.string.pres_detail_accident_label_location)
    )
}

@Composable
private fun TechnicalPrescriptionInformation(accessCode: String?, taskId: String) {
    SubHeader(stringResource(R.string.pres_detail_technical_information))

    if (accessCode != null) {
        Label(
            text = accessCode,
            label = stringResource(id = R.string.access_code)
        )
    }

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
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    clipboardManager.setText(AnnotatedString(text))
                    Toast
                        .makeText(context, "$label $text", Toast.LENGTH_SHORT)
                        .show()
                }
            )
            .padding(PaddingDefaults.Medium)
            .fillMaxWidth()
    ) {
        Text(
            text = text,
            style = AppTheme.typography.body1
        )
        Spacer4()
        Text(
            text = label,
            style = AppTheme.typography.body2,
            color = AppTheme.typographyColors.body2l
        )
    }
}

@Composable
private fun Header(
    text: String
) = Text(
    text = text,
    style = AppTheme.typography.h6,
    fontWeight = FontWeight(500),
    modifier = Modifier.padding(
        start = PaddingDefaults.Medium,
        end = PaddingDefaults.Medium,
        top = PaddingDefaults.Medium * 1.5f
    )
)

@Composable
private fun SubHeader(
    text: String
) =
    Text(
        text = text,
        style = AppTheme.typography.subtitle1,
        fontWeight = FontWeight(500),
        modifier = Modifier.padding(
            top = 40.dp,
            end = PaddingDefaults.Medium,
            start = PaddingDefaults.Medium,
            bottom = PaddingDefaults.Medium
        )
    )

@Composable
private fun EmergencyServiceCard() {
    Card(
        modifier = Modifier
            .padding(
                start = PaddingDefaults.Medium,
                top = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium
            )
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
                    style = AppTheme.typography.subtitle1
                )
                Text(
                    stringResource(R.string.pres_detail_noctu_info),
                    style = AppTheme.typography.body2
                )
            }
        }
    }
}

@Composable
fun SubstitutionAllowed() {
    HintCard(
        modifier = Modifier.padding(PaddingDefaults.Medium),
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
