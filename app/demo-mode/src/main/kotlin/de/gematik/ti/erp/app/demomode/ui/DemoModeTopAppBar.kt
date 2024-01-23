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

package de.gematik.ti.erp.app.demomode.ui

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import de.gematik.ti.erp.app.demomode.DemoModeObserver

@Suppress("ComposableNaming")
@Composable
fun checkForDemoMode(
    demoModeStatusBarColor: Color,
    demoModeContent: @Composable ColumnScope.() -> Unit,
    appContent: @Composable ColumnScope.() -> Unit
) {
    val demoModeObserver = LocalContext.current.getDemoModeObserver()
    val isDemoMode = demoModeObserver?.isDemoMode() ?: false
    val systemUiController = rememberSystemUiController()

    Column(
        modifier = Modifier.fillMaxWidth().statusBarsPadding()
    ) {
        if (isDemoMode) {
            SideEffect {
                systemUiController.setStatusBarColor(demoModeStatusBarColor)
            }
            demoModeContent()
        }
        appContent()
    }
}

@Composable
fun DemoModeStatusBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    textColor: Color,
    demoModeActiveText: String,
    demoModeEndText: String,
    onClickDemoModeEnd: () -> Unit
) {
    Row(
        modifier = Modifier
            .then(modifier)
            .background(backgroundColor),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = demoModeActiveText,
            color = textColor
        )
        TextButton(onClick = onClickDemoModeEnd) {
            Text(
                text = demoModeEndText,
                color = textColor,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun Context.getActivity(): AppCompatActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is AppCompatActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

internal fun Context.getDemoModeObserver(): DemoModeObserver? = getActivity() as? DemoModeObserver
