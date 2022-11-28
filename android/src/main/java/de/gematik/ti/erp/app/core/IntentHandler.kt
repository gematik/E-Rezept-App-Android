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

package de.gematik.ti.erp.app.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import java.net.URI

const val FastTrackBaseUri = "https://das-e-rezept-fuer-deutschland.de/extauth"
const val ShareBaseUri = "https://das-e-rezept-fuer-deutschland.de/prescription"

@Stable
class IntentHandler(private val context: Context) {
    private val extAuthChannel = Channel<String>(Channel.CONFLATED)
    private val shareChannel = Channel<String>(Channel.CONFLATED)

    val extAuthIntent = extAuthChannel.receiveAsFlow()

    val shareIntent = shareChannel.receiveAsFlow()

    suspend fun propagateIntent(intent: Intent) {
        intent.data?.let {
            val value = it.toString()
            Napier.d("Received new intent: $value")

            when {
                value.startsWith(FastTrackBaseUri) ->
                    extAuthChannel.send(value)
                value.startsWith(ShareBaseUri) ->
                    shareChannel.send(value)
            }
        }
    }

    fun startFastTrackApp(redirect: URI) {
        clear() // clear possible cached values
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(redirect.toString())))
    }

    private fun clear() {
        extAuthChannel.tryReceive()
        shareChannel.tryReceive()
    }
}

val LocalIntentHandler =
    staticCompositionLocalOf<IntentHandler> { error("No intent handler provided!") }
