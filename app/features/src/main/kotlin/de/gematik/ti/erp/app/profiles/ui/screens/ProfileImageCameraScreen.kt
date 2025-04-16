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

package de.gematik.ti.erp.app.profiles.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.base.openSettingsAsNewActivity
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.permissions.cameraPermission
import de.gematik.ti.erp.app.permissions.getBitmapFromCamera
import de.gematik.ti.erp.app.permissions.hasPermissionOrRationale
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.profiles.presentation.rememberProfileImagePersonalizedImageScreenController
import de.gematik.ti.erp.app.profiles.ui.components.CircularBitmapImage
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.AccessToCameraDenied
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.erezeptButtonColors
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.greyCircularBorder

class ProfileImageCameraScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        ProfileImageCameraScreenComponent(
            navController = navController,
            navBackStackEntry = navBackStackEntry
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileImageCameraScreenComponent(
    navController: NavController,
    navBackStackEntry: NavBackStackEntry
) {
    val profileId = remember {
        requireNotNull(
            navBackStackEntry.arguments?.getString(ProfileRoutes.PROFILE_NAV_PROFILE_ID)
        ) { "ProfileId is missing in ProfileImageCameraScreen" }
    }
    val controller = rememberProfileImagePersonalizedImageScreenController(profileId)
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val cameraPermissionState = rememberPermissionState(permission = cameraPermission)
    val takePictureLauncher = getBitmapFromCamera {
        imageBitmap = it
    }
    val permissionPresent by remember(cameraPermissionState.status) {
        derivedStateOf {
            cameraPermissionState.hasPermissionOrRationale()
        }
    }

    LaunchedEffect(Unit) {
        when {
            cameraPermissionState.status.isGranted -> takePictureLauncher.launch(null)
            else -> cameraPermissionState.launchPermissionRequest()
        }
    }

    @Requirement(
        "O.Data_8#3",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Image without metadata is saved here."
    )
    ProfileImageCameraScreenScaffold(
        takePhotoText = stringResource(R.string.profile_image_selector_take_photo),
        savePhotoText = stringResource(R.string.profile_image_selector_save),
        imageBitmap = imageBitmap,
        onClick = {
            when {
                cameraPermissionState.status.isGranted -> takePictureLauncher.launch(null)
                else -> cameraPermissionState.launchPermissionRequest()
            }
        },
        onSavePicture = {
            imageBitmap?.let {
                controller.updateProfileImageBitmap(it)
                navController.popBackStack()
            }
        },
        onBack = {
            navController.popBackStack()
        },
        permissionPresent = permissionPresent
    )
}

@Composable
private fun ProfileImageCameraScreenScaffold(
    takePhotoText: String,
    savePhotoText: String,
    imageBitmap: Bitmap?,
    onClick: () -> Unit,
    onBack: () -> Unit,
    onSavePicture: () -> Unit,
    permissionPresent: Boolean
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            NavigationTopAppBar(
                navigationMode = NavigationBarMode.Close,
                title = stringResource(R.string.profile_image_selector_title),
                onBack = onBack
            )
        },
        bottomBar = {
            AnimatedVisibility(permissionPresent) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = PaddingDefaults.XXLargeMedium),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        modifier = Modifier.padding(PaddingDefaults.Medium),
                        enabled = permissionPresent,
                        colors = erezeptButtonColors(),
                        onClick = {
                            imageBitmap?.let { onSavePicture() } ?: run { onClick() }
                        }
                    ) {
                        Text(
                            text = when {
                                imageBitmap != null -> savePhotoText
                                else -> takePhotoText
                            }
                        )
                    }
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .background(AppTheme.colors.neutral000)
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!permissionPresent) {
                SpacerMedium()
                AccessToCameraDenied(
                    onClick = {
                        context.openSettingsAsNewActivity(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    },
                    showTopBar = false,
                    showSettingsButton = true
                )
            } else {
                imageBitmap?.let { bitmap ->
                    CircularBitmapImage(
                        modifier = Modifier
                            .clip(CircleShape)
                            .greyCircularBorder()
                            .padding(vertical = PaddingDefaults.Medium),
                        image = bitmap,
                        size = SizeDefaults.thirtyEightfold
                    )
                } ?: run {
                    Image(
                        modifier = Modifier
                            .clip(CircleShape)
                            .padding(SizeDefaults.twentyfold)
                            .fillMaxSize(),
                        imageVector = Icons.Rounded.AddAPhoto,
                        colorFilter = ColorFilter.tint(AppTheme.colors.primary400),
                        contentDescription = null

                    )
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
internal fun ProfileImageCameraScreenNoPermissionPreview() {
    PreviewAppTheme {
        ProfileImageCameraScreenScaffold(
            takePhotoText = "Take Photo",
            savePhotoText = "Save Photo",
            imageBitmap = null,
            onClick = {},
            onSavePicture = {},
            onBack = {},
            permissionPresent = false
        )
    }
}

@LightDarkPreview
@Composable
internal fun ProfileImageCameraScreenWithPermissionPreview() {
    PreviewAppTheme {
        val context = LocalContext.current
        ProfileImageCameraScreenScaffold(
            imageBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.prescription),
            takePhotoText = "Take Photo",
            savePhotoText = "Save Photo",
            onClick = {},
            onSavePicture = {},
            onBack = {},
            permissionPresent = true
        )
    }
}

@LightDarkPreview
@Composable
internal fun ProfileImageCameraScreenWithPermissionNoImagePreview() {
    PreviewAppTheme {
        ProfileImageCameraScreenScaffold(
            imageBitmap = null,
            takePhotoText = "Take Photo",
            savePhotoText = "Save Photo",
            onClick = {},
            onSavePicture = {},
            onBack = {},
            permissionPresent = true
        )
    }
}
