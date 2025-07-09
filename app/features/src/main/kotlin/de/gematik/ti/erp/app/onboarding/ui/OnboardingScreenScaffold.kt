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

package de.gematik.ti.erp.app.onboarding.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.theme.PaddingDefaults

@Composable
fun OnboardingScreenScaffold(
    modifier: Modifier = Modifier,
    state: LazyListState,
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable () -> Unit,
    content: LazyListScope.() -> Unit
) {
    Scaffold(
        modifier.systemBarsPadding(),
        topBar = topBar ?: {},
        bottomBar = bottomBar
    ) { innerPadding ->
        val contentPadding by remember {
            derivedStateOf {
                PaddingValues(
                    bottom = innerPadding.calculateBottomPadding(),
                    start = PaddingDefaults.Medium,
                    end = PaddingDefaults.Medium
                )
            }
        }
        OnboardingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
            content = content,
            contentPadding = contentPadding
        )
    }
}
