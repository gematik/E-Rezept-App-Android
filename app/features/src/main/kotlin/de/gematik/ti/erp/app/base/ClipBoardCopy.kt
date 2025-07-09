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

package de.gematik.ti.erp.app.base

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import io.github.aakira.napier.Napier

object ClipBoardCopy {
    fun copyToClipboard(
        context: Context,
        text: String
    ) {
        val manager = context.clipBoardManager
        Napier.i { "Clipboard \n $text" }
        val clip = ClipData.newPlainText("url", text)
        manager.setPrimaryClip(clip)
    }

    fun copyToClipboardWithHaptic(
        label: String,
        text: String,
        context: Context,
        hapticFeedback: HapticFeedback
    ) {
        val manager = context.clipBoardManager
        val clip = ClipData.newPlainText(label, text)
        manager.setPrimaryClip(clip)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}

val Context.clipBoardManager: ClipboardManager
    get() = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
