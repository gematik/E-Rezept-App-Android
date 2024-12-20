/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.pulltorefresh.extensions

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import de.gematik.ti.erp.app.shared.REFRESH_DELAY
import kotlinx.coroutines.delay

@Suppress("ComposableNaming")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshState.trigger(
    onStartRefreshing: (() -> Unit)? = null,
    onNavigation: (() -> Unit)? = null,
    block: suspend () -> Unit
) {
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            onStartRefreshing?.invoke()
            delay(REFRESH_DELAY)
            block()
            onNavigation?.invoke() // can be done better
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun PullToRefreshState.triggerStart() {
    if (!isRefreshing) {
        startRefresh()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun PullToRefreshState.triggerEnd() {
    if (isRefreshing) {
        endRefresh()
    }
}
