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

package de.gematik.ti.erp.app.settings.ui

import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.createWebViewClient

@Composable
fun Licences(navController: NavController) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.javaScriptEnabled = false
            webViewClient = createWebViewClient()
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.clearCache(true)
            webView.clearHistory()
        }
    }

    Scaffold(
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Back,
                headline = stringResource(R.string.settings_legal_licences),
            ) { navController.popBackStack() }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            AndroidView(
                factory = {
                    webView.loadUrl("file:///android_asset/open_source_licenses.html")
                    webView
                }
            )
        }
    }
}
