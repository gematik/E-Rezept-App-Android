/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.debugsettings.qrcode

import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import de.gematik.ti.erp.app.base.openSettingsAsNewActivity
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

private const val MAX_WEIGHT = 0.9f
private const val MIN_WEIGHT = 0.1f
private const val MID_WEIGHT = 0.4f

@Composable
fun QrCodeScanner(
    onSaveCertificate: (String) -> Unit,
    onSavePrivateKey: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    var result by remember { mutableStateOf("") }
    var closeBottomBar by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    // very simple permission handling
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCameraPermission = isGranted
        }
    )
    LaunchedEffect(true) {
        launcher.launch(android.Manifest.permission.CAMERA)
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            NavigationTopAppBar(
                navigationMode = NavigationBarMode.Close,
                title = "QR Code Scanner"
            ) {
                onBack()
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { context ->
                        val previewView = PreviewView(context)
                        val preview = Preview.Builder().build()
                        val selector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()
                        preview.setSurfaceProvider(previewView.surfaceProvider)
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setResolutionSelector(ResolutionSelector.Builder().build())
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        imageAnalysis.setAnalyzer(
                            ContextCompat.getMainExecutor(context),
                            QrCodeAnalyzer(
                                onQrCodeDetected = { qrCode ->
                                    Napier.i { "QrCodeAnalyzer onQrCodeDetected $qrCode" }
                                    closeBottomBar = false
                                    result = qrCode
                                },
                                onQrCodeNotDetected = {
                                    // handle no qr code
                                }
                            )
                        )
                        try {
                            cameraProviderFuture.get().bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            Napier.e { "Use case binding failed ${e.stackTraceToString()}" }
                            // handle exception
                        }
                        previewView
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                NoCameraPermissionSection()
            }
            AnimatedVisibility(visible = !closeBottomBar) {
                QrCodeScannerBottomBar(
                    result = result,
                    onClose = {
                        closeBottomBar = true
                    },
                    onSaveCertificate = { certificate ->
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Certificate saved",
                                duration = SnackbarDuration.Short,
                                withDismissAction = true
                            )
                        }
                        onSaveCertificate(certificate)
                    },
                    onSavePrivateKey = { privateKey ->
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Private Key saved",
                                duration = SnackbarDuration.Short,
                                withDismissAction = true
                            )
                        }
                        onSavePrivateKey(privateKey)
                    }
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.NoCameraPermissionSection() {
    val context = LocalContext.current
    Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier
                    .padding(PaddingDefaults.Medium)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = AppTheme.colors.neutral600,
                style = AppTheme.typography.body2,
                text = "No camera permission"
            )
            SpacerMedium()
            ElevatedButton(
                modifier = Modifier.padding(PaddingDefaults.Medium),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = AppTheme.colors.primary700
                ),
                onClick = {
                    context.openSettingsAsNewActivity(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    )
                },
                content = {
                    Text(
                        text = "Go to Settings",
                        color = AppTheme.colors.neutral000,
                        modifier = Modifier
                            .padding(horizontal = PaddingDefaults.Medium),
                        style = AppTheme.typography.body2
                    )
                }
            )
        }
    }
}

@Composable
private fun QrCodeScannerBottomBar(
    result: String,
    onClose: () -> Unit,
    onSaveCertificate: (String) -> Unit,
    onSavePrivateKey: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .background(AppTheme.colors.neutral100)
            .padding(PaddingDefaults.Medium)
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(MAX_WEIGHT)
                    .height(SizeDefaults.doubleHalf),
                contentAlignment = Alignment.Center
            ) {
                Divider(
                    modifier = Modifier
                        .width(SizeDefaults.fivefold)
                        .height(SizeDefaults.quarter),
                    color = AppTheme.colors.neutral999
                )
            }
            IconButton(
                modifier = Modifier
                    .weight(MIN_WEIGHT)
                    .height(SizeDefaults.doubleHalf),
                onClick = onClose
            ) {
                Icon(
                    tint = AppTheme.colors.neutral600,
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null
                )
            }
        }
        SpacerSmall()
        Text(
            text = "Scan Result",
            color = AppTheme.colors.neutral600,
            maxLines = 1,
            style = AppTheme.typography.subtitle1
        )
        SpacerTiny()
        Text(
            text = result,
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            maxLines = 5,
            color = AppTheme.colors.neutral600,
            softWrap = true,
            style = AppTheme.typography.body2
        )
        SpacerSmall()
        Row {
            ElevatedButton(
                modifier = Modifier.weight(MID_WEIGHT),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = AppTheme.colors.primary700
                ),
                onClick = {
                    onSaveCertificate(result)
                }
            ) {
                Text(
                    text = "Save Certificate",
                    color = AppTheme.colors.neutral000,
                    style = AppTheme.typography.button
                )
            }
            SpacerMedium()
            ElevatedButton(
                modifier = Modifier.weight(MID_WEIGHT),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = AppTheme.colors.primary700
                ),
                onClick = {
                    onSavePrivateKey(result)
                }
            ) {
                Text(
                    text = "Save Private Key",
                    color = AppTheme.colors.neutral000,
                    style = AppTheme.typography.button
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
fun QrCodeScannerBottomBarPreview() {
    PreviewAppTheme {
        QrCodeScannerBottomBar(
            onSaveCertificate = {},
            onSavePrivateKey = {},
            onClose = {},
            result = "This is a QR Code Result"
        )
    }
}

@LightDarkPreview
@Composable
fun QrCodeScannerBottomBarMoreTextPreview() {
    PreviewAppTheme {
        QrCodeScannerBottomBar(
            onSaveCertificate = {},
            onSavePrivateKey = {},
            onClose = {},
            result = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod " +
                "tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. " +
                "At vero eos et accusam et justo duo dolores et ea rebum. " +
                "Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. " +
                "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt " +
                "ut labore et dolore magna aliquyam erat, sed diam voluptua. " +
                "At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, " +
                "no sea takimata sanctus est Lorem ipsum dolor sit amet."
        )
    }
}

@LightDarkPreview
@Composable
fun NoCameraPermissionSectionPreview() {
    PreviewAppTheme {
        Column {
            NoCameraPermissionSection()
        }
    }
}
