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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.webview

import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.updateLayoutParams
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.AppColors
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffoldWithScrollState
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import io.github.aakira.napier.Napier

const val URI_TERMS_OF_USE = "file:///android_asset/terms_of_use.html"

@Requirement(
    "O.Arch_8#1",
    "O.Plat_11#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Webviews containing local html without javascript. No cookies are created."
)
@Composable
fun WebViewScreen(
    modifier: Modifier = Modifier,
    title: String,
    url: String,
    navigationMode: NavigationBarMode = NavigationBarMode.Back,
    onBack: () -> Unit
) {
    var scrollState by remember { mutableIntStateOf(0) }
    AnimatedElevationScaffoldWithScrollState(
        modifier = modifier,
        topBarTitle = title,
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        elevated = scrollState > 0,
        navigationMode = navigationMode,
        bottomBar = {},
        actions = {},
        onBack = onBack
    ) {
        WebView(
            modifier = Modifier.fillMaxSize(),
            url = url,
            onScroll = { scrollState = it }
        )
    }
}

@Composable
private fun WebView(
    modifier: Modifier,
    url: String,
    onScroll: (y: Int) -> Unit
) {
    @Requirement(
        "O.Arch_8#2",
        "O.Plat_11#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Javascript is disabled on all webviews used in the app."
    )
    val context = LocalContext.current
    val appColors = AppTheme.colors
    val typo = MaterialTheme.typography
    val isLightTheme = MaterialTheme.colors.isLight

    val webView = remember {
        WebView(context).apply {
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.javaScriptEnabled = false
            settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
            setOnScrollChangeListener { _, _, scrollY, _, _ -> onScroll(scrollY) }
        }
    }

    webView.webViewClient = remember(isLightTheme, appColors, typo) {
        createWebViewClient()
    }

    LaunchedEffect(isLightTheme, appColors, url) {
        webView.setBackgroundColor(appColors.neutral025.toArgb())

        // Read the HTML file and inject CSS
        val assetPath = url.removePrefix("file:///android_asset/")
        val htmlContent = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        val css = generateCss(appColors, typo)

        // Replace the styles.css link with inline CSS (handle both self-closing and regular tags)
        val modifiedHtml = htmlContent
            .replace("""<link rel="stylesheet" href="styles.css" />""", """<style>$css</style>""")
            .replace("""<link rel="stylesheet" href="styles.css">""", """<style>$css</style>""")

        webView.loadDataWithBaseURL(
            "file:///android_asset/",
            modifiedHtml,
            "text/html",
            "UTF-8",
            null
        )
    }

    var size by remember { mutableStateOf(IntSize(100, 100)) }

    DisposableEffect(webView) {
        onDispose {
            webView.clearCache(true)
            webView.clearHistory()
            webView.destroy()
        }
    }

    AndroidView(
        factory = {
            webView.loadUrl(url)
            webView
        },
        modifier = modifier.onSizeChanged { size = it }
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

private fun generateCss(appColors: AppColors, typo: Typography): String {
    val primaryColor = appColors.neutral900.toCSS()
    val backgroundColor = appColors.neutral025.toCSS()

    Napier.d("Generating CSS - primaryColor: $primaryColor, backgroundColor: $backgroundColor")

    return """
        |${baseStyles(primaryColor, backgroundColor)}
        |${headingStyles(primaryColor, typo)}
        |${textStyles(primaryColor, typo)}
        |${listStyles(primaryColor)}
        |${tableStyles(appColors)}
        |${linkStyles(appColors)}
        |${licenseStyles(primaryColor)}
    """.trimMargin()
}

private fun baseStyles(primaryColor: String, backgroundColor: String) = """
    |* {
    |    overflow-wrap: break-word;
    |}
    |body {
    |    color: $primaryColor;
    |    background: $backgroundColor;
    |    padding: 16px;
    |}
""".trimMargin()

private fun headingStyles(primaryColor: String, typo: Typography) = """
    |h1, h2, h3, h4, h5, h6 {
    |    color: $primaryColor;
    |    font-weight: bold;
    |    padding-top: 0.5em;
    |    margin: 0;
    |}
    |h5 {
    |    font-weight: 600;
    |    display: flex;
    |    align-items: center;
    |}
    |${typoColor("h1", typo.h1)}
    |${typoColor("h2", typo.h2)}
    |${typoColor("h3", typo.h3)}
    |${typoColor("h4", typo.h4)}
    |${typoColor("h5", typo.h5)}
""".trimMargin()

private fun textStyles(primaryColor: String, typo: Typography) = """
    |p, li, th, td, caption, span, strong, em, small, label, dd, dt, blockquote, code, pre {
    |    color: $primaryColor;
    |}
    |${typoColor("p", typo.body1)}
""".trimMargin()

private fun listStyles(primaryColor: String) = """
    |ul {
    |    padding-inline-start: 16px;
    |}
    |li {
    |    color: $primaryColor;
    |    padding-bottom: 4px;
    |    font-family: ui-sans-serif;
    |    font-size: 17px;
    |    line-height: 24px;
    |    letter-spacing: -0.408px;
    |}
""".trimMargin()

private fun tableStyles(appColors: AppColors) = """
    |table, th, td {
    |    border-collapse: collapse;
    |    border: 0.1px solid ${appColors.neutral600.toCSS()};
    |}
    |th, td {
    |    padding: 0.5em;
    |}
""".trimMargin()

private fun linkStyles(appColors: AppColors) = """
    |a, a:link, a:visited {
    |    color: ${appColors.primary700.toCSS()};
    |    text-decoration: none;
    |    font-family: ui-sans-serif;
    |}
""".trimMargin()

private fun licenseStyles(primaryColor: String) = """
    |section.license {
    |    color: $primaryColor;
    |    font-size: 17px;
    |    line-height: 24px;
    |    font-style: italic;
    |}
""".trimMargin()

fun createWebViewClient() = object : WebViewClient() {

    @Requirement(
        "O.Plat_10#1",
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
