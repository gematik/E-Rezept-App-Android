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

package de.gematik.ti.erp.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.AlertDialog
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import kotlinx.coroutines.launch

@Composable
fun DebugLoadingButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    onClick: suspend () -> Unit
) {
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = {
                errorMessage = null
            },
            buttons = {
                Button(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            },
            text = {
                Text(error)
            }
        )
    }
    val scope = rememberCoroutineScope()

    Button(
        modifier = modifier.fillMaxWidth(),
        onClick = {
            loading = true
            scope.launch {
                try {
                    onClick()
                } catch (e: Exception) {
                    errorMessage = e.message + (e.cause?.message?.let { " - cause: $it" } ?: "")
                } finally {
                    loading = false
                }
            }
        },
        enabled = enabled && !loading
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = AppTheme.colors.neutral600)
            SpacerSmall()
        }
        Text(text, textAlign = TextAlign.Center)
    }
}
