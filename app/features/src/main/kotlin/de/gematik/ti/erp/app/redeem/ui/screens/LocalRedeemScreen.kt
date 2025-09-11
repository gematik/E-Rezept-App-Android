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

package de.gematik.ti.erp.app.redeem.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.eurezept.navigation.EuRoutes
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.prescription.detail.presentation.rememberPrescriptionDetailController
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.redeem.model.DMCode
import de.gematik.ti.erp.app.redeem.presentation.rememberLocalRedeemScreenController
import de.gematik.ti.erp.app.redeem.ui.components.DataMatrixCodesWithSelfPayerWarning
import de.gematik.ti.erp.app.redeem.ui.preview.LocalRedeemPreview
import de.gematik.ti.erp.app.redeem.ui.preview.LocalRedeemPreviewParameter
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerXXXLarge
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.TertiaryButton
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.forceBrightness
import de.gematik.ti.erp.app.utils.uistate.UiState

@Requirement(
    "A_20181-01#1",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Only the title of the prescription and the DMC consisting of Task-ID and Access-Code are displayed."
)
class LocalRedeemScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val taskId = navBackStackEntry.arguments?.getString("taskId")
        val controller = rememberLocalRedeemScreenController(taskId ?: "")
        val prescriptionDetailsController = rememberPrescriptionDetailController(taskId ?: "")
        val profilePrescriptionData by prescriptionDetailsController.profilePrescription.collectAsStateWithLifecycle()
        val isPrescriptionEuRedeemable = (profilePrescriptionData.data?.second as? PrescriptionData.Synced)?.isEuRedeemable ?: false

        val codes by controller.dmCodes.collectAsStateWithLifecycle()
        val showSingleCodes by controller.showSingleCodes.collectAsStateWithLifecycle()
        var sharedWarningHeight by remember { mutableIntStateOf(0) }

        val activity = LocalActivity.current
        activity.forceBrightness()

        val dialog = LocalDialog.current
        val onClickEvent = ComposableEvent<Unit>()
        val onClose = {
            navController.navigate(PrescriptionRoutes.PrescriptionListScreen.route)
        }

        BackHandler {
            if (codes.data?.any { it.containsScanned } == true) {
                onClickEvent.trigger(Unit)
            } else {
                onClose()
            }
        }

        MarKScannedPrescriptionsRedeemedDialog(dialog, onClickEvent, onClose = onClose) {
            controller.redeemPrescriptions()
        }

        LocalRedeemScreenScaffold(
            codes = codes,
            isPrescriptionEuRedeemable = isPrescriptionEuRedeemable,
            showSingleCodes = showSingleCodes,
            sharedWarningHeight = sharedWarningHeight,
            onClickReady = {
                if (codes.data?.any { it.containsScanned } == true) {
                    onClickEvent.trigger(Unit)
                } else {
                    onClose()
                }
            },
            onSwitchSingleCodes = {
                controller.switchSingleCode()
                controller.getDmCodes()
            },
            onEuPrescriptionClick = {
                navController.navigate(EuRoutes.EuConsentScreen.path(taskId))
            },
            onRefreshCodes = { controller.refreshDmCodes() },
            onSharedWarningHeightUpdated = { sharedWarningHeight = it },
            onBack = { navController.popBackStack() }
        )
    }
}

@Composable
private fun LocalRedeemScreenScaffold(
    codes: UiState<List<DMCode>>,
    isPrescriptionEuRedeemable: Boolean,
    showSingleCodes: Boolean,
    sharedWarningHeight: Int,
    onClickReady: () -> Unit,
    onSwitchSingleCodes: () -> Unit,
    onEuPrescriptionClick: () -> Unit,
    onRefreshCodes: () -> Unit,
    onSharedWarningHeightUpdated: (Int) -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()
    val pagerState = rememberPagerState { codes.data?.size ?: 0 }

    AnimatedElevationScaffold(
        listState = listState,
        navigationMode = NavigationBarMode.Back,
        topBarTitle = "",
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        onBack = onBack,
        actions = {
            TextButton(
                onClick = onClickReady
            ) {
                Text(stringResource(R.string.local_redeem_done))
            }
        }
    ) { padding ->

        UiStateMachine(
            state = codes,
            onLoading = {
                Center {
                    CircularProgressIndicator()
                }
            },
            onError = {
                ErrorScreenComponent { onRefreshCodes() }
            },
            onEmpty = {
                ErrorScreenComponent { onRefreshCodes() }
            },
            onContent = { codes ->
                LocalRedeemScreenContent(
                    sharedWarningHeight = sharedWarningHeight,
                    isPrescriptionEuRedeemable = isPrescriptionEuRedeemable,
                    onEuPrescriptionClick = onEuPrescriptionClick,
                    padding = padding,
                    pagerState = pagerState,
                    codes = codes,
                    listState = listState,
                    showSingleCodes = showSingleCodes,
                    onSharedWarningHeightUpdated = onSharedWarningHeightUpdated
                ) {
                    onSwitchSingleCodes()
                }
            }
        )
    }
}

@Composable
private fun LocalRedeemScreenContent(
    sharedWarningHeight: Int,
    isPrescriptionEuRedeemable: Boolean,
    onEuPrescriptionClick: () -> Unit,
    padding: PaddingValues,
    listState: LazyListState,
    pagerState: PagerState,
    codes: List<DMCode>,
    showSingleCodes: Boolean,
    onSharedWarningHeightUpdated: (Int) -> Unit,
    onClick: () -> Unit
) {
    LazyColumn(
        Modifier
            .padding(padding)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        state = listState
    ) {
        item {
            HorizontalPager(
                state = pagerState
            ) { page ->
                if (page in codes.indices) {
                    DataMatrixCodesWithSelfPayerWarning(
                        dmCode = codes[page],
                        showSingleCodes = showSingleCodes,
                        sharedWarningHeight = sharedWarningHeight,
                        onSharedWarningHeightUpdated = onSharedWarningHeightUpdated
                    )
                }
            }
        }
        item {
            if (codes.size > 1 || (codes.size == 1 && codes.first().nrOfCodes > 1)) {
                PageIndicator(
                    modifier = Modifier,
                    pagerState = pagerState
                )
                Spacer(Modifier.fillMaxWidth())
                TertiaryButton(
                    modifier = Modifier
                        .padding(bottom = PaddingDefaults.XXLarge)
                        .navigationBarsPadding(),
                    onClick = onClick
                ) {
                    if (showSingleCodes) {
                        Text(stringResource(R.string.local_redeem_grouped_codes))
                    } else {
                        Text(stringResource(R.string.local_redeem_single_codes))
                    }
                }
            }
        }

        if (isPrescriptionEuRedeemable) {
            item {
                SpacerXXXLarge()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .clickable { onEuPrescriptionClick() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.eu_prescription_available),
                            style = AppTheme.typography.body1,
                            color = AppTheme.colors.primary700,
                            textAlign = TextAlign.Start
                        )

                        Spacer(modifier = Modifier.width(SizeDefaults.half))

                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = AppTheme.colors.primary700,
                            modifier = Modifier.size(SizeDefaults.triple)
                        )
                    }
                }
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun PageIndicator(
    modifier: Modifier,
    pagerState: PagerState
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(SizeDefaults.one))
            .background(AppTheme.colors.neutral100)
            .padding(vertical = PaddingDefaults.Small, horizontal = PaddingDefaults.Medium)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
        ) {
            repeat(pagerState.pageCount) {
                Dot(color = AppTheme.colors.neutral300)
            }
        }

        val fraction = pagerState.currentPage + pagerState.currentPageOffsetFraction
        val offsetX = with(LocalDensity.current) {
            val gap = PaddingDefaults.Small.roundToPx()
            val size = SizeDefaults.one.roundToPx()
            (fraction * (size + gap)).toDp()
        }
        Dot(modifier = Modifier.offset(x = offsetX), color = AppTheme.colors.primary500)
    }
}

@Composable
private fun Dot(modifier: Modifier = Modifier, color: Color) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .size(SizeDefaults.one)
    )
}

@Composable
private fun MarKScannedPrescriptionsRedeemedDialog(
    dialog: DialogScaffold,
    onClickEvent: ComposableEvent<Unit>,
    onClose: () -> Unit,
    markScannedAsRedeemed: () -> Unit
) {
    onClickEvent.listen {
        dialog.show { dialog ->
            ErezeptAlertDialog(
                title = stringResource(R.string.redeem_prescriptions_dialog_header),
                bodyText = stringResource(R.string.redeem_prescriptions_dialog_info),
                confirmText = stringResource(R.string.redeem_prescriptions_dialog_redeem),
                dismissText = stringResource(R.string.redeem_prescriptions_dialog_cancel),
                onDismissRequest = {
                    onClose()
                    dialog.dismiss()
                },
                onConfirmRequest = {
                    markScannedAsRedeemed()
                    onClose()
                    dialog.dismiss()
                }
            )
        }
    }
}

@LightDarkPreview
@Composable
fun LocalRedeemScreenPreview(
    @PreviewParameter(LocalRedeemPreviewParameter::class) previewData: LocalRedeemPreview
) {
    PreviewAppTheme {
        LocalRedeemScreenScaffold(
            codes = previewData.dmCodes,
            isPrescriptionEuRedeemable = true,
            showSingleCodes = previewData.showSingleCodes,
            sharedWarningHeight = 0,
            onEuPrescriptionClick = {},
            onClickReady = {},
            onSwitchSingleCodes = {},
            onRefreshCodes = {},
            onSharedWarningHeightUpdated = {},
            onBack = {}
        )
    }
}
