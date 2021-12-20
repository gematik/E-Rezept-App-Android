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

package de.gematik.ti.erp.app.redeem.ui

import android.graphics.Bitmap
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import de.gematik.ti.erp.app.utils.compose.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.zxing.common.BitMatrix
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.BackInterceptor
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.NavigationClose
import de.gematik.ti.erp.app.utils.compose.Spacer8
import de.gematik.ti.erp.app.utils.compose.annotatedStringBold
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RedeemScreen(taskIds: List<String>, navController: NavController, redeemVM: RedeemViewModel = hiltViewModel()) {
    val protocolText = stringResource(R.string.redeem_protocol_text)

    val state by produceState(redeemVM.defaultState) {
        redeemVM.screenState(taskIds).collect {
            value = it
        }
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

    val swipeableState = rememberSwipeableState(initialValue = 0)

    var pageSize by remember { mutableStateOf(IntSize(1, 1)) }
    val pageWidth = pageSize.width.toFloat()

    val anchors =
        (state.codes.indices).map {
            pageWidth * it.toFloat() to it
        }.toMap()

    var showRedeemScannedDialog by remember { mutableStateOf(false) }
    var showRedeemSyncedDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    if (state.codes.firstOrNull()?.isScanned == true) {
        BackInterceptor {
            showRedeemScannedDialog = true
        }
    } else {
        BackInterceptor {
            showRedeemSyncedDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = Color.Unspecified,
                title = {
                    Text(stringResource(R.string.redeem_title))
                },
                navigationIcon = {
                    NavigationClose(
                        onClick = {
                            if (state.codes.firstOrNull()?.isScanned == true) {
                                showRedeemScannedDialog = true
                            } else {
                                showRedeemSyncedDialog = true
                            }
                        }
                    )
                },
                elevation = 0.dp
            )
        }
    ) {

        if (showRedeemScannedDialog) {
            RedeemScannedPrescriptionsDialog(
                onClickRedeem = {
                    redeemVM.redeemPrescriptions(taskIds, protocolText)
                    navController.popBackStack()
                }
            ) {
                navController.popBackStack()
            }
        }

        if (showRedeemSyncedDialog) {
            RedeemSyncedPrescriptionsDialog {
                navController.popBackStack()
            }
        }

        if (state.codes.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(R.string.redeem_subtitle),
                    style = MaterialTheme.typography.subtitle2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .swipeable(
                            state = swipeableState,
                            anchors = anchors,
                            orientation = Orientation.Horizontal,
                            reverseDirection = true
                        )
                        .onSizeChanged {
                            pageSize = it
                        }
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .offset {
                                IntOffset(-(swipeableState.offset.value).roundToInt(), 0)
                            }
                            .wrapContentWidth(align = Alignment.Start, unbounded = true)
                    ) {
                        with(LocalDensity.current) {
                            val mod = Modifier.width(pageSize.width.toDp())
                            state.codes.forEach { code ->
                                Column {
                                    Box {
                                        DataMatrixCode(
                                            matrixCode = code.matrixCode,
                                            modifier = mod.aspectRatio(1f)
                                        )
                                    }
                                    Spacer8()
                                    Box(
                                        modifier = mod
                                            .align(Alignment.CenterHorizontally)
                                            .padding(bottom = 16.dp)
                                    ) {
                                        if (!state.showSingleCodes && code.nrOfCodes > 1) {
                                            Text(
                                                annotatedStringResource(
                                                    R.string.redeem_txt_code_description,
                                                    annotatedStringBold(code.nrOfCodes.toString())
                                                ),
                                                style = MaterialTheme.typography.body2,
                                                textAlign = TextAlign.Center,
                                                modifier = mod
                                            )
                                        }
                                    }
                                    if (code.nrOfCodes > 1) {
                                        Box(modifier = mod.align(Alignment.CenterHorizontally)) {
                                            SwitchScreenMode(
                                                mod,
                                                stringResource(R.string.redeem_show_single_codes),
                                                Icons.Rounded.ViewCarousel
                                            ) {
                                                redeemVM.onShowSingleCodes(true)
                                            }
                                        }
                                    } else if (taskIds.size > 1) {
                                        Box(modifier = mod.align(Alignment.CenterHorizontally)) {
                                            SwitchScreenMode(
                                                mod,
                                                stringResource(R.string.redeem_show_collective_codes),
                                                Icons.Rounded.QrCode
                                            ) {
                                                redeemVM.onShowSingleCodes(false)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (state.codes.size > 1) {
                    Counter(
                        page = swipeableState.currentValue,
                        maxPages = state.codes.size,
                        modifier = Modifier
                            .padding(top = 40.dp)
                            .fillMaxWidth(),
                        {
                            coroutineScope.launch {
                                swipeableState.animateTo(swipeableState.currentValue - 1)
                            }
                        },
                        {
                            coroutineScope.launch {
                                swipeableState.animateTo(swipeableState.currentValue + 1)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchScreenMode(
    modifier: Modifier,
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        TextButton(
            onClick = {
                onClick()
            },
            modifier = modifier
                .layoutId("view_single_codes")
                .align(Alignment.Center),
            contentPadding = PaddingValues()
        ) {

            Text(
                text,
                color = MaterialTheme.colors.secondary,
                style = MaterialTheme.typography.subtitle2,
            )
            Icon(
                icon,
                null,
                tint = MaterialTheme.colors.secondary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun Counter(
    page: Int,
    maxPages: Int,
    modifier: Modifier,
    onClickPrevious: () -> Unit,
    onClickNext: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier.size(48.dp)
        ) {
            if (page > 0) {
                IconButton(
                    onClick = onClickPrevious,
                    modifier = Modifier.background(
                        color = AppTheme.colors.neutral200,
                        shape = RoundedCornerShape(24.dp)
                    )
                ) {
                    Icon(Icons.Rounded.ArrowBack, null, tint = AppTheme.colors.primary600)
                }
            }
        }

        Box(
            modifier = Modifier
                .background(
                    color = AppTheme.colors.neutral200,
                    shape = RoundedCornerShape(16.dp)
                )
                .align(Alignment.CenterVertically)
        ) {
            Text(
                annotatedStringResource(
                    R.string.redeem_counter_text,
                    annotatedStringBold((page + 1).toString()),
                    annotatedStringBold(maxPages.toString()),
                ),
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .align(Alignment.Center)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(48.dp)
        ) {
            if (page + 1 < maxPages) {
                IconButton(
                    onClick = onClickNext,
                    modifier = Modifier.background(
                        color = AppTheme.colors.neutral200,
                        shape = RoundedCornerShape(24.dp)
                    )
                ) {
                    Icon(Icons.Rounded.ArrowForward, null, tint = AppTheme.colors.primary600)
                }
            }
        }
    }
}

@Composable
fun DataMatrixCode(matrixCode: BitMatrixCode, modifier: Modifier) {
    Box(
        modifier = Modifier
            .then(modifier)
            .background(Color.White)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val bmp = Bitmap.createScaledBitmap(
                        matrixCode.matrix.toBitmap(),
                        max(size.width.roundToInt(), 10),
                        max(size.height.roundToInt(), 10),
                        false
                    )

                    onDrawBehind {
                        drawImage(bmp.asImageBitmap())
                    }
                }
        )
    }
}

@Composable
private fun RedeemScannedPrescriptionsDialog(onClickRedeem: () -> Unit, onCancel: () -> Unit) {

    CommonAlertDialog(
        header = stringResource(R.string.redeem_prescriptions_dialog_header),
        info = stringResource(R.string.redeem_prescriptions_dialog_info),
        cancelText = stringResource(R.string.redeem_prescriptions_dialog_cancel),
        actionText = stringResource(R.string.redeem_prescriptions_dialog_redeem),
        onCancel = { onCancel() }
    ) {
        onClickRedeem()
    }
}

@Composable
private fun RedeemSyncedPrescriptionsDialog(onClick: () -> Unit) {

    AcceptDialog(
        header = stringResource(R.string.redeem_synced_prescriptions_dialog_header),
        info = stringResource(R.string.redeem_synced_prescriptions_dialog_info),
        acceptText = stringResource(R.string.redeem_synced_prescriptions_dialog_ok),
        onClickAccept = onClick
    )
}

private fun BitMatrix.toBitmap(): Bitmap {
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
