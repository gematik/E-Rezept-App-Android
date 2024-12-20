/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.profiles.ui.screens

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
import androidx.compose.material.CircularProgressIndicator
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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.canhub.cropper.CropImageView
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.permissions.removeMetadataFromBitmap
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.profiles.presentation.rememberProfileController
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile.Companion.profileById
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar

const val CROPPED_IMAGE_SIZE = 256
const val IMAGE_ALPHA = 0.8f

class ProfileImageCropperScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val profileId = remember { requireNotNull(navBackStackEntry.arguments?.getString(ProfileRoutes.PROFILE_NAV_PROFILE_ID)) }
        val profilesController = rememberProfileController()
        val profiles by profilesController.getProfilesState2()
        profiles?.profileById(profileId)?.let { selectedProfile ->
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
            var background: Bitmap? by remember { mutableStateOf(null) }
            val imagePickerLauncher =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
                    uri?.let {
                        @Requirement(
                            "O.Data_8#4",
                            sourceSpecification = "BSI-eRp-ePA",
                            rationale = "Metadata is removed from the bitmap before it is shown to the user to save as avatar."
                        )
                        background = removeMetadataFromBitmap(getOriginalBitMap(context, uri))
                        cropView.setImageBitmap(background)
                    } ?: run { navController.popBackStack() }
                }
            LaunchedEffect(Unit) {
                imagePickerLauncher.launch("image/*")
            }
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    NavigationTopAppBar(
                        navigationMode = NavigationBarMode.Back,
                        title = "",
                        onBack = { navController.popBackStack() },
                        actions = {
                            TextButton(onClick = {
                                cropView.getCroppedImage(
                                    reqWidth = CROPPED_IMAGE_SIZE,
                                    reqHeight = CROPPED_IMAGE_SIZE
                                )?.let {
                                    profilesController.savePersonalizedProfileImage(selectedProfile.id, it)
                                    profilesController.saveAvatarFigure(selectedProfile.id, ProfilesData.Avatar.PersonalizedImage)
                                    navController.popBackStack()
                                }
                            }) {
                                Text(text = stringResource(R.string.image_crop_save_image))
                            }
                        }
                    )
                },
                backgroundColor = Color.Black
            ) { paddingValues ->
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
        } ?: run {
            CircularProgressIndicator()
        }
    }
}

private fun getOriginalBitMap(context: Context, imageUri: Uri): Bitmap {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        @Suppress("DEPRECATION")
        return MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
    } else {
        val source = ImageDecoder.createSource(context.contentResolver, imageUri)
        return ImageDecoder.decodeBitmap(source)
    }
}
