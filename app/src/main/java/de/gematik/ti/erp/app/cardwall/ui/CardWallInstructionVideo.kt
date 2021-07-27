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

package de.gematik.ti.erp.app.cardwall.ui

import android.media.MediaPlayer
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.ClosedCaptionDisabled
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.updateLayoutParams
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.utils.compose.Spacer16

private val captionLanguageMapping = listOf(
    "de" to R.raw.subtitles_cdw_instruction_de,
    "en" to R.raw.subtitles_cdw_instruction_en,
    "tr" to R.raw.subtitles_cdw_instruction_tr
)

@Composable
fun InstructionVideo() {
    val context = LocalContext.current
    val config = LocalConfiguration.current

    var caption by remember { mutableStateOf("") }
    var aspect by remember { mutableStateOf(1.0f) }
    val player = remember {
        MediaPlayer().apply {
            setOnTimedTextListener { mp, text ->
                caption = text.text.trim()
            }

            setDataSource(context.resources.openRawResourceFd(R.raw.animation_cdw_instruction))
            setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)

            setOnVideoSizeChangedListener { _, width, height ->
                aspect = (width.toFloat() / height).takeIf { !it.isNaN() } ?: 1f
            }

            isLooping = true

            captionLanguageMapping.forEach { (_, id) ->
                val fd = context.resources.openRawResourceFd(id)
                addTimedTextSource(fd.fileDescriptor, fd.startOffset, fd.length, "application/x-subrip")
            }
        }
    }

    LaunchedEffect(player, config) {
        // first entry is preferred language
        val displayLang = config.locales[0].language
        val captionSuffix = when {
            displayLang.startsWith("en") -> "en"
            displayLang.startsWith("de") -> "de"
            displayLang.startsWith("tr") -> "tr"
            else -> "en"
        }

        val textTrackIndex = player.trackInfo.indexOfFirst { it.trackType == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT }
        player.selectTrack(textTrackIndex + captionLanguageMapping.indexOfFirst { it.first == captionSuffix })
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

    var size by remember { mutableStateOf(IntSize(100, 100)) }

    var showCaption by remember { mutableStateOf(true) }

    Column {
        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))) {
            AndroidView(
                factory = { ctx ->
                    val view = SurfaceView(ctx)

                    view.holder.addCallback(surfaceCallback)

                    view
                },
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(aspect)
                    .onSizeChanged {
                        size = it
                    }
            ) {
                it.updateLayoutParams {
                    this.height = size.width
                    this.width = size.height
                }
            }

            if (showCaption) {
                Surface(
                    contentColor = Color.White,
                    color = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .wrapContentHeight()
                        .fillMaxWidth()
                ) {
                    Text(caption, style = MaterialTheme.typography.subtitle1, textAlign = TextAlign.Center)
                }
            }
        }

        IconToggleButton(
            checked = showCaption,
            onCheckedChange = { showCaption = it },
            modifier = Modifier.align(Alignment.End)
        ) {
            when (showCaption) {
                true -> Icon(Icons.Rounded.ClosedCaption, null)
                false -> Icon(Icons.Rounded.ClosedCaptionDisabled, null)
            }
        }
        Spacer16()
    }
}
