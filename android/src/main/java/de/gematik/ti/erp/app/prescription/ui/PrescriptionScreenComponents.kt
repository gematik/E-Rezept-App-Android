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

import android.net.Uri
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SwipeableState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.common.usecase.model.PrescriptionScreenHintDefineSecurity
import de.gematik.ti.erp.app.common.usecase.model.PrescriptionScreenHintDemoModeActivated
import de.gematik.ti.erp.app.common.usecase.model.PrescriptionScreenHintTryDemoMode
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.demo.ui.DemoBanner
import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.mainscreen.ui.MainScreenViewModel
import de.gematik.ti.erp.app.mainscreen.ui.PullRefreshState
import de.gematik.ti.erp.app.prescription.ui.model.PrescriptionScreenData
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import de.gematik.ti.erp.app.settings.ui.SettingsScrollTo
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@Composable
fun SecureHardwarePrompt(
    title: String,
    description: String,
    negativeButton: String,
    onAuthenticate: () -> Unit,
    onCancel: () -> Unit,
) {
    val activity = LocalActivity.current as FragmentActivity

    val executor = remember { ContextCompat.getMainExecutor(activity) }

    val callback = remember {
        object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) {
                super.onAuthenticationSucceeded(result)

                onAuthenticate()
            }

            override fun onAuthenticationError(
                errCode: Int,
                errString: CharSequence
            ) {
                super.onAuthenticationError(errCode, errString)

                Timber.e("Failed to authenticate: $errString")

                onCancel()
            }
        }
    }

    val prompt = remember { BiometricPrompt(activity, executor, callback) }
    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setNegativeButtonText(negativeButton)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            )
            .build()
    }

    DisposableEffect(prompt) {
        prompt.authenticate(promptInfo)

        onDispose {
            prompt.cancelAuthentication()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun PrescriptionScreen(
    navController: NavController,
    mainViewModel: MainScreenViewModel = hiltViewModel(LocalActivity.current),
    prescriptionViewModel: PrescriptionViewModel = hiltViewModel(),
    uri: Uri?
) {
    val refreshState = rememberSwipeableState(false)

    var pullRefreshState by remember { mutableStateOf(PullRefreshState.None) }
    LaunchedEffect(Unit) {
        // we need a short delay until layout calculations are done;
        // otherwise we will run into the anchor null problem of the swipeable state
        delay(100)
        mainViewModel.refreshState().collect {
            pullRefreshState = it
            when (pullRefreshState) {
                PullRefreshState.IsFirstTimeBiometricAuthentication,
                PullRefreshState.HasFirstTimeValidToken,
                PullRefreshState.HasValidToken -> {
                    refreshState.animateTo(true)
                }
                else -> {
                    refreshState.snapTo(false)
                }
            }
        }
    }

    var showSecureHardwarePrompt by remember { mutableStateOf(false) }
    if (showSecureHardwarePrompt) {
        SecureHardwarePrompt(
            stringResource(R.string.alternate_auth_header),
            stringResource(R.string.alternate_auth_info),
            stringResource(R.string.cancel),
            onAuthenticate = {
                prescriptionViewModel.onAlternateAuthentication()
                showSecureHardwarePrompt = false
            },
            onCancel = { showSecureHardwarePrompt = false }
        )
    }

    val state by produceState(prescriptionViewModel.defaultState) {
        prescriptionViewModel.screenState().collect {
            value = it
        }
    }

    LaunchedEffect(refreshState.currentValue) {
        try {
            if (refreshState.currentValue) {
                prescriptionViewModel.refreshPrescriptions(
                    pullRefreshState = pullRefreshState,
                    isDemoModeActive = state.showDemoBanner,
                    onShowSecureHardwarePrompt = {
                        showSecureHardwarePrompt = true
                    },
                    onShowCardWall = { canAvailable ->
                        withContext(Dispatchers.Main) {
                            // TODO: find a better way
                            pullRefreshState = PullRefreshState.None
                            refreshState.snapTo(false)

                            navController.navigate(
                                MainNavigationScreens.CardWall.path(
                                    canAvailable
                                )
                            )
                        }
                    },
                    onRefresh = mainViewModel::onRefresh
                )
            }
        } finally {
            withContext(NonCancellable) {
                pullRefreshState = PullRefreshState.None
                refreshState.animateTo(false)
            }
        }
    }

    PullRefresh(refreshState) {
        val modifier = Modifier
        Box {
            Column(modifier = Modifier.fillMaxSize()) {
                if (state.showDemoBanner) {
                    DemoBanner {
                        mainViewModel.onDeactivateDemoMode()
                    }
                }

                NavigationAnimation(mode = NavigationMode.Open) {
                    val coroutineScope = rememberCoroutineScope()

                    Prescriptions(
                        onClickRefresh = {
                            coroutineScope.launch { refreshState.animateTo(true) }
                        },
                        prescriptionViewModel = prescriptionViewModel,
                        state = state,
                        navController = navController,
                        uri = uri
                    )
                }
            }
            // todo FastTrack: combine success/error result from FastTrack auth process with app.
            // Processing = banner, Error = Dialog?
            uri?.let {
                var showBanner by remember { mutableStateOf(true) }
                if (showBanner) Banner(modifier.align(Alignment.BottomEnd)) { showBanner = false }
//                mainViewModel.onExternAppAuthorizationResult(it)
            }
        }
    }
}

@Composable
private fun Prescriptions(
    prescriptionViewModel: PrescriptionViewModel,
    state: PrescriptionScreenData.State,
    navController: NavController,
    onClickRefresh: () -> Unit,
    uri: Uri?
) {
    val cardPaddingModifier = Modifier
        .padding(
            bottom = PaddingDefaults.Medium,
            start = PaddingDefaults.Medium,
            end = PaddingDefaults.Medium
        )
        .fillMaxWidth()
    val headerPaddingModifier = Modifier
        .padding(
            top = PaddingDefaults.XLarge,
            bottom = PaddingDefaults.Small,
            start = PaddingDefaults.Medium,
            end = PaddingDefaults.Medium
        )
        .fillMaxWidth()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 68.dp), // padding for fab
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { SpacerMedium() }

        // TODO: remove hints
        items(state.hints, key = { it.hashCode() }) {
            when (it) {
                is PrescriptionScreenHintDemoModeActivated ->
                    PrescriptionScreenDemoModeActivatedCard(cardPaddingModifier) {
                        prescriptionViewModel.onCloseHintCard(it)
                    }
                is PrescriptionScreenHintTryDemoMode ->
                    PrescriptionScreenTryDemoModeCard(
                        cardPaddingModifier,
                        onClickAction = {
                            navController.navigate(
                                MainNavigationScreens.Settings.path(
                                    SettingsScrollTo.DemoMode
                                )
                            )
                        },
                        onClose = { prescriptionViewModel.onCloseHintCard(it) }
                    )
                is PrescriptionScreenHintDefineSecurity ->
                    PrescriptionScreenDefineSecurityCard(
                        cardPaddingModifier,
                        onClickAction = {
                            navController.navigate(
                                MainNavigationScreens.Settings.path(
                                    SettingsScrollTo.Authentication
                                )
                            )
                        }
                    )
            }
        }

        item {
            RefreshDivider(
                modifier = headerPaddingModifier,
                onClickRefresh = onClickRefresh
            )
        }

        if (state.prescriptions.isEmpty()) {
            item {
                NothingToShowNote(
                    cardPaddingModifier.padding(
                        top = PaddingDefaults.Small
                    ),
                    stringResource(R.string.prescription_overview_info_no_recipes)
                )
            }
        } else {
            itemsIndexed(state.prescriptions) { index, prescription ->
                val isFirstSyncedPrescription =
                    (index == 0 && prescription is PrescriptionUseCaseData.Prescription.Synced)
                val titleChanged = (
                    index > 0 &&
                        (state.prescriptions[index - 1] as? PrescriptionUseCaseData.Prescription.Synced)?.organization !=
                        (prescription as? PrescriptionUseCaseData.Prescription.Synced)?.organization
                    )

                if (isFirstSyncedPrescription || titleChanged) {
                    Text(
                        (prescription as? PrescriptionUseCaseData.Prescription.Synced)?.organization ?: "",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(headerPaddingModifier)
                    )
                }

                when (prescription) {
                    is PrescriptionUseCaseData.Prescription.Synced ->
                        FullDetailMedication(
                            prescription,
                            state.nowInEpochDays,
                            modifier = cardPaddingModifier,
                            onClick = {
                                navController.navigate(
                                    MainNavigationScreens.PrescriptionDetail.path(
                                        prescription.taskId
                                    )
                                )
                            }
                        )

                    is PrescriptionUseCaseData.Prescription.Scanned ->
                        LowDetailMedication(
                            modifier = cardPaddingModifier,
                            prescription,
                            onClick = {
                                navController.navigate(
                                    MainNavigationScreens.PrescriptionDetail.path(
                                        prescription.taskId
                                    )
                                )
                            }
                        )
                }
            }
        }

        item { RedeemedHeader(headerPaddingModifier) }

        if (state.redeemedPrescriptions.isEmpty()) {
            item {
                NothingToShowNote(
                    cardPaddingModifier.padding(
                        top = PaddingDefaults.Small
                    ),
                    stringResource(R.string.prs_not_redeemed_note)
                )
            }
        } else {
            items(state.redeemedPrescriptions) { prescription ->
                when (prescription) {
                    is PrescriptionUseCaseData.Prescription.Scanned ->
                        LowDetailMedication(
                            modifier = cardPaddingModifier,
                            prescription,
                            onClick = {
                                navController.navigate(
                                    MainNavigationScreens.PrescriptionDetail.path(
                                        prescription.taskId
                                    )
                                )
                            }
                        )
                    is PrescriptionUseCaseData.Prescription.Synced ->
                        FullDetailMedication(
                            prescription,
                            state.nowInEpochDays,
                            modifier = cardPaddingModifier,
                            onClick = {
                                navController.navigate(
                                    MainNavigationScreens.PrescriptionDetail.path(
                                        prescription.taskId
                                    )
                                )
                            }
                        )
                }
            }
        }
    }
}

@Composable
private fun Banner(modifier: Modifier, onClose: () -> Unit) {
    Card(modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.padding(end = 16.dp), AppTheme.colors.neutral400)
            Text(
                text = stringResource(R.string.main_banner_authentication_text), Modifier.weight(1f), color = AppTheme.colors.neutral700,
                style = AppTheme.typography.body2l
            )
            Image(Icons.Rounded.Close, null, modifier = Modifier.padding(start = 20.dp).clickable { onClose() }, alpha = 0.3f)
        }
    }
}

@Composable
private fun NothingToShowNote(
    modifier: Modifier = Modifier,
    text: String
) {
    Card(
        modifier = modifier,
        backgroundColor = AppTheme.colors.neutral100,
        contentColor = AppTheme.colors.neutral600,
        elevation = 1.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text,
            textAlign = TextAlign.Center,
            style = AppTheme.typography.body2l,
            modifier = Modifier
                .padding(16.dp)
                .testTag("erx_txt_empty_list")
        )
    }
}

// TODO remove if https://issuetracker.google.com/issues/162408885 is resolved
// Source: PreUpPostDownNestedScrollConnection is currently internal in compose but we need the same
// behavior for our pull/swipe to refresh layout
@OptIn(ExperimentalMaterialApi::class)
private fun <T> SwipeableState<T>.preUpPostDownNestedScrollConnection(minBound: Float): NestedScrollConnection =
    object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val delta = available.toFloat()
            return if (delta < 0 && source == NestedScrollSource.Drag) {
                performDrag(delta).toOffset()
            } else {
                Offset.Zero
            }
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            return if (source == NestedScrollSource.Drag) {
                performDrag(available.toFloat()).toOffset()
            } else {
                Offset.Zero
            }
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            val toFling = Offset(available.x, available.y).toFloat()
            return if (toFling > 0 && offset.value > minBound) {
                performFling(velocity = toFling)
                // since we go to the anchor with tween settling, consume all for the best UX
                available
            } else {
                Velocity.Zero
            }
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            performFling(velocity = Offset(available.x, available.y).toFloat())
            return available
        }

        private fun Float.toOffset(): Offset = Offset(0f, this)

        private fun Offset.toFloat(): Float = this.y
    }

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PullRefresh(
    state: SwipeableState<Boolean>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val refreshDistance = with(LocalDensity.current) { 80.dp.toPx() }

    Box(
        modifier = modifier
            .testTag("pull2refresh")
            .nestedScroll(state.preUpPostDownNestedScrollConnection(-refreshDistance))
            .swipeable(
                state = state,
                anchors = mapOf(
                    -refreshDistance to false,
                    refreshDistance to true
                ),
                orientation = Orientation.Vertical,
            )
            .fillMaxSize()
    ) {
        content()

        val size = 48.dp
        val offset = if (!state.offset.value.isNaN()) state.offset.value else 0.0f
        val progress = offset / refreshDistance

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .wrapContentSize()
                .offset { IntOffset(y = offset.roundToInt(), x = 0) }
                .alpha(progress)
        ) {
            Card(
                shape = RoundedCornerShape(size / 2),
                elevation = 8.dp,
                modifier = Modifier
                    .padding(8.dp)
                    .size(size)
            ) {
                if (state.currentValue || state.isAnimationRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(4.dp)
                    )
                } else {
                    CircularProgressIndicator(
                        progress = progress,
                        modifier = Modifier.padding(4.dp)
                    )
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
                authoredOn = OffsetDateTime.now(),
                redeemedOn = null,
                expiresOn = LocalDate.now().plusDays(21),
                acceptUntil = LocalDate.now().plusDays(-1),
                status = PrescriptionUseCaseData.Prescription.Synced.Status.InProgress,
                isDirectAssignment = false,
            ),
            nowInEpochDays = Duration.between(
                LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC),
                LocalDateTime.now()
            ).toDays(),
            onClick = {}
        )

        FullDetailMedication(
            modifier = Modifier,
            prescription =
            PrescriptionUseCaseData.Prescription.Synced(
                organization = "Medizinisches-Versorgungszentrum (MVZ) welches irgendeinen sehr langen Namen hat",
                taskId = "",
                name = "Pantoprazol 40 mg",
                authoredOn = OffsetDateTime.now(),
                redeemedOn = null,
                expiresOn = LocalDate.now().plusDays(20),
                acceptUntil = LocalDate.now().plusDays(97),
                status = PrescriptionUseCaseData.Prescription.Synced.Status.Unknown,
                isDirectAssignment = false
            ),
            nowInEpochDays = Duration.between(
                LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC),
                LocalDateTime.now()
            ).toDays(),
            onClick = {}
        )

        FullDetailMedication(
            modifier = Modifier,
            prescription =
            PrescriptionUseCaseData.Prescription.Synced(
                organization = "Medizinisches-Versorgungszentrum (MVZ) welches irgendeinen sehr langen Namen hat",
                taskId = "",
                name = "Pantoprazol 40 mg",
                authoredOn = OffsetDateTime.now(),
                redeemedOn = null,
                expiresOn = LocalDate.now(),
                acceptUntil = LocalDate.now().plusDays(1),
                status = PrescriptionUseCaseData.Prescription.Synced.Status.Completed,
                isDirectAssignment = false
            ),
            nowInEpochDays = Duration.between(
                LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC),
                LocalDateTime.now()
            ).toDays(),
            onClick = {}
        )
    }
}

@Composable
fun expiryOrAcceptString(
    expiryDate: LocalDate,
    acceptDate: LocalDate,
    nowInEpochDays: Long
): String {
    val expiryDaysLeft = remember { expiryDate.toEpochDay() - nowInEpochDays }
    val acceptDaysLeft = remember { acceptDate.toEpochDay() - nowInEpochDays }

    return when {
        acceptDaysLeft == 0L -> {
            stringResource(id = R.string.prescription_item_accept_only_today)
        }
        expiryDaysLeft == 1L -> {
            stringResource(id = R.string.prescription_item_expiration_only_today)
        }
        expiryDaysLeft <= 0L -> {
            stringResource(id = R.string.prescription_item_expired)
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun FullDetailMedication(
    prescription: PrescriptionUseCaseData.Prescription.Synced,
    nowInEpochDays: Long,
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
                when (prescription.status) {
                    PrescriptionUseCaseData.Prescription.Synced.Status.Ready ->
                        ReadyStatusChip()
                    PrescriptionUseCaseData.Prescription.Synced.Status.InProgress ->
                        InProgressStatusChip()
                    PrescriptionUseCaseData.Prescription.Synced.Status.Completed ->
                        CompletedStatusChip()
                    PrescriptionUseCaseData.Prescription.Synced.Status.Unknown ->
                        UnknownStatusChip()
                }

                Spacer(Modifier.height(PaddingDefaults.Small + PaddingDefaults.Tiny))

                Text(
                    prescription.name,
                    style = MaterialTheme.typography.subtitle1
                )

                val text = if (prescription.redeemedOn != null) {
                    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                    prescription.redeemedOn.toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDate().format(dateFormatter)
                } else {
                    if (prescription.isDirectAssignment) {
                        stringResource(R.string.direct_assignment_will_be_forwardet)
                    } else {
                        prescription.expiresOn?.let { expiryDate ->
                            prescription.acceptUntil?.let { acceptDate ->
                                expiryOrAcceptString(
                                    expiryDate = expiryDate,
                                    acceptDate = acceptDate,
                                    nowInEpochDays = nowInEpochDays
                                )
                            }
                        }
                    }
                } ?: ""

                Text(
                    text,
                    style = AppTheme.typography.body2l
                )
            }

            Icon(
                Icons.Filled.KeyboardArrowRight, null,
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
                OffsetDateTime.now(),
                redeemedOn = OffsetDateTime.now().plusDays(2)
            ),
            onClick = {},
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
        prescription.scannedOn.toInstant().atZone(ZoneId.systemDefault())
            .toLocalDate().format(dateFormatter)
    }

    val redeemedOn = remember {
        prescription.redeemedOn?.toInstant()?.atZone(ZoneId.systemDefault())
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
                    style = MaterialTheme.typography.subtitle1,
                )
                SpacerTiny()
                Text(
                    dateText,
                    style = AppTheme.typography.body2l,
                )
            }

            Icon(
                Icons.Filled.KeyboardArrowRight, null,
                tint = AppTheme.colors.neutral400,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

/**
 * | Current        X Refresh |
 */
@Composable
private fun RefreshDivider(modifier: Modifier = Modifier, onClickRefresh: () -> Unit) {
    Row(modifier = modifier) {
        Text(
            text = stringResource(R.string.prs_divider_refresh_info),
            style = MaterialTheme.typography.h6,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.weight(1.0f))
        TextButton(
            onClick = onClickRefresh,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .testTag("erx_btn_refresh")
        ) {
            Icon(
                Icons.Rounded.Update, null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 4.dp)
            )
            Text(
                stringResource(R.string.prs_divider_refresh_action),
                style = MaterialTheme.typography.subtitle2
            )
        }
    }
}

@Composable
private fun RedeemedHeader(modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Text(
            style = MaterialTheme.typography.h6,
            text = stringResource(R.string.prs_divider_redeemed_info),
        )
    }
}
