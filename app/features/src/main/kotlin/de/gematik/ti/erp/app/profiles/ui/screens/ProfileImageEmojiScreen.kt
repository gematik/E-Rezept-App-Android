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

@file:Suppress("UnusedPrivateProperty", "UnusedPrivateMember")

package de.gematik.ti.erp.app.profiles.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.content.MediaType
import androidx.compose.foundation.content.hasMediaType
import androidx.compose.foundation.content.receiveContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text2.BasicTextField2
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import coil.compose.AsyncImage
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.profiles.model.PictureDataType
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.profiles.presentation.rememberProfileImagePersonalizedImageScreenController
import de.gematik.ti.erp.app.profiles.ui.components.CircularBitmapImage
import de.gematik.ti.erp.app.profiles.ui.components.ProfileBackgroundColorComponent
import de.gematik.ti.erp.app.profiles.ui.components.color
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.SpacerXXXLarge
import de.gematik.ti.erp.app.utils.compose.CenterColumn
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.SaveButton
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.animatedCircularBorder
import de.gematik.ti.erp.app.utils.extensions.circularBorder
import de.gematik.ti.erp.app.utils.extensions.keyboardAsState
import de.gematik.ti.erp.app.utils.extensions.showKeyboardOnNotOpen
import io.github.aakira.napier.Napier
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch

private const val TEXT_SIZE = 64f
private const val DIAMETER_X = 2f
private const val DIAMETER_Y = 2f
private const val SIZE_DP = 150

/**
 * render as a full screen
 */
class ProfileImageEmojiScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        ProfileImageEmojiComponent(
            navController = navController,
            navBackStackEntry = navBackStackEntry
        )
    }
}

@Composable
fun ProfileImageEmojiComponent(
    navController: NavController,
    navBackStackEntry: NavBackStackEntry
) {
    val profileId = remember {
        requireNotNull(
            navBackStackEntry.arguments?.getString(ProfileRoutes.PROFILE_NAV_PROFILE_ID)
        ) { "ProfileId is missing in ProfileImageEmojiScreen" }
    }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    val emptySelectionString = stringResource(R.string.profile_image_selector_empty)

    var imageUri by remember { mutableStateOf(Uri.EMPTY) }
    var textData by remember { mutableStateOf("") }
    var pictureDataType by remember { mutableStateOf(PictureDataType.EMPTY) }

    val focusContentEvent by remember { mutableStateOf(ComposableEvent<Unit>()) }
    val coroutineScope = rememberCoroutineScope()
    val isDarkMode = isSystemInDarkTheme()

    val controller = rememberProfileImagePersonalizedImageScreenController(profileId)

    val profile by controller.profile.collectAsStateWithLifecycle()
    val isSamsungDevice = controller.isSamsungDevice()

    val keyboardState by keyboardAsState()

    // these two change the background color of the profile image
    var colorName = profile?.color
    val backgroundColor = colorName?.color()?.backGroundColor ?: Color.White

    val shouldAnimateImage by remember(keyboardState, pictureDataType) {
        derivedStateOf { !keyboardState && pictureDataType == PictureDataType.EMPTY }
    }

    val enableButton by remember(pictureDataType) {
        derivedStateOf { pictureDataType != PictureDataType.EMPTY }
    }

    focusContentEvent.listen {
        coroutineScope.launch {
            focusRequester.requestFocus()
            // wait the keyboard to be open, if the focus worked and if not open the keyboard
            awaitFrame()
            keyboardController?.showKeyboardOnNotOpen(keyboardState)
        }
    }

    // Force keyboard to be open on start
    LaunchedEffect(Unit) {
        focusContentEvent.trigger()
    }

    DisposableEffect(Unit) {
        onDispose {
            keyboardController?.hide()
        }
    }

    // If the user has not selected any content, show the warning
    LaunchedEffect(textData, imageUri) {
        if (textData.isBlank() && imageUri == Uri.EMPTY) {
            pictureDataType = PictureDataType.EMPTY
        }
    }

    ProfileImageEmojiScreenContent(
        snackbarHostState = snackbarHostState,
        focusContentEvent = focusContentEvent,
        focusRequester = focusRequester,
        shouldAnimateImage = shouldAnimateImage,
        isEnabled = enableButton,
        scrollState = scrollState,
        pictureDataType = pictureDataType,
        imageUri = imageUri,
        textData = textData,
        profile = profile,
        isSamsungDevice = isSamsungDevice,
        onColorPicked = { color ->
            colorName = color
            controller.updateProfileColor(color)
        },
        onTextContentReceived = { text ->
            if (text.isNotBlank()) {
                imageUri = Uri.EMPTY
                pictureDataType = PictureDataType.TEXT
            }
            textData = text
        },
        onImageContentReceived = {
            textData = ""
            pictureDataType = PictureDataType.IMAGE
            imageUri = it
        },
        onBack = { navController.popBackStack() },
        onSelect = {
            when (pictureDataType) {
                PictureDataType.IMAGE -> {
                    uriToBitmap(context, imageUri)?.let { bitmap ->
                        controller.updateProfileImageBitmap(bitmap)
                    }
                }

                PictureDataType.TEXT -> {
                    createCircularBitmapFromText(
                        context = context,
                        text = textData,
                        isDarkMode = isDarkMode,
                        backgroundColor = backgroundColor
                    )?.let { bitmap ->
                        controller.updateProfileImageBitmap(bitmap)
                    }
                }

                else -> {
                    // no-op
                }
            }
            navController.popBackStack()
        },
        onEmpty = {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = emptySelectionString,
                    withDismissAction = true,
                    duration = SnackbarDuration.Long
                )
            }
        }
    )
}

@Suppress("MagicNumber", "CyclomaticComplexMethod", "LongParameterList")
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileImageEmojiScreenContent(
    snackbarHostState: SnackbarHostState,
    focusRequester: FocusRequester,
    focusContentEvent: ComposableEvent<Unit>,
    pictureDataType: PictureDataType,
    profile: ProfilesUseCaseData.Profile?,
    shouldAnimateImage: Boolean,
    isEnabled: Boolean,
    imageUri: Uri,
    textData: String,
    isSamsungDevice: Boolean,
    scrollState: ScrollState,
    onTextContentReceived: (String) -> Unit,
    onImageContentReceived: (Uri) -> Unit,
    onColorPicked: (ProfilesData.ProfileColorNames) -> Unit,
    onBack: () -> Unit,
    onSelect: () -> Unit,
    onEmpty: () -> Unit
) {
    val context = LocalContext.current
    val isDarkMode = isSystemInDarkTheme()
    Scaffold(
        contentColor = AppTheme.colors.neutral900,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            NavigationTopAppBar(
                navigationMode = NavigationBarMode.Close,
                title = stringResource(R.string.profile_image_selector_title),
                onBack = onBack
            )
        },
        bottomBar = {
            SaveButton(
                text = stringResource(id = R.string.onboarding_bottom_button_save),
                isEnabled = isEnabled,
                onEmpty = onEmpty
            ) { onSelect() }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.neutral025)
                .padding(it)
                .verticalScroll(scrollState)
        ) {
            SpacerMedium()
            AnimatedVisibility(pictureDataType == PictureDataType.EMPTY) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(SizeDefaults.twelvefold)
                            .clip(CircleShape)
                            .clickable { focusContentEvent.trigger() }
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            modifier = Modifier
                                .animatedCircularBorder(shouldAnimateImage)
                                .size(SizeDefaults.tenfold)
                                .alpha(0.8f),
                            colorFilter = ColorFilter.tint(AppTheme.colors.neutral999),
                            imageVector = Icons.Rounded.EmojiEmotions,
                            contentDescription = null
                        )
                    }
                }
            }
            SpacerMedium()
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                if (pictureDataType == PictureDataType.TEXT) {
                    createCircularBitmapFromText(
                        context = context,
                        text = textData,
                        isDarkMode = isDarkMode,
                        backgroundColor = profile?.color?.color()?.backGroundColor ?: Color.White
                    )?.let { bitmap ->
                        CircularBitmapImage(
                            modifier = Modifier
                                .size(SizeDefaults.twelvefold)
                                .clip(CircleShape)
                                .circularBorder(profile?.color?.color()?.borderColor ?: Color.Gray)
                                .background(profile?.color?.color()?.backGroundColor ?: Color.White)
                                .clickable { focusContentEvent.trigger() },
                            image = bitmap
                        )
                    }
                } else if (pictureDataType == PictureDataType.IMAGE) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(SizeDefaults.twelvefold)
                            .clip(CircleShape)
                            .circularBorder(profile?.color?.color()?.borderColor ?: Color.Gray)
                            .background(profile?.color?.color()?.backGroundColor ?: Color.White)
                            .clickable { focusContentEvent.trigger() },
                        contentScale = ContentScale.Fit
                    )
                }
            }
            SpacerTiny()
            if (isSamsungDevice) {
                BasicTextField(
                    value = textData,
                    onValueChange = { value ->
                        Napier.i { "onValueChange: $value" }
                        onTextContentReceived(value)
                    },
                    keyboardOptions = KeyboardOptions.Default,
                    modifier = Modifier
                        .alpha(0f) // hide the text field and only use the receiveContent
                        .height(SizeDefaults.one)
                        .focusRequester(focusRequester)
                )
            } else {
                @Suppress("LoopWithTooManyJumpStatements")
                (
                    BasicTextField2(
                        value = textData,
                        onValueChange = { value ->
                            Napier.i { "onValueChange: $value" }
                            onTextContentReceived(value)
                        },
                        keyboardOptions = KeyboardOptions.Default,
                        modifier = Modifier
                            .hideFromView()
                            .focusRequester(focusRequester)
                            .receiveContent(setOf(MediaType.All)) { content ->
                                if (content.hasMediaType(MediaType.Image)) {
                                    val clipData = content.clipEntry.clipData
                                    for (index in 0 until clipData.itemCount) {
                                        val item = clipData.getItemAt(index) ?: continue
                                        onImageContentReceived(item.uri)
                                        item.uri ?: continue
                                    }
                                }
                                content
                            }
                    )
                    )
            }

            profile?.let { editableProfile ->
                CenterColumn {
                    ProfileBackgroundColorComponent(
                        color = editableProfile.color,
                        onColorPicked = onColorPicked
                    )
                }
            }

            SpacerXXXLarge()
        }
    }
}

@Suppress("MagicNumber")
private fun createCircularBitmapFromText(
    context: Context,
    text: String,
    sizeDp: Int = SIZE_DP,
    isDarkMode: Boolean,
    backgroundColor: Color
): Bitmap? {
    try {
        val density = context.resources.displayMetrics.density
        val textSizeSp = TEXT_SIZE
        val backgroundPaint = Paint().apply {
            color = backgroundColor.toArgb()
            textSize = textSizeSp * density
            isAntiAlias = true // For smooth edges
        }

        val colouredPaint = Paint().apply {
            color = when {
                isDarkMode -> Color(0xFFE0E0E0).toArgb()
                else -> Color(0xFF757575).toArgb()
            }
            textSize = textSizeSp * density
            isAntiAlias = true // For smooth edges
        }

        // Calculate the diameter of the circular bounding box
        val diameter = sizeDp * density

        val bitmap = Bitmap.createBitmap(diameter.toInt(), diameter.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Calculate the horizontal position (x) to center the text // diameter / DIAMETER_X
        val x = when {
            containsEmoji(text) -> 95f
            else -> 90f
        }

        val ascent = -backgroundPaint.ascent()
        val descent = backgroundPaint.descent()
        val textHeight = ascent + descent
        // Calculate the vertical position (y) to center the text
        val y = (diameter - textHeight) / DIAMETER_Y + ascent

        val rect = RectF(0f, 0f, diameter, diameter)

        // Draw the white circle
        canvas.drawOval(rect, backgroundPaint)

        // Draw the text
        canvas.drawText(text, x, y, colouredPaint)

        return bitmap
    } catch (e: Exception) {
        // If an exception occurs, return null
        Napier.e { "Error on bitmap creation ${e.stackTraceToString()}" }
        return null
    }
}

private fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    val inputStream = context.contentResolver.openInputStream(uri)
    return inputStream?.use {
        BitmapFactory.decodeStream(it)
    }
}

private fun containsEmoji(text: String): Boolean {
    val emojiPattern = Regex(
        "[\\x{1F600}-\\x{1F64F}|" + // Emoticons
            "\\x{1F300}-\\x{1F5FF}|" + // Misc Symbols and Pictographs
            "\\x{1F680}-\\x{1F6FF}|" + // Transport and Map
            "\\x{1F700}-\\x{1F77F}|" + // Alchemical Symbols
            "\\x{1F780}-\\x{1F7FF}|" + // Geometric Shapes Extended
            "\\x{1F800}-\\x{1F8FF}|" + // Supplemental Arrows-C
            "\\x{1F900}-\\x{1F9FF}|" + // Supplemental Symbols and Pictographs
            "\\x{1FA70}-\\x{1FAFF}|" + // Symbols and Pictographs Extended-A
            "\\x{2600}-\\x{26FF}|" + // Misc symbols (e.g., sun, moon, umbrella, etc.)
            "\\x{2700}-\\x{27BF}|" + // Dingbats
            "\\x{2300}-\\x{23FF}|" + // Misc Technical
            "\\x{2B50}-\\x{2BFF}|" + // Additional symbols
            "\\x{1F1E6}-\\x{1F1FF}]", // Flags (iOS)
        RegexOption.IGNORE_CASE
    )
    return emojiPattern.containsMatchIn(text)
}

private fun Modifier.hideFromView() = alpha(0f).height(SizeDefaults.one)

@LightDarkPreview
@Composable
internal fun ProfileImageEmojiScreenContentEmojiPreview() {
    PreviewAppTheme {
        val scrollState = rememberScrollState()
        ProfileImageEmojiScreenContent(
            snackbarHostState = SnackbarHostState(),
            focusRequester = FocusRequester(),
            focusContentEvent = ComposableEvent(),
            pictureDataType = PictureDataType.TEXT,
            profile = null,
            shouldAnimateImage = false,
            imageUri = Uri.EMPTY,
            textData = "\uD83D\uDE00",
            isSamsungDevice = false,
            isEnabled = true,
            scrollState = scrollState,
            onTextContentReceived = { },
            onImageContentReceived = { },
            onColorPicked = { },
            onBack = { },
            onSelect = { },
            onEmpty = { }
        )
    }
}

@LightDarkPreview
@Composable
internal fun ProfileImageEmojiScreenContentTextPreview() {
    PreviewAppTheme {
        val scrollState = rememberScrollState()
        ProfileImageEmojiScreenContent(
            snackbarHostState = SnackbarHostState(),
            focusRequester = FocusRequester(),
            focusContentEvent = ComposableEvent(),
            pictureDataType = PictureDataType.TEXT,
            profile = null,
            shouldAnimateImage = false,
            imageUri = Uri.EMPTY,
            textData = "10",
            isSamsungDevice = false,
            isEnabled = true,
            scrollState = scrollState,
            onTextContentReceived = { },
            onImageContentReceived = { },
            onColorPicked = { },
            onBack = { },
            onSelect = { },
            onEmpty = { }
        )
    }
}

@LightDarkPreview
@Composable
internal fun ProfileImageEmojiScreenContentEmptyPreview() {
    PreviewAppTheme {
        val scrollState = rememberScrollState()
        ProfileImageEmojiScreenContent(
            snackbarHostState = SnackbarHostState(),
            focusRequester = FocusRequester(),
            focusContentEvent = ComposableEvent(),
            pictureDataType = PictureDataType.EMPTY,
            profile = null,
            shouldAnimateImage = true,
            imageUri = Uri.EMPTY,
            textData = "",
            isSamsungDevice = false,
            isEnabled = true,
            scrollState = scrollState,
            onTextContentReceived = { },
            onImageContentReceived = { },
            onColorPicked = { },
            onBack = { },
            onSelect = { },
            onEmpty = {}
        )
    }
}
