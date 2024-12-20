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

package de.gematik.ti.erp.app.pharmacy.ui.components

import android.media.MediaPlayer
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.updateLayoutParams
import kotlin.math.max

/**
 * [MediaPlayer] backed video composable.
 *
 * @param aspectRatioOverwrite Prevents the delayed aspect ratio calculation of the video source. Defaults to `null`.
 * @param source Android resource
 */
@Composable
fun VideoContent(
    modifier: Modifier = Modifier,
    aspectRatioOverwrite: Float? = null,
    @RawRes source: Int
) {
    val context = LocalContext.current
    var aspectRatio by remember(aspectRatioOverwrite) {
        mutableStateOf(aspectRatioOverwrite ?: 0f)
    }
    val player = remember(source) {
        MediaPlayer().apply {
            setDataSource(context.resources.openRawResourceFd(source))
            setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)

            setOnVideoSizeChangedListener { mp, width, height ->
                if (aspectRatioOverwrite == null) {
                    aspectRatio = width / max(1f, height.toFloat())
                }
            }

            isLooping = true
        }
    }

    val surfaceCallback = remember(source) {
        object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                player.prepare()
                player.start()
                player.setDisplay(holder)
            }

            @Suppress("EmptyFunctionBlock")
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                player.stop()
                player.setDisplay(null)
            }
        }
    }

    var size by remember { mutableStateOf(IntSize(1, 1)) }
    Box {
        AndroidView(
            factory = { ctx ->
                val view = SurfaceView(ctx)

                view.holder.addCallback(surfaceCallback)

                view
            },
            modifier = modifier
                .then(
                    // prevent irritating large surfaces on first layout calc
                    if (aspectRatio == 0f) {
                        Modifier
                    } else {
                        Modifier.aspectRatio(aspectRatio)
                    }
                )
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
}
