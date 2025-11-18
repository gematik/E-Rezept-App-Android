/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.eurezept.util

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

internal class TextToSpeechManager(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var currentLocale: Locale = Locale.ENGLISH

    fun initialize(
        preferredLocale: Locale = Locale.GERMANY,
        onReady: (() -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null
    ) {
        currentLocale = preferredLocale
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ready = configureEngine(preferredLocale)
                if (ready) onReady?.invoke() else onError?.invoke(IllegalStateException("TTS language/voice not ready"))
            } else {
                onError?.invoke(IllegalStateException("TTS init failed: $status"))
            }
        }.apply {
            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {}
                override fun onError(utteranceId: String?) {}
                override fun onError(utteranceId: String?, errorCode: Int) {}
            })
        }
    }

    private fun configureEngine(locale: Locale): Boolean {
        val engine = tts ?: return false

        // 1) Language availability
        val availability = engine.isLanguageAvailable(locale)
        val finalLocale = when {
            availability >= TextToSpeech.LANG_AVAILABLE -> locale
            engine.isLanguageAvailable(Locale.UK) >= TextToSpeech.LANG_AVAILABLE -> Locale.UK
            else -> Locale.ENGLISH
        }
        engine.language = finalLocale
        currentLocale = finalLocale

        // 2) Pick the best matching Voice for the locale
        val best = engine.voices
            ?.filter { it.locale.language == finalLocale.language }
            ?.sortedWith(
                compareByDescending<android.speech.tts.Voice> { it.quality }
                    .thenBy { it.latency }
                    .thenBy { it.name }
            )
            ?.firstOrNull()
        if (best != null) engine.voice = best

        // 3) Reasonable default prosody
        engine.setSpeechRate(0.9f) // slightly slower than default
        engine.setPitch(1.0f)

        return true
    }

    /**
     * Insert pauses only at natural boundaries:
     * - After sentence punctuation
     * - Around colons/semicolons
     * - Before long numbers or abbreviations (basic heuristics)
     */
    private fun humanizePauses(text: String, extraPause: Boolean): String {
        if (!extraPause) return text

        // Ensure terminal punctuation has a space; add short pauses via commas if missing
        var s = text
            .replace(Regex("\\s*([.!?])\\s*"), "$1 ") // sentence ends → "…! "
            .replace(Regex("\\s*([;:])\\s*"), "$1 ") // clause breaks
            .replace(Regex("\\s{2,}"), " ")
            .trim()

        // Add a gentle pause for long tokens (e.g., long IDs) by inserting a comma
        s = s.replace(Regex("(\\b\\w{8,}\\b)"), "$1,")
        return s
    }

    private fun speakInternal(
        text: String,
        queueMode: Int,
        speechRate: Float,
        pitch: Float,
        utteranceId: String
    ) {
        val engine = tts ?: return
        if (!ready) return

        engine.setSpeechRate(speechRate)
        engine.setPitch(pitch)

        val params = Bundle().apply {
            // Route as “assistant/accessibility” so it’s clear and not too quiet
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, android.media.AudioManager.STREAM_MUSIC)
        }
        // Stronger hint for usage type (affects ducking/focus)
        engine.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        engine.speak(text, queueMode, params, utteranceId.ifEmpty { System.nanoTime().toString() })
    }

    fun setLanguage(locale: Locale): Boolean {
        currentLocale = locale
        if (!ready) return false
        return configureEngine(locale)
    }

    fun speak(
        text: String,
        speechRate: Float = 0.9f,
        pitch: Float = 1.0f,
        utteranceId: String = "",
        addPauses: Boolean = true
    ) {
        val processed = humanizePauses(text, addPauses)
        speakInternal(processed, TextToSpeech.QUEUE_FLUSH, speechRate, pitch, utteranceId)
    }

    fun speakQueued(
        text: String,
        speechRate: Float = 0.9f,
        pitch: Float = 1.0f,
        utteranceId: String = "",
        addPauses: Boolean = true
    ) {
        val processed = humanizePauses(text, addPauses)
        speakInternal(processed, TextToSpeech.QUEUE_ADD, speechRate, pitch, utteranceId)
    }

    fun speakWithLocale(
        text: String,
        locale: Locale,
        speechRate: Float = 0.9f,
        pitch: Float = 1.0f,
        utteranceId: String = "",
        addPauses: Boolean = true,
        spellOutCharacters: Boolean = false
    ) {
        if (!ready) return
        setLanguage(locale)
        val toSpeak = if (spellOutCharacters) text.toCharArray().joinToString(" ") else text
        speak(toSpeak, speechRate, pitch, utteranceId, addPauses)
    }

    fun speakQueuedWithLocale(
        text: String,
        locale: Locale,
        speechRate: Float = 0.9f,
        pitch: Float = 1.0f,
        utteranceId: String = "",
        addPauses: Boolean = true,
        spellOutCharacters: Boolean = false
    ) {
        if (!ready) return
        setLanguage(locale)
        val toSpeak = if (spellOutCharacters) text.toCharArray().joinToString(" ") else text
        speakQueued(toSpeak, speechRate, pitch, utteranceId, addPauses)
    }

    fun cleanup() {
        ready = false
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
