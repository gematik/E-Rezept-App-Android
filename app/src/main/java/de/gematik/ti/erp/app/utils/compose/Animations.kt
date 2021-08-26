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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.collect

enum class NavigationMode {
    Forward,
    Back,
    Open
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationAnimation(
    mode: NavigationMode = NavigationMode.Forward,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    val transition = when (mode) {
        NavigationMode.Forward -> slideInHorizontally(initialOffsetX = { it / 2 })
        NavigationMode.Back -> slideInHorizontally(initialOffsetX = { -it / 2 })
        NavigationMode.Open -> slideInVertically(initialOffsetY = { it / 2 })
    }
    AnimatedVisibility(
        visibleState = remember { MutableTransitionState(false) }.apply { targetState = true },
        modifier = modifier,
        enter = transition,
        exit = ExitTransition.None,
        content = content
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavHostController.navigationModeState(
    startDestination: String
): State<NavigationMode> {
    var prevNumOfEntries by rememberSaveable(this, startDestination) { mutableStateOf(-1) }

    return produceState(NavigationMode.Open) {
        this@navigationModeState.currentBackStackEntryFlow.collect {
            value = when {
                prevNumOfEntries == -1 && it.destination.route == startDestination -> NavigationMode.Open
                this@navigationModeState.backQueue.size < prevNumOfEntries -> NavigationMode.Back
                this@navigationModeState.backQueue.size > prevNumOfEntries -> NavigationMode.Forward
                else -> NavigationMode.Open
            }

            prevNumOfEntries = this@navigationModeState.backQueue.size
        }
    }
}
