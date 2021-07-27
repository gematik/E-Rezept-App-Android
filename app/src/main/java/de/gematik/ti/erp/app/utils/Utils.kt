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

package de.gematik.ti.erp.app.utils

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Date

fun Date.convertFhirDateToOffsetDateTime(offset: ZoneOffset = ZoneOffset.UTC): OffsetDateTime {
    val offsetDateTime = this.toInstant().atOffset(offset)
    return offsetDateTime.truncatedTo(ChronoUnit.SECONDS)
}

fun Date.convertFhirDateToLocalDate(): LocalDate {
    return this.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}
fun Date.convertFhirDateToLocalDateTime(): LocalDateTime {
    return this.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}

fun createWebViewClient() = object : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        return when {
            url.startsWith("https://") -> {
                view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                true
            }
            url.startsWith("#") -> {
                view.loadUrl(url)
                true
            }
            else -> {
                false
            }
        }
    }
}
