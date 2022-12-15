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

package de.gematik.ti.erp.app.prescription.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.navigation.NavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag

import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.mainscreen.ui.MainScreenViewModel
import de.gematik.ti.erp.app.mainscreen.ui.RefreshScaffold
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.ui.model.PrescriptionScreenData
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import de.gematik.ti.erp.app.prescriptionId
import de.gematik.ti.erp.app.profiles.ui.LocalProfileHandler
import de.gematik.ti.erp.app.profiles.ui.ProfileHandler
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.DynamicText
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerShortMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.dateWithIntroductionString
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.TertiaryButton
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.dateString
import de.gematik.ti.erp.app.utils.compose.timeString
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

const val ZERO_DAYS_LEFT = 0L
const val ONE_DAY_LEFT = 1L
const val TWO_DAYS_LEFT = 2L

@Composable
fun PrescriptionScreen(
    navController: NavController,
    prescriptionViewModel: PrescriptionViewModel,
    mainScreenViewModel: MainScreenViewModel,
    onClickAvatar: () -> Unit,
    onClickArchive: () -> Unit,
    onElevateTopBar: (Boolean) -> Unit
) {
    val profileHandler = LocalProfileHandler.current
    val profileId = profileHandler.activeProfile.id

    var showUserNotAuthenticatedDialog by remember { mutableStateOf(false) }

    val onShowCardWall = {
        navController.navigate(
            MainNavigationScreens.CardWall.path(profileHandler.activeProfile.id)
        )
    }

    if (showUserNotAuthenticatedDialog) {
        UserNotAuthenticatedDialog(
            onCancel = { showUserNotAuthenticatedDialog = false },
            onShowCardWall = onShowCardWall
        )
    }

    RefreshScaffold(
        profileId = profileId,
        onUserNotAuthenticated = { showUserNotAuthenticatedDialog = true },
        mainScreenViewModel = mainScreenViewModel,
        onShowCardWall = onShowCardWall
    ) { onRefresh ->
        Prescriptions(
            prescriptionViewModel = prescriptionViewModel,
            onClickRefresh = {
                onRefresh(true, MutatePriority.UserInput)
            },
            onClickAvatar = onClickAvatar,
            navController = navController,
            onElevateTopBar = onElevateTopBar,
            onClickArchive = onClickArchive
        )
    }
}

@Composable
fun UserNotAuthenticatedDialog(onCancel: () -> Unit, onShowCardWall: () -> Unit) {
    CommonAlertDialog(
        header = stringResource(R.string.user_not_authenticated_dialog_header),
        info = stringResource(R.string.user_not_authenticated_dialog_info),
        cancelText = stringResource(R.string.user_not_authenticated_dialog_cancel),
        actionText = stringResource(R.string.user_not_authenticated_dialog_connect),
        onCancel = onCancel
    ) {
        onShowCardWall()
    }
}

val CardPaddingModifier = Modifier
    .padding(
        bottom = PaddingDefaults.Medium,
        start = PaddingDefaults.Medium,
        end = PaddingDefaults.Medium
    )
    .fillMaxWidth()

@Composable
private fun Prescriptions(
    prescriptionViewModel: PrescriptionViewModel,
    navController: NavController,
    onClickRefresh: () -> Unit,
    onClickAvatar: () -> Unit,
    onClickArchive: () -> Unit,
    onElevateTopBar: (Boolean) -> Unit
) {
    val state by produceState<PrescriptionScreenData.State?>(null) {
        prescriptionViewModel.screenState().collect {
            value = it
        }
    }

    state?.let {
        PrescriptionsContent(
            onClickRefresh = onClickRefresh,
            onClickAvatar = onClickAvatar,
            state = it,
            navController = navController,
            onElevateTopBar = onElevateTopBar,
            onClickArchive = onClickArchive
        )
    }
}

private val FabPadding = 68.dp

@Composable
private fun PrescriptionsContent(
    onClickRefresh: () -> Unit,
    onClickAvatar: () -> Unit,
    onClickArchive: () -> Unit,
    state: PrescriptionScreenData.State,
    navController: NavController,
    onElevateTopBar: (Boolean) -> Unit
) {
    val listState = rememberLazyListState()
    val profileHandler = LocalProfileHandler.current

    LaunchedEffect(Unit) {
        snapshotFlow {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }.collect {
            onElevateTopBar(it)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTag.Prescriptions.Content),
        state = listState,
        contentPadding = PaddingValues(bottom = FabPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if (state.prescriptions.isNotEmpty()) {
            item {
                SpacerXXLarge()
                ProfileConnectionSection(onClickAvatar, onClickRefresh)
                SpacerMedium()
            }
            prescriptionContent(
                state = state,
                navController = navController
            )
        } else {
            emptyContent(profileHandler, onClickRefresh, onClickAvatar)
        }
        if (state.redeemedPrescriptions.isNotEmpty()) {
            item {
                SpacerLarge()
                TextButton(onClick = onClickArchive) {
                    Text(stringResource(R.string.archived_prescriptions_button))
                }
                SpacerLarge()
            }
        }
    }
}

fun LazyListScope.emptyContent(
    profileHandler: ProfileHandler,
    onClickConnect: () -> Unit,
    onClickAvatar: () -> Unit
) {
    item {
        Spacer(modifier = Modifier.size(80.dp))
        MainScreenAvatar(onClickAvatar)
    }
    if (profileHandler.connectionState(profileHandler.activeProfile) !=
        ProfileHandler.ProfileConnectionState.LoggedIn
    ) {
        item {
            SpacerMedium()
            TertiaryButton(onClickConnect, modifier = Modifier.testTag(TestTag.Main.LoginButton)) {
                Text(stringResource(R.string.mainscreen_login))
            }
        }
        item {
            SpacerLarge()
            Text(
                stringResource(R.string.mainscreen_empty_content_header),
                style = AppTheme.typography.subtitle1,
                modifier = Modifier.testTag(TestTag.Main.CenterScreenMessageField)
            )
        }
        item {
            SpacerSmall()
            Text(
                stringResource(R.string.mainscreen_empty_not_connected_info),
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                style = AppTheme.typography.body2,
                textAlign = TextAlign.Center
            )
        }
    } else {
        item {
            SpacerLarge()
            Text(stringResource(R.string.mainscreen_empty_content_header), style = AppTheme.typography.subtitle1)
        }
        item {
            SpacerMedium()
            Text(
                stringResource(R.string.mainscreen_empty_connected_info),
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                style = AppTheme.typography.body2,
                textAlign = TextAlign.Center
            )
            SpacerMedium()
            Icon(Icons.Rounded.ArrowDownward, null)
        }
    }
}

private fun LazyListScope.prescriptionContent(
    navController: NavController,
    state: PrescriptionScreenData.State
) {
    state.prescriptions.forEachIndexed { index, prescription ->
        item(key = "prescription-${prescription.taskId}") {
            when (prescription) {
                is PrescriptionUseCaseData.Prescription.Synced ->
                    FullDetailMedication(
                        prescription,
                        modifier = CardPaddingModifier,
                        onClick = {
                            navController.navigate(
                                MainNavigationScreens.PrescriptionDetail.path(
                                    taskId = prescription.taskId
                                )
                            )
                        }
                    )

                is PrescriptionUseCaseData.Prescription.Scanned ->
                    LowDetailMedication(
                        modifier = CardPaddingModifier,
                        prescription,
                        onClick = {
                            navController.navigate(
                                MainNavigationScreens.PrescriptionDetail.path(
                                    taskId = prescription.taskId
                                )
                            )
                        }
                    )
            }
        }
    }
}

@Preview
@Composable
private fun FullDetailRecipeCardPreview() {
    AppTheme {
        Column {
            FullDetailMedication(
                modifier = Modifier,
                prescription =
                PrescriptionUseCaseData.Prescription.Synced(
                    "",
                    organization = "Medizinisches-Versorgungszentrum (MVZ) welches irgendeinen sehr langen Namen hat",
                    name = "Pantoprazol 40 mg - Medikament mit sehr vielen Namensbestandteilen",
                    authoredOn = Instant.now(),
                    isIncomplete = false,
                    redeemedOn = null,
                    expiresOn = Instant.now().plus(21, ChronoUnit.DAYS),
                    acceptUntil = Instant.now().minus(1, ChronoUnit.DAYS),
                    state = SyncedTaskData.SyncedTask.Other(SyncedTaskData.TaskStatus.InProgress, Instant.now()),
                    isDirectAssignment = false,
                    multiplePrescriptionState = PrescriptionUseCaseData.Prescription.MultiplePrescriptionState()
                ),
                onClick = {}
            )
            SpacerMedium()

            FullDetailMedication(
                modifier = Modifier,
                prescription =
                PrescriptionUseCaseData.Prescription.Synced(
                    organization = "Medizinisches-Versorgungszentrum (MVZ) welches irgendeinen sehr langen Namen hat",
                    taskId = "",
                    name = "Pantoprazol 40 mg",
                    isIncomplete = false,
                    authoredOn = Instant.now(),
                    redeemedOn = null,
                    expiresOn = Instant.now().plus(20, ChronoUnit.DAYS),
                    acceptUntil = Instant.now().plus(97, ChronoUnit.DAYS),
                    state = SyncedTaskData.SyncedTask.Other(SyncedTaskData.TaskStatus.Other, Instant.now()),
                    isDirectAssignment = false,
                    multiplePrescriptionState = PrescriptionUseCaseData.Prescription.MultiplePrescriptionState()
                ),
                onClick = {}
            )
            SpacerMedium()

            FullDetailMedication(
                modifier = Modifier,
                prescription =
                PrescriptionUseCaseData.Prescription.Synced(
                    organization = "Medizinisches-Versorgungszentrum (MVZ) welches irgendeinen sehr langen Namen hat",
                    taskId = "",
                    name = "Pantoprazol 40 mg",
                    isIncomplete = false,
                    authoredOn = Instant.now(),
                    redeemedOn = null,
                    expiresOn = Instant.now(),
                    acceptUntil = Instant.now().plus(1, ChronoUnit.DAYS),
                    state = SyncedTaskData.SyncedTask.Other(SyncedTaskData.TaskStatus.Completed, Instant.now()),
                    isDirectAssignment = false,
                    multiplePrescriptionState = PrescriptionUseCaseData.Prescription.MultiplePrescriptionState()
                ),
                onClick = {}
            )
            SpacerMedium()

            FullDetailMedication(
                modifier = Modifier,
                prescription =
                PrescriptionUseCaseData.Prescription.Synced(
                    organization = "Medizinisches-Versorgungszentrum (MVZ) welches irgendeinen sehr langen Namen hat",
                    taskId = "12344",
                    name = "Pantoprazol 40 mg",
                    authoredOn = Instant.now(),
                    isIncomplete = false,
                    redeemedOn = null,
                    expiresOn = Instant.now().minus(1, ChronoUnit.DAYS),
                    acceptUntil = Instant.now().minus(1, ChronoUnit.DAYS),
                    state = SyncedTaskData.SyncedTask.LaterRedeemable(
                        Instant.now().minus(1, ChronoUnit.DAYS)
                    ),
                    isDirectAssignment = false,
                    multiplePrescriptionState = PrescriptionUseCaseData.Prescription.MultiplePrescriptionState(
                        isPartOfMultiplePrescription = true,
                        numerator = "1",
                        denominator = "4",
                        start = Instant.now()
                    )
                ),
                onClick = {}
            )

            SpacerMedium()
            FullDetailMedication(
                modifier = Modifier,
                prescription =
                PrescriptionUseCaseData.Prescription.Synced(
                    organization = "Medizinisches-Versorgungszentrum (MVZ) welches irgendeinen sehr langen Namen hat",
                    taskId = "12344",
                    name = "Medication",
                    authoredOn = Instant.now(),
                    isIncomplete = true,
                    redeemedOn = null,
                    expiresOn = Instant.now().minus(1, ChronoUnit.DAYS),
                    acceptUntil = Instant.now().minus(1, ChronoUnit.DAYS),
                    state = SyncedTaskData.SyncedTask.LaterRedeemable(
                        Instant.now().minus(1, ChronoUnit.DAYS)
                    ),
                    isDirectAssignment = false,
                    multiplePrescriptionState = PrescriptionUseCaseData.Prescription.MultiplePrescriptionState(
                        isPartOfMultiplePrescription = false,
                        numerator = null,
                        denominator = null,
                        start = null
                    )
                ),
                onClick = {}
            )
        }
    }
}

@Composable
fun readyPrescriptionStateInfo(
    acceptDaysLeft: Long,
    expiryDaysLeft: Long
): AnnotatedString? = when {
    acceptDaysLeft == ZERO_DAYS_LEFT -> buildAnnotatedString {
        appendInlineContent(
            id = "warningAmber",
            alternateText = stringResource(R.string.prescription_item_warning_amber)
        )
        append(stringResource(R.string.prescription_item_accept_only_today))
    }

    expiryDaysLeft == ZERO_DAYS_LEFT -> buildAnnotatedString {
        appendInlineContent(
            id = "warningAmber",
            alternateText = stringResource(R.string.prescription_item_warning_amber)
        )
        append(stringResource(R.string.prescription_item_expiration_only_today))
    }

    acceptDaysLeft in ONE_DAY_LEFT..TWO_DAYS_LEFT -> buildAnnotatedString {
        appendInlineContent(
            id = "warningAmber",
            alternateText = stringResource(R.string.prescription_item_warning_amber)
        )
        append(
            annotatedPluralsResource(
                R.plurals.prescription_item_accept_days,
                acceptDaysLeft.toInt(),
                AnnotatedString(acceptDaysLeft.toString())
            )
        )
    }

    expiryDaysLeft in ONE_DAY_LEFT..TWO_DAYS_LEFT -> buildAnnotatedString {
        appendInlineContent(
            id = "warningAmber",
            alternateText = stringResource(R.string.prescription_item_warning_amber)
        )
        append(
            annotatedPluralsResource(
                R.plurals.prescription_item_expiration_days_new,
                expiryDaysLeft.toInt(),
                AnnotatedString(expiryDaysLeft.toString())
            )
        )
    }

    acceptDaysLeft > TWO_DAYS_LEFT -> annotatedPluralsResource(
        R.plurals.prescription_item_accept_days,
        1 + acceptDaysLeft.toInt(),
        AnnotatedString((1 + acceptDaysLeft).toString())
    )

    expiryDaysLeft > TWO_DAYS_LEFT -> annotatedPluralsResource(
        R.plurals.prescription_item_expiration_days_new,
        1 + expiryDaysLeft.toInt(),
        AnnotatedString((1 + expiryDaysLeft).toString())
    )

    else -> null
}

@Composable
fun prescriptionStateInfo(
    state: SyncedTaskData.SyncedTask.TaskState,
    now: Instant = Instant.now(),
    textAlign: TextAlign = TextAlign.Left
) {
    val warningAmber = mapOf(
        "warningAmber" to InlineTextContent(
            Placeholder(
                width = 0.em,
                height = 0.em,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                modifier = Modifier.padding(end = PaddingDefaults.Tiny),
                contentDescription = null,
                tint = AppTheme.colors.red600
            )
        }
    )

    when (state) {
        is SyncedTaskData.SyncedTask.LaterRedeemable -> {
            Text(
                text = dateWithIntroductionString(
                    R.string.pres_detail_medication_redeemable_on,
                    state.redeemableOn
                ),
                style = AppTheme.typography.body2l,
                textAlign = textAlign
            )
        }

        is SyncedTaskData.SyncedTask.Ready -> {
            val expiryDaysLeft = remember { Duration.between(now, state.expiresOn).toDays() }
            val acceptDaysLeft = remember { Duration.between(now, state.acceptUntil).toDays() }

            val text = readyPrescriptionStateInfo(acceptDaysLeft, expiryDaysLeft)

            when {
                acceptDaysLeft in ZERO_DAYS_LEFT..TWO_DAYS_LEFT ||
                    expiryDaysLeft in ZERO_DAYS_LEFT..TWO_DAYS_LEFT ->
                    text?.let {
                        DynamicText(
                            it,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = AppTheme.typography.body2,
                            color = AppTheme.colors.red600,
                            inlineContent = warningAmber
                        )
                    }

                acceptDaysLeft > TWO_DAYS_LEFT || expiryDaysLeft > TWO_DAYS_LEFT ->
                    text?.let { Text(it, style = AppTheme.typography.body2, textAlign = textAlign) }

                else -> {}
            }
        }

        is SyncedTaskData.SyncedTask.InProgress -> {
            val lastModified = remember { LocalDateTime.ofInstant(state.lastModified, ZoneId.systemDefault()) }
            val text = sentOrCompletedPhrase(lastModified, now)
            Text(text, style = AppTheme.typography.body2, textAlign = textAlign)
        }

        is SyncedTaskData.SyncedTask.Pending -> {
            val sentOn = remember { LocalDateTime.ofInstant(state.sentOn, ZoneId.systemDefault()) }
            val text = sentOrCompletedPhrase(sentOn, now)
            Text(text, style = AppTheme.typography.body2, textAlign = textAlign)
        }

        is SyncedTaskData.SyncedTask.Expired -> {
            Text(
                dateWithIntroductionString(R.string.pres_detail_medication_expired_on, state.expiredOn),
                style = AppTheme.typography.body2,
                textAlign = textAlign
            )
        }

        is SyncedTaskData.SyncedTask.Other -> {
            if (state.state == SyncedTaskData.TaskStatus.Completed) {
                val completedOn = remember { LocalDateTime.ofInstant(state.lastModified, ZoneId.systemDefault()) }
                val text = sentOrCompletedPhrase(completedOn, now, true)
                Text(text, style = AppTheme.typography.body2, textAlign = textAlign)
            }
        }
    }
}

@Composable
private fun sentOrCompletedPhrase(lastModified: LocalDateTime, now: Instant, completed: Boolean = false): String {
    val dayDifference = remember {
        Duration.between(lastModified, now.atZone(ZoneId.systemDefault()).toLocalDateTime()).toDays()
    }
    val minDifference = remember {
        Duration.between(lastModified, now.atZone(ZoneId.systemDefault()).toLocalDateTime()).toMinutes()
    }
    val text = when {
        minDifference < 5L -> if (completed) {
            stringResource(R.string.received_now)
        } else {
            stringResource(R.string.sent_now)
        }

        minDifference < 60L -> if (completed) {
            annotatedStringResource(
                R.string.received_x_min_ago,
                minDifference
            ).toString()
        } else {
            annotatedStringResource(
                R.string.sent_x_min_ago,
                minDifference
            ).toString()
        }

        dayDifference < 0L -> if (completed) {
            annotatedStringResource(
                R.string.received_on_day,
                minDifference
            ).toString()
        } else {
            annotatedStringResource(
                R.string.sent_on_day,
                remember { dateString(lastModified) }
            ).toString()
        }

        else -> if (completed) {
            annotatedStringResource(
                R.string.received_on_minute,
                minDifference
            ).toString()
        } else {
            annotatedStringResource(
                R.string.sent_on_minute,
                remember { timeString(lastModified) }
            ).toString()
        }
    }
    return text
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FullDetailMedication(
    prescription: PrescriptionUseCaseData.Prescription.Synced,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val showDirectAssignmentLabel by derivedStateOf {
        val isCompleted =
            (prescription.state as? SyncedTaskData.SyncedTask.Other)?.state == SyncedTaskData.TaskStatus.Completed

        prescription.isDirectAssignment && !isCompleted
    }

    Card(
        modifier = modifier
            .semantics {
                prescriptionId = prescription.taskId
            }
            .testTag(TestTag.Prescriptions.FullDetailPrescription),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color = AppTheme.colors.neutral300),
        backgroundColor = AppTheme.colors.neutral050,
        elevation = 0.dp,
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(PaddingDefaults.Medium)) {
            Column(modifier = Modifier.weight(1f)) {
                if (prescription.isIncomplete) {
                    FailureStatusChip()
                } else if (showDirectAssignmentLabel) {
                    DirectAssignmentStatusChip()
                } else {
                    when (prescription.state) {
                        is SyncedTaskData.SyncedTask.InProgress -> InProgressStatusChip()
                        is SyncedTaskData.SyncedTask.Pending -> PendingStatusChip()
                        is SyncedTaskData.SyncedTask.Ready -> ReadyStatusChip()
                        is SyncedTaskData.SyncedTask.Expired -> ExpiredStatusChip()
                        is SyncedTaskData.SyncedTask.LaterRedeemable -> LaterRedeemableStatusChip()

                        is SyncedTaskData.SyncedTask.Other -> {
                            when (prescription.state.state) {
                                SyncedTaskData.TaskStatus.Completed -> CompletedStatusChip()
                                else -> UnknownStatusChip()
                            }
                        }
                    }
                }

                Spacer(Modifier.height(PaddingDefaults.Small + PaddingDefaults.Tiny))

                val medicationName =
                    prescription.name ?: stringResource(R.string.prescription_medication_default_name)

                Text(
                    modifier = Modifier.testTag(TestTag.Prescriptions.FullDetailPrescriptionName),
                    text = medicationName,
                    style = AppTheme.typography.subtitle1,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                if (!prescription.isDirectAssignment) {
                    prescriptionStateInfo(prescription.state)
                }

                if (prescription.multiplePrescriptionState.isPartOfMultiplePrescription) {
                    prescription.multiplePrescriptionState.numerator?.let { numerator ->
                        prescription.multiplePrescriptionState.denominator?.let { denominator ->
                            SpacerShortMedium()
                            NumeratorChip(numerator, denominator)
                        }
                    }
                }
            }

            Icon(
                Icons.Filled.KeyboardArrowRight,
                null,
                tint = AppTheme.colors.neutral400,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

@Preview
@Composable
private fun LowDetailRecipeCardPreview() {
    AppTheme {
        LowDetailMedication(
            Modifier,
            prescription = PrescriptionUseCaseData.Prescription.Scanned(
                "",
                Instant.now(),
                redeemedOn = Instant.now().plus(2, ChronoUnit.DAYS)
            ),
            onClick = {}
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LowDetailMedication(
    modifier: Modifier = Modifier,
    prescription: PrescriptionUseCaseData.Prescription.Scanned,
    onClick: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    val scannedOn = remember {
        prescription.scannedOn.atZone(ZoneId.systemDefault())
            .toLocalDate().format(dateFormatter)
    }

    val redeemedOn = remember {
        prescription.redeemedOn?.atZone(ZoneId.systemDefault())
            ?.toLocalDate()?.format(dateFormatter)
    }

    val dateText = if (redeemedOn != null) {
        stringResource(R.string.prs_low_detail_redeemed_on, redeemedOn)
    } else {
        stringResource(R.string.prs_low_detail_scanned_on, scannedOn)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color = AppTheme.colors.neutral300),
        elevation = 0.dp,
        backgroundColor = AppTheme.colors.neutral050,
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(PaddingDefaults.Medium)) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    stringResource(R.string.prs_low_detail_medication),
                    style = AppTheme.typography.subtitle1
                )
                SpacerTiny()
                Text(
                    dateText,
                    style = AppTheme.typography.body2l
                )
            }

            Icon(
                Icons.Filled.KeyboardArrowRight,
                null,
                tint = AppTheme.colors.neutral400,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}
