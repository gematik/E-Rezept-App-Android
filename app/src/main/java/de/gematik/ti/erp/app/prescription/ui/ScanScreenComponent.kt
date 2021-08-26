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

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect.createOneShot
import android.os.VibrationEffect.createWaveform
import android.os.Vibrator
import android.util.Size
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
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
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.prescription.ui.model.ScanScreen
import de.gematik.ti.erp.app.theme.AppColorsThemeLight
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.compose.annotatedStringBold
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.testId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.Executors

@Composable
fun ScanScreen(
    mainNavController: NavController,
    scanViewModel: ScanPrescriptionViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var shouldShowEduDialog by rememberSaveable { mutableStateOf(false) }
    var eduDialogAccepted by rememberSaveable { mutableStateOf(false) }
    var camPermissionGranted by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        shouldShowEduDialog =
            context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        if (!shouldShowEduDialog) {
            eduDialogAccepted = true
        }
    }

    val camPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            camPermissionGranted = it
        }

    if (shouldShowEduDialog && !eduDialogAccepted) {
        EducationalDialog {
            eduDialogAccepted = true
        }
    }

    LaunchedEffect(eduDialogAccepted) {
        if (eduDialogAccepted) {
            camPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var flashEnabled by remember { mutableStateOf(false) }

    val state by scanViewModel.screenState().collectAsState(ScanScreen.defaultScreenState)

    var cancelRequested by remember { mutableStateOf(false) }
    BackHandler(!cancelRequested && state.hasCodesToSave()) {
        cancelRequested = true
    }

    if (cancelRequested && state.hasCodesToSave()) {
        SaveDialog(
            onDismissRequest = { cancelRequested = false },
            onCancel = { mainNavController.popBackStack() }
        )
    }

    Box {
        if (camPermissionGranted) {
            CameraView(
                scanViewModel,
                Modifier.fillMaxSize(),
                flashEnabled = flashEnabled,
                onFlashToggled = {
                    flashEnabled = it
                }
            )
        } else {
            Surface(
                color = Color.Black,
                contentColor = Color.White,
                modifier = Modifier.fillMaxSize()
            ) {
                ConstraintLayout(
                    modifier = Modifier
                        .padding(16.dp)
                        .testId("camera/disallowed")
                ) {
                    val (icon, header, info) = createRefs()

                    createVerticalChain(icon, header, info, chainStyle = ChainStyle.Packed(0.5f))

                    Icon(
                        Icons.Rounded.ErrorOutline, null,
                        modifier = Modifier
                            .size(48.dp)
                            .constrainAs(icon) {
                                centerHorizontallyTo(parent)
                            }
                    )
                    Text(
                        stringResource(R.string.cam_access_denied_headline),
                        style = MaterialTheme.typography.h6,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.constrainAs(header) {
                            centerHorizontallyTo(parent)
                            top.linkTo(icon.bottom, 16.dp)
                        }
                    )
                    Text(
                        stringResource(R.string.cam_access_denied_description),
                        style = MaterialTheme.typography.subtitle1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.constrainAs(info) {
                            centerHorizontallyTo(parent)
                            top.linkTo(header.bottom, 8.dp)
                        }
                    )
                }
            }
        }
        ScanOverlay(
            flashEnabled = flashEnabled,
            onFlashClick = { flashEnabled = it },
            modifier = Modifier.fillMaxSize()
        )

        Column {
            Spacer(modifier = Modifier.weight(1.0f))
            if (state.snackBar.shouldShow()) {
                SnackBar(
                    state.snackBar,
                    onSaveClick = {
                        scanViewModel.saveToDatabase()
                        mainNavController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    HapticAndAudibleFeedback()
}

@Composable
private fun HapticAndAudibleFeedback(scanVM: ScanPrescriptionViewModel = viewModel()) {
    val context = LocalContext.current

    val toneGenerator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ToneGenerator(AudioManager.STREAM_ACCESSIBILITY, 100)
        } else {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        }
    }

    LaunchedEffect(scanVM.vibration) {
        scanVM.vibration.collect {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrate(vibrator, it)
            beep(toneGenerator, it)
        }
    }
    DisposableEffect(Unit) {
        onDispose { toneGenerator.release() }
    }
}

@Composable
private fun EducationalDialog(
    onContinue: () -> Unit,
) {
    var dialogOpen by remember { mutableStateOf(true) }

    if (dialogOpen) {
        CommonAlertDialog(
            header = stringResource(R.string.cam_edu_headline),
            info = stringResource(R.string.cam_edu_description),
            cancelText = stringResource(R.string.cancel),
            actionText = stringResource(R.string.cam_edu_accept),
            onCancel = { dialogOpen = false },
            onClickAction = onContinue
        )
    }
}

@Composable
private fun SaveDialog(
    onDismissRequest: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Text(
                stringResource(R.string.cam_cancel_msg),
                style = MaterialTheme.typography.body1
            )
        },
        buttons = {
            Row(Modifier.padding(bottom = 12.dp, start = 12.dp, end = 12.dp)) {
                Spacer(modifier = Modifier.weight(1.0f))
                TextButton(
                    onClick = { onDismissRequest() },
                    modifier = Modifier.testId("camera/saveDialog/dismissDialogButton")
                ) {
                    Text(stringResource(R.string.cam_cancel_resume).uppercase(Locale.getDefault()))
                }
                TextButton(onClick = { onCancel() }, modifier = Modifier.testId("camera/saveDialog/saveButton")) {
                Text(stringResource(R.string.cam_cancel_ok).uppercase(Locale.getDefault()))
            }
            }
        }
    )
}

private fun beep(toneGenerator: ToneGenerator, pattern: ScanScreen.VibrationPattern) {
    when (pattern) {
        ScanScreen.VibrationPattern.Saved -> toneGenerator.startTone(
            ToneGenerator.TONE_PROP_PROMPT,
            1000
        )
        ScanScreen.VibrationPattern.Error -> toneGenerator.startTone(
            ToneGenerator.TONE_PROP_NACK,
            1000
        )
    }
}

private fun vibrate(vibrator: Vibrator, pattern: ScanScreen.VibrationPattern) {
    if (pattern == ScanScreen.VibrationPattern.None) {
        return
    }

    val canVibrate: Boolean = vibrator.hasVibrator()
    if (canVibrate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                when (pattern) {
                    ScanScreen.VibrationPattern.Focused ->
                        createOneShot(100L, 100)
                    ScanScreen.VibrationPattern.Saved ->
                        createOneShot(300L, 100)
                    ScanScreen.VibrationPattern.Error ->
                        createWaveform(
                            longArrayOf(100, 100, 300),
                            intArrayOf(
                                100,
                                0,
                                100
                            ),
                            -1
                        )
                    ScanScreen.VibrationPattern.None -> error("Should not be reached")
                }
            )
        } else {
            @Suppress("DEPRECATION")
            when (pattern) {
                ScanScreen.VibrationPattern.Focused ->
                    vibrator.vibrate(longArrayOf(0L, 100L), -1)
                ScanScreen.VibrationPattern.Saved ->
                    vibrator.vibrate(longArrayOf(0L, 300L), -1)
                ScanScreen.VibrationPattern.Error ->
                    vibrator.vibrate(longArrayOf(0L, 100L, 100L, 300L), -1)
                ScanScreen.VibrationPattern.None -> error("Should not be reached")
            }
        }
    }
}

@Composable
private fun SnackBar(
    data: ScanScreen.SnackBar,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) =
    Card(
        backgroundColor = Color.Black.copy(alpha = 0.6f),
        contentColor = Color.White,
        shape = RoundedCornerShape(8.dp),
        elevation = 0.dp,
        modifier = modifier.padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val codeTxt =
                annotatedPluralsResource(R.plurals.txt_recipe, data.totalNrOfCodes)
            val prescriptionTxt =
                annotatedPluralsResource(R.plurals.txt_prescription, data.totalNrOfPrescriptions)

            Text(
                annotatedStringResource(
                    R.string.txt_number_of_prescriptions_available,
                    annotatedStringBold(data.totalNrOfPrescriptions.toString()),
                    prescriptionTxt,
                    annotatedStringBold(data.totalNrOfCodes.toString()),
                    codeTxt
                ),
                style = MaterialTheme.typography.subtitle1
            )
            Spacer16()
            TextButton(
                onClick = onSaveClick,
                colors = ButtonDefaults.textButtonColors(contentColor = AppColorsThemeLight.primary300),
                modifier = Modifier
                    .align(Alignment.End)
                    .testId("camera/saveButton")
            ) {
                Text(
                    annotatedPluralsResource(
                        R.plurals.txt_btn_add_prescriptions,
                        data.totalNrOfPrescriptions,
                        buildAnnotatedString { data.totalNrOfPrescriptions.toString() },
                    ).toUpperCase()
                )
            }
        }
    }

@Composable
private fun InfoCard(
    info: ScanScreen.Info,
    modifier: Modifier,
) =
    Card(
        backgroundColor = Color.Black.copy(alpha = 0.6f),
        contentColor = Color.White,
        shape = RoundedCornerShape(8.dp),
        elevation = 0.dp,
        modifier = modifier.padding(start = 24.dp, end = 24.dp)
    ) {
        Box(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
        ) {
            val view = LocalView.current

            val scanning = stringResource(R.string.cam_info_scanning)
            val invalid = stringResource(R.string.cam_info_invalid)
            val duplicated = stringResource(R.string.cam_info_duplicated)
            val detected = if (info is ScanScreen.Info.Scanned) {
                annotatedPluralsResource(
                    R.plurals.cam_info_detected,
                    info.nr,
                    annotatedStringBold(info.nr.toString())
                )
            } else {
                buildAnnotatedString { }
            }

            when (info) {
                ScanScreen.Info.Focus -> Text(
                    scanning,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.subtitle1
                )
                ScanScreen.Info.ErrorNotValid -> InfoError(invalid)
                ScanScreen.Info.ErrorDuplicated -> InfoError(duplicated)
                is ScanScreen.Info.Scanned -> Text(
                    detected,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.subtitle1
                )
            }

            val acc = when (info) {
                ScanScreen.Info.Focus -> scanning
                ScanScreen.Info.ErrorNotValid -> invalid
                ScanScreen.Info.ErrorDuplicated -> duplicated
                is ScanScreen.Info.Scanned -> detected.text
            }

            DisposableEffect(view, acc) {
                view.announceForAccessibility(acc)

                onDispose {}
            }
        }
    }

@Composable
private fun InfoError(text: String) {
    Row {
        Icon(Icons.Rounded.Close, null, modifier = Modifier.size(24.dp))
        Spacer4()
        Text(
            text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle1
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun CameraView(
    scanVM: ScanPrescriptionViewModel = viewModel(),
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
                if (cameraInfo.torchState.value == TorchState.ON != flashEnabled) {
                    cameraControl.enableTorch(flashEnabled)
                }
            }
        }
    }

    SideEffect {
        camera?.apply {
            if (cameraInfo.torchState.value == TorchState.ON != flashEnabled) {
                onFlashToggled(flashEnabled)
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
                        setAnalyzer(Executors.newSingleThreadExecutor(), scanVM.scanner)
                    }

                preview.setSurfaceProvider(camPreviewView.surfaceProvider)

                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalysis
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
                    scanVM.processor.onLayoutChange(
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

@Composable
private fun TopBar(
    flashEnabled: Boolean,
    onFlashClick: (Boolean) -> Unit,
) {
    val backPressDispatcher = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher

    Surface(
        color = Color.Unspecified,
        contentColor = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            val accCancel = stringResource(R.string.cam_acc_cancel)
            val accTorch = stringResource(R.string.cam_acc_torch)

            IconButton(
                onClick = { backPressDispatcher.onBackPressed() },
                modifier = Modifier
                    .testId("camera/closeButton")
                    .semantics { contentDescription = accCancel }
            ) {
                Icon(Icons.Rounded.Close, null, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            IconToggleButton(
                checked = flashEnabled,
                onCheckedChange = onFlashClick,
                modifier = Modifier
                    .testId("camera/flashToggle")
                    .semantics { contentDescription = accTorch }
            ) {
                val ic = if (flashEnabled) {
                    Icons.Rounded.FlashOn
                } else {
                    Icons.Rounded.FlashOff
                }
                Icon(ic, null, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun ScanOverlay(
    flashEnabled: Boolean,
    onFlashClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    scanVM: ScanPrescriptionViewModel = viewModel(),
) {
    var points by remember { mutableStateOf(FloatArray(8)) }

    val state by produceState(
        ScanScreen.defaultOverlayState
    ) {
        scanVM.scanOverlayState().collect {
            value = it
            it.area?.let {
                points = it
            }
        }
    }

    val fillColor =
        when (state.state) {
            ScanScreen.ScanState.Hold -> AppTheme.colors.scanOverlayHoldFill
            ScanScreen.ScanState.Save -> AppTheme.colors.scanOverlaySavedFill
            ScanScreen.ScanState.Error -> AppTheme.colors.scanOverlayErrorFill
            ScanScreen.ScanState.Final -> AppTheme.colors.scanOverlayHoldFill
        }

    val outlineColor =
        when (state.state) {
            ScanScreen.ScanState.Hold -> AppTheme.colors.scanOverlayHoldOutline
            ScanScreen.ScanState.Save -> AppTheme.colors.scanOverlaySavedOutline
            ScanScreen.ScanState.Error -> AppTheme.colors.scanOverlayErrorOutline
            ScanScreen.ScanState.Final -> AppTheme.colors.scanOverlayHoldOutline
        }

    val showAimAid = state.area == null

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (state.area != null) {
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
                    ),
                )
            }
        }
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                flashEnabled = flashEnabled,
                onFlashClick = onFlashClick
            )
            Spacer(modifier = Modifier.size(24.dp))
            InfoCard(
                state.info,
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
