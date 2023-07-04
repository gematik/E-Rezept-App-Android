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

package de.gematik.ti.erp.app.redeem.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.datamatrix.DataMatrixWriter
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyOrderState
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.TertiaryButton
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

@Requirement(
    "A_20181",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Only the title of the prescription and the DMC consisting of Task-ID and Access-Code are displayed."
)
@OptIn(ExperimentalPagerApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun LocalRedeemScreen(
    onBack: () -> Unit,
    onFinished: () -> Unit,
    orderState: PharmacyOrderState
) {
    val localRedeemState = rememberLocalRedeemState(orderState)
    val codes by localRedeemState.codes

    var isZoomedOut by remember { mutableStateOf(true) }

    if (codes.isEmpty()) {
        return
    }

    val activity = LocalActivity.current
    DisposableEffect(Unit) {
        activity.window?.attributes = activity.window?.attributes?.apply {
            screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        }
        onDispose {
            activity.window?.attributes = activity.window?.attributes?.apply {
                screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
        }
    }

    val redeemController = rememberRedeemController()
    var showRedeemScannedDialog by remember { mutableStateOf(false) }
    if (showRedeemScannedDialog) {
        val scope = rememberCoroutineScope()
        val prescriptions by orderState.prescriptions
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
                    code = codes[page],
                    isZoomedOut = isZoomedOut,
                    onClickZoom = {
                        isZoomedOut = it
                    }
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

@Requirement(
    "A_19183#1",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "User displays a DMC to redeem a prescription in a pharmacy."
)
@Composable
private fun DataMatrix(
    modifier: Modifier,
    code: LocalRedeemState.DMCode,
    isZoomedOut: Boolean,
    onClickZoom: (Boolean) -> Unit
) {
    val matrix = remember(code) { createBitMatrix(code.payload) }
    val shape = RoundedCornerShape(16.dp)

    AppTheme(darkTheme = false) {
        Column(
            modifier = modifier
                .background(AppTheme.colors.neutral000, shape)
                .border(1.dp, AppTheme.colors.neutral300, shape)
                .padding(PaddingDefaults.Medium)
        ) {
            @Suppress("MagicNumber")
            Box(
                modifier = Modifier
                    .scale(scale = if (isZoomedOut) 0.7f else 1f)
                    .drawDataMatrix(matrix)
                    .aspectRatio(1f)
                    .fillMaxWidth()
            )
            SpacerMedium()
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (code.name != null) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = code.name,
                        style = AppTheme.typography.h6,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = AppTheme.colors.neutral999
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                SpacerMedium()
                TertiaryButton(
                    onClick = { onClickZoom(!isZoomedOut) }
                ) {
                    if (isZoomedOut) {
                        Icon(Icons.Rounded.ZoomIn, null)
                    } else {
                        Icon(Icons.Rounded.ZoomOut, null)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
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

private const val BitmapMinSize = 10

fun Modifier.drawDataMatrix(matrix: BitMatrix) =
    drawWithCache {
        val bmp = Bitmap.createScaledBitmap(
            matrix.toBitmap(),
            max(size.width.roundToInt(), BitmapMinSize),
            max(size.height.roundToInt(), BitmapMinSize),
            false
        )

        onDrawBehind {
            drawImage(bmp.asImageBitmap())
        }
    }

fun createBitMatrix(data: String): BitMatrix =
    // width & height is unused in the underlying implementation
    DataMatrixWriter().encode(data, BarcodeFormat.DATA_MATRIX, 1, 1)

fun BitMatrix.toBitmap(): Bitmap {
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bmp.setPixel(
                x,
                y,
                if (get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            )
        }
    }
    return bmp
}
