/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.onboarding.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerShortMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall

@Composable
fun OnboardingScaffold(
    modifier: Modifier = Modifier,
    state: LazyListState,
    bottomBar: @Composable () -> Unit,
    content: LazyListScope.() -> Unit
) {
    Scaffold(
        modifier.systemBarsPadding(),
        bottomBar = bottomBar
    ) { innerPadding ->
        val contentPadding by derivedStateOf {
            PaddingValues(
                bottom = innerPadding.calculateBottomPadding(),
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium
            )
        }
        OnboardingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
            content = content,
            contentPadding = contentPadding
        )
    }
}

@Composable
fun OnboardingLazyColumn(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    state: LazyListState,
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        state = state,
        modifier = modifier.testTag(TestTag.Onboarding.ScreenContent),
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun OnboardingBottomBar(
    modifier: Modifier = Modifier,
    info: String?,
    buttonText: String,
    buttonEnabled: Boolean,
    buttonModifier: Modifier,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.neutral000)
            .padding(horizontal = PaddingDefaults.Medium)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpacerShortMedium()
        if (info != null) {
            Text(info, style = AppTheme.typography.caption1l, textAlign = TextAlign.Center)
            SpacerSmall()
        }
        PrimaryButtonSmall(
            modifier = buttonModifier,
            enabled = buttonEnabled,
            onClick = onButtonClick
        ) {
            Text(buttonText)
        }
        SpacerLarge()
    }
}
