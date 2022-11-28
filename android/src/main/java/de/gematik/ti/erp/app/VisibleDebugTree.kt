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

package de.gematik.ti.erp.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class VisibleDebugTree : Antilog() {
    val rotatingLog = MutableStateFlow<List<AnnotatedString>>(emptyList())

    override fun performLog(priority: LogLevel, tag: String?, throwable: Throwable?, message: String?) {
        rotatingLog.update {
            if (it.size > 500) {
                it.drop(10)
            } else {
                it
            } + buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(tag ?: "unknown")
                }
                append(" ")
                if (priority == LogLevel.ERROR) {
                    withStyle(SpanStyle(color = Color.Red)) {
                        append(message ?: "")
                    }
                } else {
                    append(message ?: "")
                }
                throwable?.run {
                    withStyle(SpanStyle(color = Color.Red)) {
                        append(throwable.message ?: "")
                    }
                }
            }
        }
    }
}
