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

@file:Suppress("UnusedPrivateMember")

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.MainActivity

@Composable
fun OutlinedDebugButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    error("Debug button should only be used in debug builds!")
}

fun Modifier.visualTestTag(tag: String) =
    this

@Composable
fun DebugOverlay(elements: Map<String, MainActivity.Element>) {
    error("Debug overlay should only be used in debug builds!")
}
