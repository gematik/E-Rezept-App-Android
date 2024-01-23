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

package de.gematik.ti.erp.app.profiles.ui

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.updateLayoutParams
import com.canhub.cropper.CropImageView
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar

const val CROPPED_IMAGE_SIZE = 256
const val IMAGE_ALPHA = 0.8f

@Composable
fun ProfileImageCropper(onSaveCroppedImage: (Bitmap) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val cropView = remember {
        CropImageView(context).apply {
            isAutoZoomEnabled = false
            cropShape = CropImageView.CropShape.OVAL
            setFixedAspectRatio(true)
        }
    }

    var readStoragePermissionGranted by rememberSaveable { mutableStateOf(false) }
    val readStoragePermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            readStoragePermissionGranted = it
        }

    val readStoragePermissionRequired =
        Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q
    @Requirement(
        "O.Plat_3#1",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "platform dialog for READ_EXTERNAL_STORAGE"
    )
    LaunchedEffect(readStoragePermissionRequired, readStoragePermissionGranted) {
        if (readStoragePermissionRequired && !readStoragePermissionGranted) {
            readStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            NavigationTopAppBar(
                navigationMode = NavigationBarMode.Back,
                title = "",
                onBack = onBack,
                actions = {
                    TextButton(onClick = {
                        cropView.getCroppedImage(reqWidth = CROPPED_IMAGE_SIZE, reqHeight = CROPPED_IMAGE_SIZE)?.let {
                            onSaveCroppedImage(it)
                        }
                    }) {
                        Text(text = stringResource(R.string.image_crop_save_image))
                    }
                }
            )
        },
        backgroundColor = Color.Black
    ) { paddingValues ->
        var background: Bitmap? by remember { mutableStateOf(null) }
        val imagePickerLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    background = getOriginalBitMap(context, uri)
                    cropView.setImageBitmap(background)
                } ?: run { onBack() }
            }

        LaunchedEffect(Unit) {
            imagePickerLauncher.launch("image/*")
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            background?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier
                        .blur(12.dp)
                        .alpha(IMAGE_ALPHA)
                        .fillMaxSize()
                )
            }

            val width = with(LocalDensity.current) {
                this@BoxWithConstraints.maxWidth.roundToPx()
            }
            val height = with(LocalDensity.current) {
                this@BoxWithConstraints.maxHeight.roundToPx()
            }
            AndroidView(
                factory = {
                    cropView
                }
            ) {
                it.updateLayoutParams {
                    this.height = height
                    this.width = width
                }
            }
        }
    }
}

fun getOriginalBitMap(context: Context, imageUri: Uri): Bitmap {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        @Suppress("DEPRECATION")
        return MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
    } else {
        val source = ImageDecoder.createSource(context.contentResolver, imageUri)
        return ImageDecoder.decodeBitmap(source)
    }
}
