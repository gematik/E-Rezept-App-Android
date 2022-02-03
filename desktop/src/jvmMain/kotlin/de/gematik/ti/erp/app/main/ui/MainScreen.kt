/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.main.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.google.zxing.common.BitMatrix
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.common.App
import de.gematik.ti.erp.app.common.ClosablePopupScaffold
import de.gematik.ti.erp.app.common.OverlayPopup
import de.gematik.ti.erp.app.common.SpacerLarge
import de.gematik.ti.erp.app.common.SpacerMedium
import de.gematik.ti.erp.app.common.SpacerSmall
import de.gematik.ti.erp.app.common.theme.AppTheme
import de.gematik.ti.erp.app.common.theme.PaddingDefaults
import de.gematik.ti.erp.app.login.ui.LoginWithHealthCard
import de.gematik.ti.erp.app.login.ui.LoginWithHealthCardViewModel
import de.gematik.ti.erp.app.navigation.ui.Destination
import de.gematik.ti.erp.app.navigation.ui.Navigation
import de.gematik.ti.erp.app.prescription.usecase.createMatrixCode
import de.gematik.ti.erp.app.rememberScope
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ImageInfo
import org.kodein.di.bind
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.subDI
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton
import kotlin.math.roundToInt

object MainNavigation {
    // marker interfaces providing some context
    interface Onboarding : Destination
    interface Popup : Destination
    interface Prescriptions : Destination
    interface Communications : Destination

    // onboarding
    object Welcome : Onboarding
    object Login : Onboarding, Popup

    // prescriptions
    object PrescriptionsRedeemed : Prescriptions
    object PrescriptionsUnredeemed : Prescriptions
    data class DataMatrixCode(val taskId: String, val accessCode: String) : Prescriptions, Popup

    // communications
    object PharmacyCommunications : Communications

    object Protocol : Destination
}

@Composable
fun MainScreen(
    mainViewModel: MainScreenViewModel,
    navigation: Navigation
) {
    val showPopup = navigation.currentBackStackEntry is MainNavigation.Popup

    OverlayPopup(
        showPopup,
        popupContent = {
            val destination = remember { navigation.currentBackStackEntry }
            when (destination) {
                is MainNavigation.Login -> LoginWithHealthCard(navigation)
                is MainNavigation.DataMatrixCode -> DataMatrixCode(
                    navigation,
                    taskId = destination.taskId,
                    accessCode = destination.accessCode
                )
            }
        }
    ) {
        when (navigation.currentBackStackEntry) {
            is MainNavigation.Onboarding ->
                InitialWelcomeScreen(onClickShowLogin = { navigation.navigate(MainNavigation.Login) })
            is MainNavigation.Protocol,
            is MainNavigation.Communications,
            is MainNavigation.Prescriptions ->
                LoggedInScreen(
                    mainViewModel = mainViewModel,
                    navigation = navigation
                )
        }
    }
}

@Composable
private fun DataMatrixCode(
    navigation: Navigation,
    taskId: String,
    accessCode: String,
) {
    ClosablePopupScaffold(onClose = { navigation.back() }) {
        Box(Modifier.fillMaxSize()) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(PaddingDefaults.Medium)
                    .align(Alignment.Center)

            ) {
                DataMatrixCode(
                    taskId = taskId,
                    accessCode = accessCode,
                    modifier = Modifier.aspectRatio(1f)
                )
            }
        }
    }
}

@Composable
private fun DataMatrixCode(
    taskId: String,
    accessCode: String,
    modifier: Modifier
) {
    val matrix = remember { createMatrixCode(taskId, accessCode) }
    Box(
        modifier = Modifier
            .then(modifier)
            .background(Color.White)
            .padding(PaddingDefaults.Medium)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    onDrawBehind {
                        drawImage(
                            matrix.toImageBitmap(),
                            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                            filterQuality = FilterQuality.None
                        )
                    }
                }
        )
    }
}

private fun BitMatrix.toImageBitmap(): ImageBitmap {
    val bytesPerPixel = 4
    val pixels = ByteArray(width * height * bytesPerPixel)

    var i = 0
    for (y in 0 until height) {
        for (x in 0 until width) {
            val rgb = (if (get(x, y)) 0x00 else 0xff).toByte()
            pixels[i++] = rgb // b
            pixels[i++] = rgb // g
            pixels[i++] = rgb // r
            pixels[i++] = 0xff.toByte() // a
        }
    }

    val bitmap = Bitmap()
    bitmap.allocPixels(ImageInfo.makeS32(width, height, ColorAlphaType.UNPREMUL))
    bitmap.installPixels(pixels)
    return bitmap.asComposeImageBitmap()
}

@Composable
private fun LoginWithHealthCard(
    navigation: Navigation
) {
    val scope = rememberScope()

    subDI(diBuilder = {
        bind {
            scoped(scope).singleton {
                LoginWithHealthCardViewModel(instance(), instance(), instance())
            }
        }
    }) {
        val loginWithHealthCardViewModel by rememberInstance<LoginWithHealthCardViewModel>()

        LoginWithHealthCard(
            loginWithHealthCardViewModel,
            onFinished = { navigation.navigate(MainNavigation.PrescriptionsUnredeemed) },
            onClose = { navigation.back() }
        )
    }
}

@Composable
fun Logo(
    modifier: Modifier = Modifier
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val colorFilter = if (MaterialTheme.colors.isLight) null else ColorFilter.tint(AppTheme.colors.neutral800)
        Image(
            painterResource("images/ic_onboarding_logo_flag.xml"),
            null,
            modifier = Modifier.height(16.dp).wrapContentWidth(),
            contentScale = ContentScale.FillHeight,
            colorFilter = colorFilter
        )
        SpacerSmall()
        Image(
            painterResource("images/ic_onboarding_logo_gematik.xml"),
            null,
            modifier = Modifier.height(32.dp).wrapContentWidth(),
            contentScale = ContentScale.FillHeight,
            colorFilter = colorFilter
        )
    }
}

@Composable
fun InitialWelcomeScreen(
    onClickShowLogin: () -> Unit
) {
    Scaffold(
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(Modifier.fillMaxSize()) {
            Logo(
                Modifier.padding(top = 48.dp, start = 96.dp).height(32.dp),
            )
            Spacer(Modifier.weight(1f))
            Column(Modifier.align(Alignment.CenterHorizontally), horizontalAlignment = Alignment.CenterHorizontally) {
                if (BuildKonfig.INTERNAL) {
                    Image(
                        painterResource("images/erp_logo_dev.webp"),
                        null,
                        modifier = Modifier.size(100.dp)
                    )
                    SpacerMedium()
                }
                Text(App.strings.desktopMainWelcomeTitle(), style = MaterialTheme.typography.h5)
                SpacerSmall()
                Text(App.strings.desktopMainWelcomeSubtitle(), style = MaterialTheme.typography.body1)
                SpacerLarge()
                Button(onClick = onClickShowLogin) {
                    Text(App.strings.desktopMainLoginWithHealthcard())
                }
            }
            Spacer(Modifier.weight(1f))
            Image(
                painterResource("images/crew.webp"), null,
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}
