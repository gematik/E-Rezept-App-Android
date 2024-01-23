/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.webview

import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.updateLayoutParams
import androidx.webkit.WebViewAssetLoader
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode

const val URI_TERMS_OF_USE = "file:///android_asset/terms_of_use.html"
const val URI_DATA_TERMS = "file:///android_asset/data_terms.html"

@Requirement(
    "O.Arch_8#1",
    "O.Plat_11#1",
    "O.Plat_14",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Webview containing local html without javascript. No cookies are created."
)
@Composable
fun WebViewScreen(
    modifier: Modifier = Modifier,
    title: String,
    url: String,
    navigationMode: NavigationBarMode = NavigationBarMode.Back,
    onBack: () -> Unit
) {
    var scrollState by remember { mutableStateOf(0) }
    AnimatedElevationScaffold(
        elevated = scrollState > 0,
        navigationMode = navigationMode,
        bottomBar = {},
        actions = {},
        topBarTitle = title,
        onBack = onBack,
        modifier = modifier
    ) {
        WebView(Modifier.fillMaxSize(), url, onScroll = { scrollState = it })
    }
}

@Composable
private fun WebView(
    modifier: Modifier,
    url: String,
    onScroll: (y: Int) -> Unit
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colors
    val typo = MaterialTheme.typography
    val webView = remember(colors, typo) {
        WebView(context).apply {
            setBackgroundColor(colors.background.toArgb())
            settings.javaScriptEnabled = false
            setOnScrollChangeListener { _, _, scrollY, _, _ -> onScroll(scrollY) }
            webViewClient = createWebViewClient(colors, typo)
        }
    }

    var size by remember { mutableStateOf(IntSize(100, 100)) }

    DisposableEffect(webView) {
        onDispose {
            webView.clearCache(true)
            webView.clearHistory()
        }
    }

    AndroidView(
        factory = {
            webView.loadUrl(url)
            webView
        },
        modifier = modifier
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

private fun TextUnit.toCSS(): String {
    val unit = when (this.type) {
        TextUnitType.Sp -> "sp"
        TextUnitType.Em -> "em"
        else -> "px"
    }
    return "${this.value}$unit"
}

private const val MaxColorIntValue = 255

private fun Float.toIntColor() = (this * MaxColorIntValue).toInt()

private fun Color.toCSS(): String =
    "rgba(${this.red.toIntColor()}, ${this.green.toIntColor()}, ${this.blue.toIntColor()}, ${this.alpha})"

private fun typoColor(tag: String, style: TextStyle): String =
    """
    |$tag {
    |    color: inherit;
    |    font-size: ${style.fontSize.toCSS()};
    |    font-weight: ${style.fontWeight?.weight ?: FontWeight.Medium.weight};
    |    line-height: ${style.lineHeight.toCSS()};
    |    letter-spacing: ${style.letterSpacing.toCSS()};
    |}
    """.trimMargin()

fun createWebViewClient(colors: Colors, typo: Typography) = object : WebViewClient() {
    private val css = """
    |body {
    |    color: ${colors.onBackground.toCSS()};
    |    background: ${colors.background.toCSS()};
    |    padding: 16px;
    |    word-wrap: break-word;
    |}
    |li {
    |    padding-bottom: 4px;
    |}
    |h1, h2, h3, h4 {
    |   padding-top: 0.5em;
    |   margin: 0;
    |}
    |${typoColor("h1", typo.h1)}
    |${typoColor("h2", typo.h2)}
    |${typoColor("h3", typo.h3)}
    |${typoColor("h4", typo.h4)}
    |${typoColor("p", typo.body1)}
    |table, th, td {
    |   border-collapse: collapse;
    |   border: 0.1px solid ${colors.onSurface.toCSS()};
    |}
    |th, td {
    |   padding: 0.5em;
    |}
    |a, a:link, a:visited {
    |    color: ${colors.primary.toCSS()};
    |    text-decoration: none;
    |}
    """.trimMargin()

    private val cssLoader = WebViewAssetLoader.Builder()
        .setDomain("localhost")
        .addPathHandler("/style/") {
            WebResourceResponse("text/css", "UTF-8", css.byteInputStream())
        }
        .build()

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        return cssLoader.shouldInterceptRequest(request.url)
    }

    @Requirement(
        "O.Plat_13",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Disables unused schemes"
    )
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val isAllowedScheme = request.url.scheme == "https" || request.url.scheme == "mailto"
        return if (isAllowedScheme && request.url.host != "localhost") {
            view.context.startActivity(Intent(Intent.ACTION_VIEW, request.url))
            true
        } else {
            false
        }
    }
}
