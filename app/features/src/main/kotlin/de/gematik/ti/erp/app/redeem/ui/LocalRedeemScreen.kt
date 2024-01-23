/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.redeem.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.pharmacy.presentation.PharmacyOrderController
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.DataMatrix
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.TertiaryButton
import de.gematik.ti.erp.app.utils.compose.createBitMatrix
import de.gematik.ti.erp.app.utils.extensions.forceBrightness
import kotlinx.coroutines.launch

@Requirement(
    "A_20181",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Only the title of the prescription and the DMC consisting of Task-ID and Access-Code are displayed."
)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun LocalRedeemScreen(
    activeProfile: ProfilesUseCaseData.Profile,
    orderState: PharmacyOrderController,
    onBack: () -> Unit,
    onFinished: () -> Unit
) {
    val localRedeemState = rememberLocalRedeemState(orderState)
    val codes by localRedeemState.codes

    if (codes.isEmpty()) {
        return
    }

    val activity = LocalActivity.current
    activity.forceBrightness()

    val redeemController = rememberRedeemController(activeProfile)
    var showRedeemScannedDialog by remember { mutableStateOf(false) }
    if (showRedeemScannedDialog) {
        val scope = rememberCoroutineScope()
        val prescriptions by orderState.prescriptionsState
        RedeemScannedPrescriptionsDialog(
            onClickRedeem = {
                scope.launch {
                    redeemController.redeemScannedTasks(prescriptions.map { it.taskId })
                    onFinished()
                }
            },
            onCancel = onFinished
        )
    }

    Scaffold(
        topBar = {
            NavigationTopAppBar(
                navigationMode = NavigationBarMode.Back,
                elevation = 0.dp,
                title = "",
                onBack = onBack,
                actions = {
                    TextButton(
                        onClick = {
                            if (codes.any { it.containsScanned }) {
                                showRedeemScannedDialog = true
                            } else {
                                onFinished()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.local_redeem_done))
                    }
                }
            )
        }
    ) { innerPadding ->
        val pagerState = rememberPagerState()

        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            HorizontalPager(
                count = codes.size,
                state = pagerState
            ) { page ->
                DataMatrix(
                    modifier = Modifier
                        .padding(PaddingDefaults.Medium)
                        .fillMaxWidth(),
                    code = codes[page]
                )
            }
            if (codes.size > 1 || (codes.size == 1 && codes.first().nrOfCodes > 1)) {
                PageIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    pagerState = pagerState
                )
                Spacer(Modifier.weight(1f))
                TertiaryButton(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = PaddingDefaults.XXLarge)
                        .navigationBarsPadding(),
                    onClick = {
                        if (localRedeemState.isSingleCodes) {
                            localRedeemState.onSwitchToGroupedCodes()
                        } else {
                            localRedeemState.onSwitchToSingleCodes()
                        }
                    }
                ) {
                    if (localRedeemState.isSingleCodes) {
                        Text(stringResource(R.string.local_redeem_grouped_codes))
                    } else {
                        Text(stringResource(R.string.local_redeem_single_codes))
                    }
                }
            }
        }
    }
}

@Composable
fun DataMatrix(
    modifier: Modifier,
    code: LocalRedeemState.DMCode
) {
    val matrix = remember(code) { createBitMatrix(code.payload) }

    DataMatrix(modifier, matrix, code.name)
}

@Requirement(
    "A_19183#1",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "User displays a DMC to redeem a prescription in a pharmacy."
)
@Composable
private fun PageIndicator(
    modifier: Modifier,
    pagerState: PagerState
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
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

        val fraction = pagerState.currentPage + pagerState.currentPageOffset
        val offsetX = with(LocalDensity.current) {
            val gap = PaddingDefaults.Small.roundToPx()
            val size = 8.dp.roundToPx()
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
            .size(8.dp)
    )
}

@Composable
private fun RedeemScannedPrescriptionsDialog(onClickRedeem: () -> Unit, onCancel: () -> Unit) =
    CommonAlertDialog(
        header = stringResource(R.string.redeem_prescriptions_dialog_header),
        info = stringResource(R.string.redeem_prescriptions_dialog_info),
        cancelText = stringResource(R.string.redeem_prescriptions_dialog_cancel),
        actionText = stringResource(R.string.redeem_prescriptions_dialog_redeem),
        onCancel = onCancel,
        onClickAction = onClickRedeem
    )
