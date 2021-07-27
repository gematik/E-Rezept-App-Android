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

package de.gematik.ti.erp.app.prescription.ui

import android.text.format.DateUtils
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.SwipeableState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.QrCode
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
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.min
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.api.Result
import de.gematik.ti.erp.app.common.usecase.model.PrescriptionScreenHintDefineSecurity
import de.gematik.ti.erp.app.common.usecase.model.PrescriptionScreenHintDemoModeActivated
import de.gematik.ti.erp.app.common.usecase.model.PrescriptionScreenHintNewPrescriptions
import de.gematik.ti.erp.app.common.usecase.model.PrescriptionScreenHintTryDemoMode
import de.gematik.ti.erp.app.core.AppModel
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.component1
import de.gematik.ti.erp.app.core.component2
import de.gematik.ti.erp.app.demo.ui.DemoBanner
import de.gematik.ti.erp.app.idp.repository.SingleSignOnToken
import de.gematik.ti.erp.app.idp.usecase.RefreshFlowException
import de.gematik.ti.erp.app.mainscreen.ui.MainScreenFragmentDirections
import de.gematik.ti.erp.app.mainscreen.ui.MainScreenViewModel
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import de.gematik.ti.erp.app.settings.ui.SettingsScrollTo
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.compose.testId
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

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
fun PrescriptionScreen() {
    val (mainVM: MainScreenViewModel, prescriptionVM: PrescriptionViewModel) = AppModel.viewModels

    val frNavCtr = AppModel.frNavController

    val refreshState = rememberSwipeableState(false)

    var showSecureHardwarePrompt by remember { mutableStateOf(false) }

    if (showSecureHardwarePrompt) {
        SecureHardwarePrompt(
            stringResource(R.string.alternate_auth_header),
            stringResource(R.string.alternate_auth_info),
            stringResource(R.string.cancel),
            onAuthenticate = {
                prescriptionVM.onAlternateAuthentication()
                showSecureHardwarePrompt = false
            },
            onCancel = { showSecureHardwarePrompt = false }
        )
    }

    LaunchedEffect(refreshState.currentValue) {
        try {
            if (refreshState.currentValue) {
                Timber.d("Pull2Fresh: refresh prescriptions")
                when (val r = prescriptionVM.refreshPrescriptions()) {
                    is Result.Error -> {
                        refreshState.snapTo(false)
                        (r.exception.cause as? RefreshFlowException)?.let {
                            if (it.userActionRequired) {
                                if (it.tokenScope == SingleSignOnToken.Scope.AlternateAuthentication) {
                                    showSecureHardwarePrompt = true
                                } else {
                                    frNavCtr.navigate(MainScreenFragmentDirections.actionMainScreenFragmentToCardWallFragment())
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            withContext(NonCancellable) {
                refreshState.animateTo(false)
            }
        }
    }

    val state by produceState(prescriptionVM.defaultState) {
        prescriptionVM.screenState().collect {
            value = it
        }
    }

    PullRefresh(refreshState) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (state.showDemoBanner) {
                DemoBanner {
                    mainVM.onDeactivateDemoMode()
                }
            }

            NavigationAnimation(mode = NavigationMode.Open) {
                val coroutineScope = rememberCoroutineScope()

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

                var visibleStateOfPrescriptions by remember { mutableStateOf(setOf<Int>()) }

                var prescriptionToEditName by remember {
                    mutableStateOf<PrescriptionUseCaseData.Recipe.Scanned?>(
                        null
                    )
                }
                prescriptionToEditName?.let { prescription ->
                    EditScannedPrescriptionsName(
                        name = prescription.title ?: "",
                        onDismissRequest = { prescriptionToEditName = null }
                    ) {
                        coroutineScope.launch {
                            prescriptionVM.editScannedPrescriptionsName(
                                it,
                                prescription
                            )
                        }
                        prescriptionToEditName = null
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        SpacerMedium()
                    }

                    items(state.hints, key = { it.hashCode() }) {
                        when (it) {
                            is PrescriptionScreenHintDemoModeActivated ->
                                PrescriptionScreenDemoModeActivatedCard(cardPaddingModifier) {
                                    prescriptionVM.onCloseHintCard(it)
                                }
                            is PrescriptionScreenHintTryDemoMode ->
                                PrescriptionScreenTryDemoModeCard(
                                    cardPaddingModifier,
                                    onClickAction = {
                                        frNavCtr.navigate(
                                            MainScreenFragmentDirections.actionMainScreenFragmentToSettingsFragment(
                                                SettingsScrollTo.DemoMode
                                            )
                                        )
                                    },
                                    onClose = { prescriptionVM.onCloseHintCard(it) }
                                )
                            is PrescriptionScreenHintDefineSecurity ->
                                PrescriptionScreenDefineSecurityCard(
                                    cardPaddingModifier,
                                    onClickAction = {
                                        frNavCtr.navigate(
                                            MainScreenFragmentDirections.actionMainScreenFragmentToSettingsFragment(
                                                SettingsScrollTo.Authentication
                                            )
                                        )
                                    }
                                )
                            is PrescriptionScreenHintNewPrescriptions ->
                                PrescriptionScreenNewPrescriptionsCard(
                                    cardPaddingModifier,
                                    it.count,
                                    onClickAction = {
                                        mainVM.onClickRecipeScannedCard(
                                            state.prescriptions.mapNotNull {
                                                it as? PrescriptionUseCaseData.Recipe.Scanned
                                            }
                                        )
                                    }
                                )
                        }
                    }

                    item {
                        RefreshDivider(
                            modifier = headerPaddingModifier,
                            onClickRefresh = {
                                coroutineScope.launch {
                                    refreshState.animateTo(true)
                                }
                            }
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

                        items(state.prescriptions) { recipe ->
                            val key = recipe.hashCode()
                            val visibleState =
                                remember(key) { MutableTransitionState(key in visibleStateOfPrescriptions) }

                            DisposableEffect(visibleState.currentState) {
                                onDispose {
                                    if (visibleState.currentState) {
                                        visibleStateOfPrescriptions =
                                            visibleStateOfPrescriptions + key
                                    }
                                }
                            }

                            AnimatedVisibility(
                                visibleState = visibleState.apply { targetState = true },
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut(),
                            ) {
                                when (recipe) {
                                    is PrescriptionUseCaseData.Recipe.Synced -> FullDetailRecipeCard(
                                        cardPaddingModifier,
                                        recipe = recipe,
                                        nowInEpochDays = state.nowInEpochDays,
                                        onClickPrescription = {
                                            frNavCtr.navigate(
                                                MainScreenFragmentDirections.actionMainScreenFragmentToPrescriptionDetailFragment(
                                                    it.taskId
                                                )
                                            )
                                        },
                                        onClickRedeem = { mainVM.onClickRecipeCard(recipe) }
                                    )
                                    is PrescriptionUseCaseData.Recipe.Scanned -> {
                                        LowDetailRecipeCard(
                                            cardPaddingModifier,
                                            recipe = recipe,
                                            onClickEditPrescriptionName = {
                                                prescriptionToEditName = it
                                            },
                                            onClickPrescription = {
                                                frNavCtr.navigate(
                                                    MainScreenFragmentDirections.actionMainScreenFragmentToPrescriptionDetailFragment(
                                                        it.taskId
                                                    )
                                                )
                                            },
                                            onClickRedeem = { mainVM.onClickRecipeCard(recipe) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        RedeemedHeader(headerPaddingModifier)
                    }

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
                        items(state.redeemedPrescriptions) { recipe ->
                            RecipeRedeemedCard(
                                modifier = cardPaddingModifier,
                                recipe = recipe,
                                nowInEpochDays = state.nowInEpochDays
                            )
                        }
                    }
                }
            }
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
                .testId("erx_txt_empty_list")
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

@Composable
private fun PrescriptionScreenCard(
    modifier: Modifier = Modifier,
    border: BorderStroke? = BorderStroke(0.5.dp, AppTheme.colors.neutral300),
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    content: @Composable () -> Unit
) =
    Card(
        border = border,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        elevation = 2.dp,
        content = content
    )

@Composable
private fun FullDetailRecipeCard(
    modifier: Modifier,
    recipe: PrescriptionUseCaseData.Recipe.Synced,
    nowInEpochDays: Long,
    onClickPrescription: (PrescriptionUseCaseData.Prescription.Synced) -> Unit,
    onClickRedeem: (PrescriptionUseCaseData.Recipe.Synced) -> Unit
) {
    PrescriptionScreenCard(modifier) {
        Column {
            Row(
                modifier = Modifier.padding(
                    start = PaddingDefaults.Medium,
                    end = PaddingDefaults.Medium,
                    top = PaddingDefaults.Medium
                )
            ) {
                Text(
                    recipe.organization,
                    style = AppTheme.typography.subtitle2l,
                    modifier = Modifier.weight(1f)
                )

                val shortDate = DateUtils.getRelativeTimeSpanString(
                    recipe.authoredOn.toEpochSecond() * 1000L,
                    System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS
                ).toString()

                Text(
                    shortDate,
                    style = AppTheme.typography.body2l,
                    modifier = Modifier.layoutId("date")
                )
            }

            SpacerSmall()

            Column {
                recipe.prescriptions.forEachIndexed { index, med ->
                    FullDetailMedication(
                        med,
                        recipe.redeemedOn,
                        nowInEpochDays,
                        modifier = Modifier.padding(PaddingDefaults.Medium),
                        onClick = { onClickPrescription(med) }
                    )

                    if (index < recipe.prescriptions.size - 1) {
                        Divider(startIndent = 16.dp)
                    }
                }
            }

            RedeemButton(
                onClick = { onClickRedeem(recipe) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .layoutId("redeem")
                    .testId("prescription/redeemFullDetailButton")
            )
        }
    }
}

@Preview
@Composable
private fun FullDetailRecipeCardPreview() {
    AppTheme {
        FullDetailRecipeCard(
            Modifier,
            recipe = PrescriptionUseCaseData.Recipe.Synced(
                organization = "Medizinisches-Versorgungszentrum (MVZ) welches irgendeinen sehr langen Namen hat",
                authoredOn = OffsetDateTime.now(),
                prescriptions = listOf(
                    PrescriptionUseCaseData.Prescription.Synced(
                        "",
                        "Pantoprazol 40 mg - Medikament mit sehr vielen Namensbestandteilen",
                        expiresOn = LocalDate.now()
                    ),
                    PrescriptionUseCaseData.Prescription.Synced(
                        "",
                        "Pantoprazol 40 mg",
                        expiresOn = LocalDate.now()
                    ),
                    PrescriptionUseCaseData.Prescription.Synced(
                        "",
                        "Pantoprazol 40 mg",
                        expiresOn = LocalDate.now()
                    ),
                ),
                redeemedOn = null
            ),
            nowInEpochDays = Duration.between(LocalDateTime.MIN, LocalDateTime.now()).toDays(),
            onClickRedeem = {},
            onClickPrescription = {}
        )
    }
}

@Composable
private fun expiryString(date: LocalDate, nowInEpochDays: Long): String {

    val daysLeft = date.toEpochDay() - nowInEpochDays

    return when {
        daysLeft == 1L -> {
            stringResource(id = R.string.prescription_item_expiration_only_today)
        }
        daysLeft <= 0L -> {
            stringResource(id = R.string.prescription_item_expiration_expired)
        }
        else -> {
            annotatedPluralsResource(
                R.plurals.prescription_item_expiration_days,
                daysLeft.toInt(),
                AnnotatedString(daysLeft.toString())
            ).toString()
        }
    }
}

@Composable
private fun FullDetailMedication(
    prescription: PrescriptionUseCaseData.Prescription.Synced,
    redeemedOn: OffsetDateTime?,
    nowInEpochDays: Long,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(modifier)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                prescription.name,
                style = MaterialTheme.typography.subtitle1
            )

            val text = if (redeemedOn != null) {
                val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                redeemedOn.toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate().format(dateFormatter)
            } else {
                prescription.expiresOn?.let {
                    expiryString(it, nowInEpochDays)
                } ?: ""
            }

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
                .align(CenterVertically)
        )
    }
}

@Composable
private fun LowDetailRecipeCard(
    modifier: Modifier,
    recipe: PrescriptionUseCaseData.Recipe.Scanned,
    onClickEditPrescriptionName: (PrescriptionUseCaseData.Recipe.Scanned) -> Unit,
    onClickPrescription: (PrescriptionUseCaseData.Prescription.Scanned) -> Unit,
    onClickRedeem: (PrescriptionUseCaseData.Recipe.Scanned) -> Unit
) {
    PrescriptionScreenCard(modifier) {
        Column {
            val lDr = LocalLayoutDirection.current
            val buttonOffsetX = ButtonDefaults.TextButtonContentPadding.calculateLeftPadding(lDr)
            val buttonOffsetY = ButtonDefaults.TextButtonContentPadding.calculateTopPadding()

            Row(
                modifier = Modifier.padding(
                    start = PaddingDefaults.Medium,
                    end = PaddingDefaults.Medium,
                    top = PaddingDefaults.Medium
                )
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    TextButton(
                        onClick = {
                            onClickEditPrescriptionName(recipe)
                        },
                        modifier = Modifier
                            .offset(x = -buttonOffsetX, y = -buttonOffsetY)
                    ) {
                        val name = buildAnnotatedString {
                            append(
                                if (!recipe.title.isNullOrBlank()) {
                                    recipe.title
                                } else {
                                    stringResource(R.string.prs_low_detail_default_name)
                                }
                            )
                            appendInlineContent("pen", "pen")
                        }

                        val c = mapOf(
                            "pen" to InlineTextContent(
                                Placeholder(
                                    width = 1.5.em,
                                    height = 1.5.em,
                                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                                )
                            ) {
                                Icon(
                                    Icons.Filled.Edit, null,
                                    tint = MaterialTheme.colors.secondary,
                                    modifier = Modifier
                                        .padding(4.dp)
                                )
                            }
                        )

                        Text(
                            name,
                            color = MaterialTheme.colors.secondary,
                            style = MaterialTheme.typography.subtitle2,
                            inlineContent = c
                        )
                    }
                }

                val shortDate = DateUtils.getRelativeTimeSpanString(
                    recipe.scanSessionEnd.toEpochSecond() * 1000L,
                    System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS
                ).toString()

                Text(
                    shortDate,
                    style = AppTheme.typography.body2l
                )
            }

            Spacer(modifier = Modifier.size(min(0.dp, 4.dp - buttonOffsetX)))

            Column {

                recipe.prescriptions.forEachIndexed { index, med ->
                    LowDetailMedication(med, onClick = { onClickPrescription(med) })

                    if (index < recipe.prescriptions.size - 1) {
                        Divider(startIndent = 56.dp)
                    }
                }
            }

            RedeemButton(
                onClick = { onClickRedeem(recipe) },
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(56.dp)
                    .layoutId("redeem")
                    .testId("prescription/redeemLowDetailButton")
            )
        }
    }
}

@Preview
@Composable
private fun LowDetailRecipeCardPreview() {
    AppTheme {
        LowDetailRecipeCard(
            Modifier,
            recipe = PrescriptionUseCaseData.Recipe.Scanned(
                title = "Medizinisches-Versorgungszentrum (MVZ) welches irgendeinen sehr langen Namen hat",
                scanSessionEnd = OffsetDateTime.now(),
                prescriptions = listOf(
                    PrescriptionUseCaseData.Prescription.Scanned(
                        "",
                        1,
                    ),
                    PrescriptionUseCaseData.Prescription.Scanned(
                        "",
                        2,
                    ),
                    PrescriptionUseCaseData.Prescription.Scanned(
                        "",
                        3,
                    )
                ),
                redeemedOn = null
            ),
            onClickEditPrescriptionName = {},
            onClickRedeem = {},
            onClickPrescription = {}
        )
    }
}

@Preview
@Composable
private fun LowDetailRecipeCardPreviewWithoutName() {
    AppTheme {
        LowDetailRecipeCard(
            Modifier,
            recipe = PrescriptionUseCaseData.Recipe.Scanned(
                title = null,
                scanSessionEnd = OffsetDateTime.now(),
                prescriptions = listOf(
                    PrescriptionUseCaseData.Prescription.Scanned(
                        "",
                        1,
                    ),
                    PrescriptionUseCaseData.Prescription.Scanned(
                        "",
                        2,
                    ),
                    PrescriptionUseCaseData.Prescription.Scanned(
                        "",
                        3,
                    )
                ),
                redeemedOn = null
            ),
            onClickEditPrescriptionName = { },
            onClickRedeem = {},
            onClickPrescription = {}
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun RecipeRedeemedCard(
    modifier: Modifier = Modifier,
    recipe: PrescriptionUseCaseData.Recipe,
    nowInEpochDays: Long
) {
    val frNavCtr = AppModel.frNavController
    var expanded by remember { mutableStateOf(false) }

    PrescriptionScreenCard(modifier) {

        var name = ""
        var creationDate: OffsetDateTime? = null
        var redeemedOn: OffsetDateTime? = null
        var nrOfRedeemedPrescriptions = 0

        when (recipe) {
            is PrescriptionUseCaseData.Recipe.Scanned -> {
                creationDate = recipe.scanSessionEnd
                redeemedOn = recipe.redeemedOn
                nrOfRedeemedPrescriptions = recipe.prescriptions.size
                name = if (!recipe.title.isNullOrBlank()) {
                    recipe.title
                } else {
                    stringResource(R.string.prs_low_detail_default_name)
                }
            }
            is PrescriptionUseCaseData.Recipe.Synced -> {
                creationDate = recipe.authoredOn
                redeemedOn = recipe.redeemedOn
                nrOfRedeemedPrescriptions = recipe.prescriptions.size
                name = recipe.organization
            }
        }

        val shortDate = DateUtils.getRelativeTimeSpanString(
            creationDate.toEpochSecond() * 1000L,
            System.currentTimeMillis(),
            DateUtils.DAY_IN_MILLIS
        ).toString()

        Column {
            Row(
                modifier = Modifier.padding(PaddingDefaults.Medium)
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        name,
                        color = AppTheme.colors.neutral600,
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier.align(Alignment.TopStart)
                    )
                }
                Text(
                    shortDate,
                    style = MaterialTheme.typography.body2,
                    color = AppTheme.typographyColors.body2l,
                )
            }

            AnimatedVisibility(visible = expanded) {

                Column {
                    when (recipe) {
                        is PrescriptionUseCaseData.Recipe.Scanned -> {
                            recipe.prescriptions.forEachIndexed { index, med ->
                                LowDetailMedication(med) {
                                    frNavCtr.navigate(
                                        MainScreenFragmentDirections.actionMainScreenFragmentToPrescriptionDetailFragment(
                                            med.taskId
                                        )
                                    )
                                }
                                if (index < recipe.prescriptions.size - 1) {
                                    Divider(startIndent = 56.dp)
                                }
                            }
                        }
                        is PrescriptionUseCaseData.Recipe.Synced -> {
                            recipe.prescriptions.forEachIndexed { index, med ->
                                FullDetailMedication(
                                    med,
                                    redeemedOn,
                                    nowInEpochDays,
                                    modifier = Modifier.padding(PaddingDefaults.Medium)
                                ) {
                                    val action =
                                        MainScreenFragmentDirections.actionMainScreenFragmentToPrescriptionDetailFragment(
                                            med.taskId
                                        )
                                    frNavCtr.navigate(action)
                                }
                                if (index < recipe.prescriptions.size - 1) {
                                    Divider(startIndent = 16.dp)
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = AppTheme.colors.neutral100)
                    .clickable(onClick = { expanded = !expanded })
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        redeemedOn.let {
                            val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                            val redeemedOnFormatted =
                                it!!.toInstant().atZone(ZoneId.systemDefault())
                                    .toLocalDate().format(dateFormatter)
                            Row {
                                Text(
                                    redeemedPrescriptions(nrOfRedeemedPrescriptions),
                                    style = MaterialTheme.typography.body1,
                                )
                            }
                            Row {
                                Text(
                                    stringResource(
                                        R.string.prs_redeemed_on,
                                        redeemedOnFormatted
                                    ),
                                    style = AppTheme.typography.body2l,
                                )
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .align(CenterVertically)
                    ) {
                        val accExpand = stringResource(R.string.prs_acc_expand_redeemed)
                        IconToggleButton(
                            checked = expanded,
                            onCheckedChange = { expanded = it },
                            modifier = Modifier
                                .semantics { contentDescription = accExpand }
                        ) {
                            val ic = if (expanded) {
                                Icons.Rounded.KeyboardArrowUp
                            } else {
                                Icons.Rounded.KeyboardArrowDown
                            }
                            Icon(
                                ic, null,
                                tint = AppTheme.colors.neutral400,
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(End),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun redeemedPrescriptions(count: Int) =
    annotatedPluralsResource(
        R.plurals.prs_nr_redeemed,
        count,
        buildAnnotatedString { append(count.toString()) }
    )

@Composable
private fun RedeemButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(
        onClick = onClick,
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            contentColor = AppTheme.colors.primary700,
            backgroundColor = AppTheme.colors.neutral100
        ),
        modifier = modifier
    ) {
        Text(stringResource(R.string.prescription_group_submit_ready).uppercase(Locale.getDefault()))
    }
}

@Composable
private fun LowDetailMedication(
    prescription: PrescriptionUseCaseData.Prescription.Scanned,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(modifier = modifier.clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                Icons.Rounded.QrCode, null,
                tint = MaterialTheme.colors.secondary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                stringResource(R.string.prs_low_detail_medication_name, prescription.nr),
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .weight(1f)
            )
            Icon(
                Icons.Filled.KeyboardArrowRight, null,
                tint = AppTheme.colors.neutral400,
                modifier = Modifier.size(24.dp)
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
            modifier = Modifier.align(CenterVertically)
        )
        Spacer(modifier = Modifier.weight(1.0f))
        TextButton(
            onClick = onClickRefresh,
            modifier = Modifier
                .align(CenterVertically)
                .testId("erx_btn_refresh")
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

@Composable
private fun EditScannedPrescriptionsName(
    name: String,
    onDismissRequest: () -> Unit,
    onEdit: (text: String) -> Unit
) {
    var textValue by remember { mutableStateOf(name) }

    AlertDialog(
        title = {
            Text(
                stringResource(R.string.pres_edit_txt_title),
                style = MaterialTheme.typography.subtitle1,
            )
        },
        onDismissRequest = onDismissRequest,
        text = {
            Column() {
                Text(
                    stringResource(R.string.pres_edit_txt_info),
                    style = MaterialTheme.typography.body2
                )
                Box(modifier = Modifier.padding(top = 12.dp)) {
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = {
                            textValue = it
                        },
                        placeholder = { Text(stringResource(R.string.pres_edit_txt_place_holder)) }
                    )
                }
            }
        },
        buttons = {
            Row(Modifier.padding(bottom = 12.dp, start = 12.dp, end = 12.dp)) {
                Spacer(modifier = Modifier.weight(1.0f))
                TextButton(onClick = { onDismissRequest() }) {
                    Text(stringResource(R.string.cancel).uppercase(Locale.getDefault()))
                }
                TextButton(onClick = { onEdit(textValue) }) {
                    Text(stringResource(R.string.ok).uppercase(Locale.getDefault()))
                }
            }
        },
    )
}
