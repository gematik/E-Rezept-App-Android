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

package de.gematik.ti.erp.app.terms

import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.updateLayoutParams
import androidx.navigation.NavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.createWebViewClient

@Composable
fun DataProtectionScreen(navController: NavController) {
    Scaffold(
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Back,
                headline = stringResource(R.string.onb_data_consent),
            ) { navController.popBackStack() }
        }
    ) {
        TermsView(Modifier.fillMaxSize(), "file:///android_asset/data_terms.html")
    }
}

@Composable
fun TermsOfUseScreen(navController: NavController) {
    Scaffold(
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Back,
                headline = stringResource(R.string.onb_terms_of_use),
            ) { navController.popBackStack() }
        }
    ) {
        TermsView(Modifier.fillMaxSize(), "file:///android_asset/terms_of_use.html")
    }
}

@Composable
private fun TermsView(
    modifier: Modifier,
    url: String
) {
    val context = LocalContext.current
    val termsView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = false
            webViewClient = createWebViewClient()
        }
    }

    var size by remember { mutableStateOf(IntSize(100, 100)) }

    AndroidView(
        factory = {
            termsView.loadUrl(url)
            termsView
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
