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

package de.gematik.ti.erp.app.cardwall.ui.screens

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlashlightOff
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallScreen
import de.gematik.ti.erp.app.cardwall.presentation.CardWallScannerController
import de.gematik.ti.erp.app.cardwall.presentation.CardWallSharedViewModel
import de.gematik.ti.erp.app.cardwall.presentation.rememberScannerController
import de.gematik.ti.erp.app.cardwall.ui.model.ScannerCallbacks
import de.gematik.ti.erp.app.cardwall.ui.model.ScannerState
import de.gematik.ti.erp.app.cardwall.ui.preview.CardScannerScreenParameter
import de.gematik.ti.erp.app.cardwall.ui.preview.CardScannerScreenParameterProvider
import de.gematik.ti.erp.app.core.LifecycleEventObserver
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.mlkitscanner.usecase.CameraSetupParams
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.AccessToCameraDenied
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.OutlinedIconButton
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import java.util.concurrent.Executors

private object ScannerConstants {
    const val OVERLAY_ALPHA = 0.6f
    const val ANIMATION_DURATION_MS = 1000
}

class CardWallScannerScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val sharedViewModel: CardWallSharedViewModel
) : CardWallScreen() {

    @Composable
    override fun Content() {
        val scannerController = rememberScannerController()
        val hasPermission = setupCameraPermissions(scannerController)

        if (hasPermission) {
            ScannerContent(
                cardWallScannerController = scannerController,
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        } else {
            AccessToCameraDenied(
                showSettingsButton = true
            ) {
                navController.popBackStack()
            }
        }
    }

    @Composable
    private fun setupCameraPermissions(scannerController: CardWallScannerController): Boolean {
        val context = LocalContext.current
        val hasPermission by scannerController.hasPermission.collectAsStateWithLifecycle()

        LaunchedEffect(scannerController) {
            scannerController.checkCameraPermission(context)
        }

        LifecycleEventObserver { event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scannerController.checkCameraPermission(context)
            }
        }

        val camPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            scannerController.checkCameraPermission(context)
        }

        LaunchedEffect(hasPermission) {
            if (!hasPermission) {
                camPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        return hasPermission
    }
}

@Composable
private fun ScannerContent(
    cardWallScannerController: CardWallScannerController,
    navController: NavController,
    sharedViewModel: CardWallSharedViewModel
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val detectedCan by cardWallScannerController.detectedCan.collectAsStateWithLifecycle()
    val isScanning by cardWallScannerController.isScanning.collectAsStateWithLifecycle()
    var isFlashlightOn by remember { mutableStateOf(false) }

    LaunchedEffect(detectedCan) {
        detectedCan?.let {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val debugHealthCardReference = remember {
        context.getString(R.string.health_card_reference_debug)
    }

    val releaseHealthCardReference = remember {
        context.getString(R.string.health_card_reference_release)
    }

    ScannerEventHandlers(
        navController = navController,
        sharedViewModel = sharedViewModel,
        cardWallScannerController = cardWallScannerController
    ) { handleCanSelection, handleClose ->

        ScannerScreenContent(
            state = ScannerState(
                detectedCan = detectedCan,
                isScanning = isScanning,
                isFlashlightOn = isFlashlightOn
            ),
            callbacks = ScannerCallbacks(
                onSetupCamera = cardWallScannerController::setupCamera,
                onToggleFlashlight = cardWallScannerController::toggleFlashlight,
                onImageAnalyzed = { imageProxy, previewView, density, screenWidth, screenHeight ->
                    cardWallScannerController.processImageFrame(
                        imageProxy,
                        previewView,
                        density,
                        screenWidth,
                        screenHeight,
                        debugHealthCardReference,
                        releaseHealthCardReference
                    )
                },
                onCanSelected = handleCanSelection,
                onClose = handleClose,
                onFlashlightToggle = { isFlashlightOn = !isFlashlightOn }
            )
        )
    }
}

@Composable
private fun ScannerEventHandlers(
    navController: NavController,
    sharedViewModel: CardWallSharedViewModel,
    cardWallScannerController: CardWallScannerController,
    content: @Composable (
        handleCanSelection: (String) -> Unit,
        handleClose: () -> Unit
    ) -> Unit
) {
    val handleCanSelection: (String) -> Unit =
        remember(navController, sharedViewModel, cardWallScannerController) {
            {
                    can ->
                val previousRoute = navController.previousBackStackEntry?.destination?.route
                cardWallScannerController.reset()

                when (previousRoute) {
                    CardUnlockRoutes.CardUnlockCanScreen.route -> {
                        navController.navigate(CardUnlockRoutes.CardUnlockCanScreen.path(scannedCan = can))
                    }

                    else -> {
                        sharedViewModel.setScannedCan(can)
                        navController.popBackStack()
                    }
                }
            }
        }

    val handleClose: () -> Unit = remember(navController, cardWallScannerController) {
        {
            cardWallScannerController.reset()
            navController.popBackStack()
        }
    }

    BackHandler(onBack = handleClose)

    content(handleCanSelection, handleClose)
}

@Composable
fun ScannerScreenContent(
    state: ScannerState,
    callbacks: ScannerCallbacks,
    isPreview: Boolean = false
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onSetupCamera = callbacks.onSetupCamera,
            onToggleFlashlight = callbacks.onToggleFlashlight,
            onImageAnalyzed = callbacks.onImageAnalyzed,
            isPaused = !state.isScanning,
            isFlashlightOn = state.isFlashlightOn,
            isPreview = isPreview
        )

        ScanningOverlay(
            modifier = Modifier.fillMaxSize(),
            detectedCan = state.detectedCan
        )

        TopControlIcons(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            isFlashlightOn = state.isFlashlightOn,
            onCloseClicked = callbacks.onClose,
            onFlashlightToggle = callbacks.onFlashlightToggle
        )

        BottomActionButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            detectedCan = state.detectedCan,
            onCanSelected = callbacks.onCanSelected
        )
    }
}

@Composable
private fun TopControlIcons(
    modifier: Modifier = Modifier,
    isFlashlightOn: Boolean,
    onCloseClicked: () -> Unit,
    onFlashlightToggle: () -> Unit
) {
    Row(
        modifier = modifier.padding(PaddingDefaults.Medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        CircularIconButton(
            onClick = onCloseClicked,
            icon = Icons.Rounded.Close,
            contentDescription = stringResource(R.string.health_card_order_close)
        )

        FlashlightToggleButton(
            isFlashlightOn = isFlashlightOn,
            onToggle = onFlashlightToggle
        )
    }
}

@Composable
private fun CircularIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(SizeDefaults.sixfold)
            .background(
                color = AppTheme.colors.neutral050,
                shape = CircleShape
            )
            .semantics {
                traversalIndex = 1f
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = AppTheme.colors.neutral600,
            modifier = Modifier.size(SizeDefaults.triple)
        )
    }
}

@Composable
private fun FlashlightToggleButton(
    isFlashlightOn: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedIconButton(
        onClick = onToggle,
        imageVector = if (isFlashlightOn) Icons.Outlined.FlashlightOff else Icons.Outlined.FlashlightOn,
        contentDescription = null,
        text = stringResource(
            if (isFlashlightOn) {
                R.string.cdw_scanner_flashlight_off
            } else {
                R.string.cdw_scanner_flashlight_on
            }
        ),
        border = BorderStroke(1.dp, AppTheme.colors.primary700),
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = AppTheme.colors.neutral000,
            contentColor = AppTheme.colors.primary700
        ),
        shape = RoundedCornerShape(SizeDefaults.triple),
        modifier = modifier.semantics {
            traversalIndex = 2f
        }
    )
}

@Composable
private fun BottomActionButton(
    modifier: Modifier = Modifier,
    detectedCan: String?,
    onCanSelected: (String) -> Unit
) {
    PrimaryButton(
        onClick = { detectedCan?.let(onCanSelected) },
        enabled = detectedCan != null,
        shape = RoundedCornerShape(SizeDefaults.triple),
        modifier = modifier
            .padding(
                horizontal = SizeDefaults.sevenfoldAndHalf,
                vertical = PaddingDefaults.Medium
            )
            .semantics {
                traversalIndex = 0f
            },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = AppTheme.colors.primary600,
            disabledBackgroundColor = AppTheme.colors.neutral300
        )
    ) {
        Text(
            text = stringResource(R.string.cdw_scanner_accept_button),
            style = AppTheme.typography.button,
            color = if (detectedCan != null) Color.White else AppTheme.colors.neutral500,
            modifier = Modifier.padding(vertical = PaddingDefaults.Small)
        )
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onSetupCamera: (CameraSetupParams) -> Unit,
    onToggleFlashlight: (Boolean) -> Unit,
    onImageAnalyzed: (ImageProxy, PreviewView, Density, Int, Int) -> Unit,
    isPaused: Boolean = false,
    isFlashlightOn: Boolean = false,
    isPreview: Boolean = false
) {
    if (isPreview) {
        MockCameraPreview(modifier = modifier)
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    val screenDimensions = remember(configuration) {
        with(density) {
            Pair(
                configuration.screenWidthDp.dp.toPx().toInt(),
                configuration.screenHeightDp.dp.toPx().toInt()
            )
        }
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    LaunchedEffect(isFlashlightOn) {
        onToggleFlashlight(isFlashlightOn)
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PreviewView(context).also { preview ->
                    previewView = preview
                }
            }
        )

        LaunchedEffect(previewView) {
            previewView?.let { preview ->
                onSetupCamera(
                    CameraSetupParams(
                        context = context,
                        lifecycleOwner = lifecycleOwner,
                        previewView = preview,
                        cameraExecutor = cameraExecutor,
                        onImageAnalysis = { imageProxy ->
                            if (!isPaused) {
                                onImageAnalyzed(
                                    imageProxy,
                                    preview,
                                    density,
                                    screenDimensions.first,
                                    screenDimensions.second
                                )
                            } else {
                                imageProxy.close()
                            }
                        }
                    )
                )
            }
        }

        capturedBitmap?.let { bitmap ->
            if (isPaused) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }

    LaunchedEffect(isPaused) {
        when {
            isPaused && capturedBitmap == null -> {
                previewView?.getBitmap()?.let { bitmap ->
                    capturedBitmap = bitmap
                }
            }

            !isPaused -> capturedBitmap = null
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScanningOverlay(
    modifier: Modifier = Modifier,
    detectedCan: String? = null
) {
    val hasDetectedCan = detectedCan != null

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cardWidth = SizeDefaults.fortyfold.toPx()
            val cardHeight = SizeDefaults.twentyfivefold.toPx()
            val cardLeft = (size.width - cardWidth) / 2
            val cardTop = (size.height - cardHeight) / 2

            val overlayPath = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(0f, 0f, size.width, size.height))
                addRoundRect(
                    RoundRect(
                        rect = Rect(cardLeft, cardTop, cardLeft + cardWidth, cardTop + cardHeight),
                        cornerRadius = CornerRadius(SizeDefaults.one.toPx())
                    )
                )
            }

            drawPath(
                path = overlayPath,
                color = Color.Black.copy(alpha = ScannerConstants.OVERLAY_ALPHA)
            )
        }

        InstructionCard(
            modifier = Modifier.align(Alignment.TopCenter),
            hasDetectedCan = hasDetectedCan,
            detectedCan = detectedCan
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun InstructionCard(
    modifier: Modifier = Modifier,
    hasDetectedCan: Boolean,
    detectedCan: String?
) {
    val focusRequester = remember { FocusRequester() }

    val instructionTextRes = if (hasDetectedCan) {
        R.string.cdw_scanner_can_detected
    } else {
        R.string.cdw_scanner_instruction_text
    }

    val instructionText = stringResource(instructionTextRes)

    val contentDescription = buildString {
        append(instructionText)
        if (hasDetectedCan) {
            detectedCan?.let { append(". $it") }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = SizeDefaults.doubleHalf,
                vertical = SizeDefaults.thirteenfold
            )
            .focusRequester(focusRequester)
            .focusable()
            .semantics {
                traversalIndex = -1f
                this.contentDescription = contentDescription
            },
        backgroundColor = Color.White,
        shape = RoundedCornerShape(PaddingDefaults.Small),
        elevation = PaddingDefaults.Tiny
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = SizeDefaults.doubleHalf,
                vertical = PaddingDefaults.Small
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = instructionTextRes,
                transitionSpec = { scaleIn() + fadeIn() with scaleOut() + fadeOut() }
            ) { textRes ->
                Text(
                    text = stringResource(textRes),
                    style = AppTheme.typography.body1,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }

            AnimatedVisibility(
                visible = hasDetectedCan && detectedCan != null,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioHighBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(ScannerConstants.ANIMATION_DURATION_MS))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SpacerSmall()
                    Text(
                        text = detectedCan.orEmpty(),
                        style = AppTheme.typography.h4,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

@Composable
fun MockCameraPreview(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(AppTheme.colors.neutral050),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Camera Preview",
            color = AppTheme.colors.neutral999,
            style = AppTheme.typography.body1
        )
    }
}

@LightDarkPreview
@Composable
fun ScannerScreenContentPreview(
    @PreviewParameter(CardScannerScreenParameterProvider::class) previewData: CardScannerScreenParameter
) {
    PreviewAppTheme {
        val state = ScannerState(
            detectedCan = previewData.detectedCan,
            isScanning = previewData.isScanning,
            isFlashlightOn = previewData.isFlashlightOn
        )

        val callbacks = ScannerCallbacks(
            onSetupCamera = { },
            onToggleFlashlight = { },
            onImageAnalyzed = { _, _, _, _, _ -> },
            onCanSelected = { },
            onClose = { },
            onFlashlightToggle = { }
        )

        ScannerScreenContent(
            state = state,
            callbacks = callbacks,
            isPreview = true
        )
    }
}
