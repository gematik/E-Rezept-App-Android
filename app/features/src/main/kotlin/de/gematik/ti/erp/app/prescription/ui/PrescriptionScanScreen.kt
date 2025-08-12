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

package de.gematik.ti.erp.app.prescription.ui

import android.Manifest
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.VibrationEffect.createOneShot
import android.os.VibrationEffect.createWaveform
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Size
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.navigation.navigateAndClearStack
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.prescription.presentation.rememberScanPrescriptionController
import de.gematik.ti.erp.app.prescription.ui.model.ScanData
import de.gematik.ti.erp.app.redeem.navigation.RedeemRoutes
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.AccessToCameraDenied
import de.gematik.ti.erp.app.utils.compose.BottomSheetAction
import de.gematik.ti.erp.app.utils.compose.CameraTopBar
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.compose.annotatedStringBold
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

// TODO: Cleanup to move more logic into the viewmodel to make it more testable
@Requirement(
    "O.Purp_2#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Scanning tasks contains purpose related data input."
)
class PrescriptionScanScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val dialog = LocalDialog.current

        val confirmCancelDialogEvent = ComposableEvent<Unit>()

        val scanPrescriptionController = rememberScanPrescriptionController()
        val scanPrescriptionState by scanPrescriptionController.state
        val overlayState by scanPrescriptionController.overlayState
        val vibrationPattern = scanPrescriptionController.vibration
        val scanner = scanPrescriptionController.scanner
        val processor = scanPrescriptionController.processor

        // stops the camera overlay from scanning data points
        var stopCameraOverlayScan by remember { mutableStateOf(false) }

        confirmCancelDialogEvent.listen {
            dialog.show {
                stopCameraOverlayScan = true
                SaveDialog(
                    onDismissRequest = {
                        stopCameraOverlayScan = false
                        it.dismiss()
                    },
                    onCancel = {
                        it.dismiss()
                        navController.navigate(PrescriptionRoutes.PrescriptionListScreen.path())
                    }
                )
            }
        }

        var camPermissionGranted by rememberSaveable { mutableStateOf(false) }

        val camPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { camPermissionGranted = it }

        @Requirement(
            "O.Plat_3#2",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "platform dialog for CAMERA"
        )
        LaunchedEffect(Unit) {
            if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                camPermissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                camPermissionGranted = true
            }
        }

        var flashEnabled by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
        val coroutineScope = rememberCoroutineScope()

        // conditional back
        val onBack: () -> Unit = {
            if (sheetState.isVisible) {
                coroutineScope.launch { sheetState.hide() }
            }
            if (scanPrescriptionState.hasCodesToSave()) {
                confirmCancelDialogEvent.trigger()
            } else {
                navController.navigateAndClearStack(PrescriptionRoutes.PrescriptionListScreen.route)
            }
        }

        BackHandler { onBack() }

        ModalBottomSheetLayout(
            sheetState = sheetState,
            sheetContent = {
                SheetContent(
                    onClickSave = {
                        scanPrescriptionController.saveToDatabase()
                        navController.navigate(PrescriptionRoutes.PrescriptionListScreen.path())
                    },
                    onClickRedeem = {
                        scanPrescriptionController.saveToDatabase()
                        navController.navigate(RedeemRoutes.HowToRedeemScreen.path())
                    }
                )
            }
        ) {
            if (camPermissionGranted) {
                PrescriptionScanScreenContent(
                    scanner = scanner,
                    processor = processor,
                    flashEnabled = flashEnabled,
                    sheetState = sheetState,
                    stopCameraOverlayScan = stopCameraOverlayScan,
                    overlayState = overlayState,
                    state = scanPrescriptionState,
                    coroutineScope = coroutineScope,
                    onFlashToggled = { flashEnabled = it },
                    onClickClose = { onBack() }
                )
            } else {
                AccessToCameraDenied {
                    navController.navigate(PrescriptionRoutes.PrescriptionListScreen.path())
                }
            }
        }

        HapticAndAudibleFeedback(vibrationPattern)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PrescriptionScanScreenContent(
    scanner: TwoDCodeScanner,
    processor: TwoDCodeProcessor,
    flashEnabled: Boolean,
    onFlashToggled: (Boolean) -> Unit,
    onClickClose: () -> Unit,
    sheetState: ModalBottomSheetState,
    stopCameraOverlayScan: Boolean,
    overlayState: ScanData.OverlayState,
    state: ScanData.State,
    coroutineScope: CoroutineScope
) {
    Box {
        CameraView(
            modifier = Modifier.fillMaxSize(),
            scanner = scanner,
            processor = processor,
            flashEnabled = flashEnabled,
            onFlashToggled = onFlashToggled
        )

        ScanOverlay(
            modifier = Modifier.fillMaxSize(),
            enabled = !sheetState.isVisible && !stopCameraOverlayScan,
            flashEnabled = flashEnabled,
            onFlashClick = onFlashToggled,
            onClickClose = onClickClose,
            overlayState = overlayState
        )

        if (state.snackBar.shouldShow()) {
            ActionBarButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(
                        start = PaddingDefaults.Medium,
                        end = PaddingDefaults.Medium,
                        top = PaddingDefaults.Medium,
                        bottom = PaddingDefaults.XLarge
                    ),
                data = state.snackBar,
                onClick = { coroutineScope.launch { sheetState.show() } }
            )
        }
    }
}

@Composable
private fun SheetContent(
    onClickSave: () -> Unit,
    onClickRedeem: () -> Unit
) {
    SpacerMedium()
    Text(
        stringResource(R.string.cam_next_sheet_how_to_proceed),
        style = AppTheme.typography.body1l,
        modifier = Modifier
            .padding(horizontal = PaddingDefaults.Medium)
    )
    SpacerSmall()
    BottomSheetAction(
        icon = { Icon(Icons.Rounded.ShoppingBag, null) },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.cam_next_sheet_order_now_title))
                SpacerSmall()
            }
        },
        info = { Text(stringResource(R.string.cam_next_sheet_order_now_info)) },
        modifier = Modifier.fillMaxWidth(),
        onClick = onClickRedeem
    )
    BottomSheetAction(
        icon = Icons.Rounded.SaveAlt,
        title = stringResource(R.string.cam_next_sheet_order_later_title),
        info = stringResource(R.string.cam_next_sheet_order_later_info),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClickSave
    )
    Spacer(Modifier.navigationBarsPadding())
}

@Suppress("MagicNumber")
@Composable
private fun HapticAndAudibleFeedback(scanPrescriptionVibration: MutableSharedFlow<ScanData.VibrationPattern>) {
    val context = LocalContext.current

    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_ACCESSIBILITY, 100) }

    LaunchedEffect(scanPrescriptionVibration) {
        scanPrescriptionVibration.collect {
            val vibrator = if (VERSION.SDK_INT >= VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("deprecation")
                context.getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            vibrate(vibrator, it)
            beep(toneGenerator, it)
        }
    }
    DisposableEffect(Unit) {
        onDispose { toneGenerator.release() }
    }
}

@Composable
private fun SaveDialog(
    onDismissRequest: () -> Unit,
    onCancel: () -> Unit
) {
    ErezeptAlertDialog(
        body = stringResource(R.string.cam_cancel_msg),
        okText = stringResource(R.string.cam_cancel_ok),
        dismissText = stringResource(R.string.cam_cancel_resume),
        dismissButtonTestTag = "camera/saveDialog/dismissDialogButton",
        confirmButtonTestTag = "camera/saveDialog/saveButton",
        onConfirmRequest = onCancel,
        onDismissRequest = onDismissRequest
    )
    /* AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Text(
                stringResource(R.string.cam_cancel_msg)
            )
        },
        buttons = {
            TextButton(
                onClick = { onDismissRequest() },
                modifier = Modifier.testTag("camera/saveDialog/dismissDialogButton")
            ) {
                Text(stringResource(R.string.cam_cancel_resume).uppercase(Locale.getDefault()))
            }
            TextButton(onClick = { onCancel() }, modifier = Modifier.testTag("camera/saveDialog/saveButton")) {
                Text(stringResource(R.string.cam_cancel_ok).uppercase(Locale.getDefault()))
            }
        }
    )  */
}

@Suppress("MagicNumber")
private fun beep(toneGenerator: ToneGenerator, pattern: ScanData.VibrationPattern) {
    when (pattern) {
        ScanData.VibrationPattern.Saved -> toneGenerator.startTone(
            ToneGenerator.TONE_PROP_PROMPT,
            1000
        )

        ScanData.VibrationPattern.Error -> toneGenerator.startTone(
            ToneGenerator.TONE_PROP_NACK,
            1000
        )

        else -> {}
    }
}

@Suppress("MagicNumber")
private fun vibrate(vibrator: Vibrator, pattern: ScanData.VibrationPattern) {
    if (pattern == ScanData.VibrationPattern.None) {
        return
    }

    val canVibrate: Boolean = vibrator.hasVibrator()
    if (canVibrate) {
        vibrator.vibrate(
            when (pattern) {
                ScanData.VibrationPattern.Focused -> createOneShot(100L, 100)
                ScanData.VibrationPattern.Saved -> createOneShot(300L, 100)
                ScanData.VibrationPattern.Error -> createWaveform(longArrayOf(100, 100, 300), intArrayOf(100, 0, 100), -1)
                ScanData.VibrationPattern.None -> error("Should not be reached")
            }
        )
    }
}

@Composable
private fun ActionBarButton(
    data: ScanData.ActionBar,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) =
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            contentColor = AppTheme.colors.primary700,
            backgroundColor = AppTheme.colors.neutral100
        ),
        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .heightIn(min = 48.dp)
            .testTag("camera/nextButton")
    ) {
        Text(
            annotatedPluralsResource(
                R.plurals.cam_next_with,
                data.totalNrOfPrescriptions,
                AnnotatedString(data.totalNrOfPrescriptions.toString())
            )
        )
    }

@Composable
private fun InfoCard(
    info: ScanData.Info,
    modifier: Modifier
) =
    Card(
        backgroundColor = Color.Black.copy(alpha = 0.6f),
        contentColor = Color.White,
        shape = RoundedCornerShape(8.dp),
        elevation = 0.dp,
        modifier = modifier.padding(horizontal = PaddingDefaults.Large)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.Tiny)
        ) {
            val view = LocalView.current

            val scanning = stringResource(R.string.cam_info_scanning)
            val invalid = stringResource(R.string.cam_info_invalid)
            val duplicated = stringResource(R.string.cam_info_duplicated)
            val detected = if (info is ScanData.Info.Scanned) {
                annotatedPluralsResource(
                    R.plurals.cam_info_detected,
                    info.nr,
                    annotatedStringBold(info.nr.toString())
                )
            } else {
                buildAnnotatedString { }
            }

            when (info) {
                ScanData.Info.Focus -> Text(
                    scanning,
                    textAlign = TextAlign.Center,
                    style = AppTheme.typography.subtitle1
                )

                ScanData.Info.ErrorNotValid -> InfoError(invalid)
                ScanData.Info.ErrorDuplicated -> InfoError(duplicated)
                is ScanData.Info.Scanned -> Text(
                    detected,
                    textAlign = TextAlign.Center,
                    style = AppTheme.typography.subtitle1
                )
            }

            val acc = when (info) {
                ScanData.Info.Focus -> scanning
                ScanData.Info.ErrorNotValid -> invalid
                ScanData.Info.ErrorDuplicated -> duplicated
                is ScanData.Info.Scanned -> detected.text
            }

            DisposableEffect(view, acc) {
                view.announceForAccessibility(acc)

                onDispose {}
            }
        }
    }

@Composable
private fun InfoError(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Close, null, modifier = Modifier.size(24.dp))
        SpacerTiny()
        Text(
            text,
            textAlign = TextAlign.Center,
            style = AppTheme.typography.subtitle1
        )
    }
}

@Suppress("UnsafeOptInUsageError", "MagicNumber")
@Composable
private fun CameraView(
    scanner: TwoDCodeScanner,
    processor: TwoDCodeProcessor,
    modifier: Modifier,
    flashEnabled: Boolean,
    onFlashToggled: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val camPreviewView = remember { PreviewView(context) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    LaunchedEffect(camera, flashEnabled) {
        withContext(Dispatchers.Main) {
            camera?.apply {
                cameraControl.enableTorch(flashEnabled)
                lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    onFlashToggled(cameraInfo.torchState.value == TorchState.ON)
                }
            }
        }
    }

    DisposableEffect(camPreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                val cameraSelector =
                    CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1024, 768))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply {
                        setAnalyzer(Executors.newSingleThreadExecutor(), scanner)
                    }

                preview.setSurfaceProvider(camPreviewView.surfaceProvider)

                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            },
            ContextCompat.getMainExecutor(context)
        )

        onDispose {
            cameraProviderFuture.cancel(true)
        }
    }

    AnimatedVisibility(
        modifier = Modifier.background(Color.Black),
        visibleState = remember { MutableTransitionState(false) }.apply { targetState = true },
        exit = fadeOut(),
        enter = fadeIn(animationSpec = tween(800, 200))
    ) {
        AndroidView(
            {
                camPreviewView.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }

                camPreviewView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                    processor.onLayoutChange(
                        Size(
                            camPreviewView.width,
                            camPreviewView.height
                        )
                    )
                }

                camPreviewView
            },
            modifier = modifier
        )
    }
}

@Suppress("ComplexMethod", "MagicNumber")
@Composable
private fun ScanOverlay(
    enabled: Boolean,
    flashEnabled: Boolean,
    onFlashClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onClickClose: () -> Unit,
    overlayState: ScanData.OverlayState
) {
    var points by remember { mutableStateOf(FloatArray(8)) }

    LaunchedEffect(enabled) {
        if (enabled) {
            overlayState.area?.let {
                points = it
            }
        }
    }

    val fillColor =
        when (overlayState.state) {
            ScanData.ScanState.Hold -> AppTheme.colors.scanOverlayHoldFill
            ScanData.ScanState.Save -> AppTheme.colors.scanOverlaySavedFill
            ScanData.ScanState.Error -> AppTheme.colors.scanOverlayErrorFill
            ScanData.ScanState.Final -> AppTheme.colors.scanOverlayHoldFill
        }

    val outlineColor =
        when (overlayState.state) {
            ScanData.ScanState.Hold -> AppTheme.colors.scanOverlayHoldOutline
            ScanData.ScanState.Save -> AppTheme.colors.scanOverlaySavedOutline
            ScanData.ScanState.Error -> AppTheme.colors.scanOverlayErrorOutline
            ScanData.ScanState.Final -> AppTheme.colors.scanOverlayHoldOutline
        }

    val showAimAid = overlayState.area == null && enabled

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (overlayState.area != null) {
                val p = Path().apply {
                    moveTo(points[0], points[1])
                    lineTo(points[2], points[3])
                    lineTo(points[4], points[5])
                    lineTo(points[6], points[7])
                    close()
                }

                // fill
                drawPath(p, SolidColor(fillColor))

                // outline
                drawPath(
                    p,
                    SolidColor(outlineColor),
                    style = Stroke(
                        width = 4.dp.toPx(),
                        pathEffect = PathEffect.cornerPathEffect(4.dp.toPx())
                    )
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            CameraTopBar(
                flashEnabled = flashEnabled,
                onClickClose = onClickClose,
                onFlashClick = onFlashClick
            )
            Spacer(modifier = Modifier.size(24.dp))
            InfoCard(
                overlayState.info,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            if (showAimAid) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val infTransition = rememberInfiniteTransition()
                    val scale by infTransition.animateFloat(
                        label = "",
                        initialValue = 1.0f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Icon(
                        Icons.Rounded.CropFree,
                        null,
                        tint = AppTheme.colors.yellow500,
                        modifier = Modifier
                            .size(48.dp)
                            .scale(scale)
                            .align(
                                Alignment { size, space, _ ->
                                    IntOffset(
                                        space.width / 2 - size.width / 2,
                                        space.height / 3 - size.height / 2
                                    )
                                }
                            )
                    )
                }
            }
        }
    }
}
