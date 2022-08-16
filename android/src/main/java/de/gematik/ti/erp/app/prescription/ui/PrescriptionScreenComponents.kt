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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.mainscreen.ui.MlKitPermissionDialog
import de.gematik.ti.erp.app.mainscreen.ui.PrescriptionTabs
import de.gematik.ti.erp.app.mainscreen.ui.RefreshScaffold
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.ui.model.PrescriptionScreenData
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import de.gematik.ti.erp.app.profiles.ui.LocalProfileHandler
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.dateWithIntroductionString
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.dateString
import de.gematik.ti.erp.app.utils.compose.phrasedDateString
import de.gematik.ti.erp.app.utils.compose.timeString
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun PrescriptionScreen(
    navController: NavController,
    prescriptionViewModel: PrescriptionViewModel,
    listState: LazyListState,
    selectedTab: PrescriptionTabs,
    onEmptyScreenChange: (PrescriptionScreenData.EmptyActiveScreenState) -> Unit
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
        onShowCardWall = onShowCardWall
    ) { onRefresh ->
        Prescriptions(
            prescriptionViewModel = prescriptionViewModel,
            onClickRefresh = {
                onRefresh(true, MutatePriority.UserInput)
            },
            listState = listState,
            navController = navController,
            selectedTab = selectedTab,
            onEmptyScreenChange = onEmptyScreenChange
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

private val HeaderPaddingModifier = Modifier
    .padding(
        top = PaddingDefaults.Large,
        bottom = PaddingDefaults.Medium,
        start = PaddingDefaults.Medium,
        end = PaddingDefaults.Medium
    )
    .fillMaxWidth()

private val CardPaddingModifier = Modifier
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
    listState: LazyListState,
    selectedTab: PrescriptionTabs,
    onEmptyScreenChange: (PrescriptionScreenData.EmptyActiveScreenState) -> Unit
) {
    val state by produceState<PrescriptionScreenData.State?>(null) {
        prescriptionViewModel.screenState().collect {
            value = it
        }
    }

    state?.let {
        when (selectedTab) {
            PrescriptionTabs.Redeemable -> ActivePrescriptionTab(
                onClickRefresh = onClickRefresh,
                state = it,
                listState = listState,
                navController = navController,
                onEmptyScreenChange = onEmptyScreenChange
            )
            PrescriptionTabs.Archive -> ArchivePrescriptionTab(
                state = it,
                listState = listState,
                navController = navController
            )
        }
    }
}

private val FabPadding = 68.dp

@Composable
private fun ActivePrescriptionTab(
    onClickRefresh: () -> Unit,
    state: PrescriptionScreenData.State,
    navController: NavController,
    listState: LazyListState,
    onEmptyScreenChange: (PrescriptionScreenData.EmptyActiveScreenState) -> Unit
) {
    val profileHandler = LocalProfileHandler.current
    val emptyScreen = state.emptyActiveScreen(profileHandler.activeProfile)
    var showMlKitPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(emptyScreen) {
        onEmptyScreenChange(emptyScreen)
    }

    if (showMlKitPermissionDialog) {
        MlKitPermissionDialog(
            onAccept = {
                navController.navigate(MainNavigationScreens.Camera.path())
                showMlKitPermissionDialog = false
            },
            onDecline = {
                showMlKitPermissionDialog = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = if (emptyScreen != PrescriptionScreenData.EmptyActiveScreenState.NotEmpty) {
            PaddingValues(0.dp)
        } else {
            PaddingValues(bottom = FabPadding)
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (emptyScreen != PrescriptionScreenData.EmptyActiveScreenState.NotEmpty) {
            Arrangement.Center
        } else {
            Arrangement.Top
        }
    ) {
        when (emptyScreen) {
            PrescriptionScreenData.EmptyActiveScreenState.LoggedIn -> {
                item {
                    HomeHealthCardConnected(
                        modifier = Modifier.padding(PaddingDefaults.Medium),
                        onClickAction = onClickRefresh
                    )
                }
            }
            PrescriptionScreenData.EmptyActiveScreenState.LoggedOut -> {
                item {
                    HomeHealthCardDisconnected(
                        modifier = Modifier.padding(PaddingDefaults.Medium),
                        onClickAction = onClickRefresh
                    )
                }
            }
            PrescriptionScreenData.EmptyActiveScreenState.NeverConnected -> {
                item {
                    HomeNoHealthCard(
                        modifier = Modifier.padding(PaddingDefaults.Medium),
                        onClickAction = {
                            showMlKitPermissionDialog = true
                        }
                    )
                }
            }
            PrescriptionScreenData.EmptyActiveScreenState.LoggedOutWithoutTokenBiometrics -> {
                item {
                    HomeConnectedWithoutTokenBiometrics(
                        modifier = Modifier.padding(PaddingDefaults.Medium),
                        onClickAction = onClickRefresh
                    )
                }
            }
            PrescriptionScreenData.EmptyActiveScreenState.LoggedOutWithoutToken -> {
                item {
                    HomeConnectedWithoutToken(
                        modifier = Modifier.padding(PaddingDefaults.Medium),
                        onClickAction = onClickRefresh
                    )
                }
            }
            PrescriptionScreenData.EmptyActiveScreenState.NotEmpty -> {
                prescriptionContent(
                    state = state,
                    navController = navController
                )
            }
        }
    }
}

private fun LazyListScope.prescriptionContent(
    navController: NavController,
    state: PrescriptionScreenData.State
) {
    item { SpacerXXLarge() }
    state.prescriptions.forEachIndexed { index, prescription ->
        item(key = "$index-${prescription.taskId}") {
            val isFirstSyncedPrescription =
                remember { index == 0 && prescription is PrescriptionUseCaseData.Prescription.Synced }

            val titleChanged = remember {
                index > 0 &&
                    (state.prescriptions[index - 1] as? PrescriptionUseCaseData.Prescription.Synced)?.organization !=
                    (prescription as? PrescriptionUseCaseData.Prescription.Synced)?.organization
            }

            if (isFirstSyncedPrescription) {
                Text(
                    (prescription as? PrescriptionUseCaseData.Prescription.Synced)?.organization ?: "",
                    style = AppTheme.typography.h6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(CardPaddingModifier)
                )
            } else if (titleChanged) {
                Text(
                    (prescription as? PrescriptionUseCaseData.Prescription.Synced)?.organization ?: "",
                    style = AppTheme.typography.h6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(HeaderPaddingModifier)
                )
            }

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

@Composable
private fun ArchivePrescriptionTab(
    state: PrescriptionScreenData.State,
    listState: LazyListState,
    navController: NavController
) {
    val profileHandler = LocalProfileHandler.current
    val emptyScreen = state.emptyArchiveScreen(profileHandler.activeProfile)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (emptyScreen != PrescriptionScreenData.EmptyArchiveScreenState.NotEmpty) {
            Arrangement.Center
        } else {
            Arrangement.Top
        }
    ) {
        when (emptyScreen) {
            PrescriptionScreenData.EmptyArchiveScreenState.NeverConnected -> {
                item {
                    ArchiveNoHealthCardInitial(
                        modifier = Modifier.padding(PaddingDefaults.Medium)
                    )
                }
            }
            PrescriptionScreenData.EmptyArchiveScreenState.NothingArchived -> {
                item {
                    ArchiveNoHealthCardRedeemed(
                        modifier = Modifier.padding(PaddingDefaults.Medium)
                    )
                }
            }
            else -> {
                item { SpacerXXLarge() }
                state.redeemedPrescriptions.forEachIndexed { index, prescription ->
                    item {
                        val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy") }

                        val isFirstArchievedPrescription =
                            remember { index == 0 }

                        val indexedPrescription = if (index > 0) {
                            state.redeemedPrescriptions[index - 1]
                        } else {
                            prescription
                        }
                        val instantOfArchivedPrescriptionIndexed by remember {
                            mutableStateOf(
                                indexedPrescription.redeemedOn ?: when (indexedPrescription) {
                                    is PrescriptionUseCaseData.Prescription.Synced -> indexedPrescription.authoredOn
                                    is PrescriptionUseCaseData.Prescription.Scanned -> indexedPrescription.scannedOn
                                }
                            )
                        }

                        val instantOfArchivedPrescription by remember {
                            mutableStateOf(
                                prescription.redeemedOn ?: when (prescription) {
                                    is PrescriptionUseCaseData.Prescription.Synced -> prescription.authoredOn
                                    is PrescriptionUseCaseData.Prescription.Scanned -> prescription.scannedOn
                                }
                            )
                        }

                        val yearChanged = remember {
                            index > 0 &&
                                (
                                    instantOfArchivedPrescriptionIndexed.atZone(ZoneId.systemDefault())
                                        ?.toLocalDate()?.format(dateFormatter) !=
                                        instantOfArchivedPrescription.atZone(ZoneId.systemDefault())
                                            ?.toLocalDate()?.format(dateFormatter)
                                    )
                        }

                        if (isFirstArchievedPrescription) {
                            Text(
                                instantOfArchivedPrescription.atZone(ZoneId.systemDefault())
                                    ?.toLocalDate()?.format(dateFormatter).toString(),
                                style = AppTheme.typography.h6,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(CardPaddingModifier)
                            )
                        } else if (yearChanged) {
                            Text(
                                instantOfArchivedPrescription.atZone(ZoneId.systemDefault())
                                    ?.toLocalDate()?.format(dateFormatter).toString(),
                                style = AppTheme.typography.h6,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(CardPaddingModifier)
                            )
                        }

                        when (prescription) {
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
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun FullDetailRecipeCardPreview() {
    AppTheme {
        FullDetailMedication(
            modifier = Modifier,
            prescription =
            PrescriptionUseCaseData.Prescription.Synced(
                "",
                organization = "Medizinisches-Versorgungszentrum (MVZ) welches irgendeinen sehr langen Namen hat",
                name = "Pantoprazol 40 mg - Medikament mit sehr vielen Namensbestandteilen",
                authoredOn = Instant.now(),
                redeemedOn = null,
                expiresOn = Instant.now().plus(21, ChronoUnit.DAYS),
                acceptUntil = Instant.now().minus(1, ChronoUnit.DAYS),
                state = SyncedTaskData.SyncedTask.Other(SyncedTaskData.TaskStatus.InProgress),
                isDirectAssignment = false
            ),
            onClick = {}
        )

        FullDetailMedication(
            modifier = Modifier,
            prescription =
            PrescriptionUseCaseData.Prescription.Synced(
                organization = "Medizinisches-Versorgungszentrum (MVZ) welches irgendeinen sehr langen Namen hat",
                taskId = "",
                name = "Pantoprazol 40 mg",
                authoredOn = Instant.now(),
                redeemedOn = null,
                expiresOn = Instant.now().plus(20, ChronoUnit.DAYS),
                acceptUntil = Instant.now().plus(97, ChronoUnit.DAYS),
                state = SyncedTaskData.SyncedTask.Other(SyncedTaskData.TaskStatus.Other),
                isDirectAssignment = false
            ),
            onClick = {}
        )

        FullDetailMedication(
            modifier = Modifier,
            prescription =
            PrescriptionUseCaseData.Prescription.Synced(
                organization = "Medizinisches-Versorgungszentrum (MVZ) welches irgendeinen sehr langen Namen hat",
                taskId = "",
                name = "Pantoprazol 40 mg",
                authoredOn = Instant.now(),
                redeemedOn = null,
                expiresOn = Instant.now(),
                acceptUntil = Instant.now().plus(1, ChronoUnit.DAYS),
                state = SyncedTaskData.SyncedTask.Other(SyncedTaskData.TaskStatus.Completed),
                isDirectAssignment = false
            ),
            onClick = {}
        )
    }
}

@Composable
fun expiryOrAcceptString(
    state: SyncedTaskData.SyncedTask.TaskState,
    now: LocalDate = LocalDate.now(),
    nowDateTime: LocalDateTime = LocalDateTime.now()
): String =
    when (state) {
        is SyncedTaskData.SyncedTask.Other -> ""
        is SyncedTaskData.SyncedTask.Ready -> {
            val expiryDaysLeft = remember {
                LocalDateTime.ofInstant(state.expiresOn, ZoneId.systemDefault()).toLocalDate()
                    .toEpochDay() - now.toEpochDay()
            }
            val acceptDaysLeft = remember {
                LocalDateTime.ofInstant(state.acceptUntil, ZoneId.systemDefault()).toLocalDate()
                    .toEpochDay() - now.toEpochDay()
            }

            when {
                acceptDaysLeft == 0L -> {
                    stringResource(id = R.string.prescription_item_accept_only_today)
                }
                expiryDaysLeft == 1L -> {
                    stringResource(id = R.string.prescription_item_expiration_only_today)
                }

                else ->
                    if (acceptDaysLeft > 1L) {
                        annotatedPluralsResource(
                            R.plurals.prescription_item_accept_days,
                            acceptDaysLeft.toInt(),
                            AnnotatedString(acceptDaysLeft.toString())
                        ).toString()
                    } else {
                        annotatedPluralsResource(
                            R.plurals.prescription_item_expiration_days_new,
                            expiryDaysLeft.toInt(),
                            AnnotatedString(expiryDaysLeft.toString())
                        ).toString()
                    }
            }
        }
        is SyncedTaskData.SyncedTask.InProgress -> {
            val lastModified = remember { LocalDateTime.ofInstant(state.lastModified, ZoneId.systemDefault()) }
            val dayDifference = remember {
                LocalDateTime.ofInstant(state.lastModified, ZoneId.systemDefault()).toLocalDate()
                    .toEpochDay() - now.toEpochDay()
            }
            val minDifference = remember { Duration.between(lastModified, nowDateTime).toMinutes() }
            when {
                minDifference < 5L -> stringResource(R.string.sent_now)
                minDifference < 60L -> annotatedStringResource(
                    R.string.sent_x_min_ago,
                    minDifference
                ).toString()
                dayDifference < 0L -> annotatedStringResource(
                    R.string.sent_on_day,
                    remember { dateString(lastModified) }
                ).toString()
                else -> annotatedStringResource(
                    R.string.sent_on_minute,
                    remember { timeString(lastModified) }
                ).toString()
            }
        }
        is SyncedTaskData.SyncedTask.Pending -> {
            val sentOn = remember { LocalDateTime.ofInstant(state.sentOn, ZoneId.systemDefault()) }

            annotatedStringResource(
                R.string.sent_on_txt_code_description,
                phrasedDateString(sentOn)
            ).toString()
        }
        is SyncedTaskData.SyncedTask.Expired -> {
            dateWithIntroductionString(R.string.pres_detail_medication_expired_on, state.expiredOn)
        }
    }

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun FullDetailMedication(
    prescription: PrescriptionUseCaseData.Prescription.Synced,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color = AppTheme.colors.neutral300),
        elevation = 0.dp,
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(PaddingDefaults.Medium)) {
            Column(modifier = Modifier.weight(1f)) {
                when (prescription.state) {
                    is SyncedTaskData.SyncedTask.Other -> {
                        when (prescription.state.state) {
                            SyncedTaskData.TaskStatus.Completed -> CompletedStatusChip()
                            else -> UnknownStatusChip()
                        }
                    }
                    is SyncedTaskData.SyncedTask.InProgress -> InProgressStatusChip()
                    is SyncedTaskData.SyncedTask.Pending -> PendingStatusChip()
                    is SyncedTaskData.SyncedTask.Ready -> ReadyStatusChip()
                    is SyncedTaskData.SyncedTask.Expired -> ExpiredStatusChip()
                }

                Spacer(Modifier.height(PaddingDefaults.Small + PaddingDefaults.Tiny))

                Text(
                    prescription.name,
                    style = AppTheme.typography.subtitle1
                )

                val text = if (prescription.redeemedOn != null) {
                    dateWithIntroductionString(
                        R.string.pres_detail_medication_redeemed_on,
                        prescription.redeemedOn
                    )
                } else {
                    if (prescription.isDirectAssignment) {
                        stringResource(R.string.direct_assignment_will_be_forwardet)
                    } else {
                        expiryOrAcceptString(prescription.state)
                    }
                } ?: ""

                Text(
                    text,
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
private fun LowDetailMedication(
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
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color = AppTheme.colors.neutral300),
        elevation = 0.dp,
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
