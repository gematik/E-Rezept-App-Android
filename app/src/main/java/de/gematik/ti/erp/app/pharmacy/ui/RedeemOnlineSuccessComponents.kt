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

package de.gematik.ti.erp.app.pharmacy.ui

import android.media.MediaPlayer
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.updateLayoutParams
import androidx.navigation.NavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import java.util.Locale

@Composable
fun RedeemOnlineSuccess(
    redeemOption: Int?,
    fragmentNavController: NavController
) {
    BackHandler {
        fragmentNavController.popBackStack()
    }
    Scaffold(
        bottomBar = {
            BottomButton(
                fragmentNavController = fragmentNavController
            )
        }
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.padding(PaddingDefaults.Medium)) {
                when (redeemOption) {
                    0 -> PickupAndCourier(
                        R.raw.animation_local,
                        stringResource(id = R.string.redeem_online_local_success_message)
                    )
                    1 -> MailDelivery(R.raw.animation_mail)
                    2 -> PickupAndCourier(
                        R.raw.animation_courier,
                        stringResource(id = R.string.redeem_online_courier_success_message)
                    )
                }
            }
        }
    }
}

@Composable
fun PickupAndCourier(source: Int, message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .clip(CircleShape)
        ) {
            VideoContent(source)
        }
        Text(
            text = stringResource(id = R.string.redeem_online_success_header),
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MailDelivery(source: Int) {
    Column {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp)
                .clip(CircleShape)
        ) {
            VideoContent(source)
        }
        Text(
            text = stringResource(id = R.string.redeem_online_mail_success_header),
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center
        )

        Spacer16()

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberedCircle(nr = 1, tint = AppTheme.colors.neutral600)
            Spacer16()
            Text(
                text = stringResource(id = R.string.redeem_online_mail_success_step1),
                modifier = Modifier.padding(end = 16.dp),
                style = MaterialTheme.typography.body1
            )
        }
        Spacer16()
        Row {
            NumberedCircle(nr = 2, tint = AppTheme.colors.neutral600)
            Spacer16()
            Text(
                text = stringResource(id = R.string.redeem_online_mail_success_step2),
                modifier = Modifier.padding(end = 16.dp),
                style = MaterialTheme.typography.body1
            )
        }
        Spacer16()
        Row {
            NumberedCircle(nr = 3, tint = AppTheme.colors.neutral600)
            Spacer16()
            Text(
                text = stringResource(id = R.string.redeem_online_mail_success_step3),
                modifier = Modifier.padding(end = 16.dp),
                style = MaterialTheme.typography.body1
            )
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun BottomButton(modifier: Modifier = Modifier, fragmentNavController: NavController) {
    BottomAppBar(modifier = modifier, backgroundColor = MaterialTheme.colors.surface) {
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { fragmentNavController.popBackStack() }
        ) {
            Text(text = stringResource(id = R.string.redeem_online_back_home).uppercase(Locale.getDefault()))
        }
        SpacerSmall()
    }
}

@Composable
fun VideoContent(source: Int) {
    val context = LocalContext.current
    var aspect by remember { mutableStateOf(1.0f) }
    val player = remember {
        MediaPlayer().apply {

            setDataSource(context.resources.openRawResourceFd(source))
            setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)

            setOnVideoSizeChangedListener { _, width, height ->
                aspect = width.toFloat() / height
            }

            isLooping = true
        }
    }

    val surfaceCallback = remember {
        object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                player.prepare()
                player.start()
                player.setDisplay(holder)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                player.stop()
                player.setDisplay(null)
            }
        }
    }

    var size by remember { mutableStateOf(IntSize(800, 800)) }
    AndroidView(
        factory = { ctx ->
            val view = SurfaceView(ctx)

            view.holder.addCallback(surfaceCallback)

            view
        },
        modifier = Modifier
            .onSizeChanged {
                size = it
            }
    ) {
        it.updateLayoutParams {
            this.height = size.width
            this.width = size.height
        }
    }
}

@Composable
private fun NumberedCircle(nr: Int, tint: Color, modifier: Modifier = Modifier) =
    Icon(
        when (nr) {
            1 -> painterResource(R.drawable.ic_step_1)
            2 -> painterResource(R.drawable.ic_step_2)
            3 -> painterResource(R.drawable.ic_step_3)
            else -> painterResource(R.drawable.ic_step_1)
        },
        null, modifier = modifier, tint = tint
    )
