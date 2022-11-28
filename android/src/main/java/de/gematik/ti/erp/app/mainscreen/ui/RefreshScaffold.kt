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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import de.gematik.ti.erp.app.prescription.ui.rememberRefreshPrescriptionsController
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SpinnerDelay = 300L

@Composable
fun RefreshScaffold(
    profileId: ProfileIdentifier,
    onUserNotAuthenticated: () -> Unit,
    mainScreenViewModel: MainScreenViewModel,
    onShowCardWall: () -> Unit,
    content: @Composable (onRefresh: (isUserAction: Boolean, priority: MutatePriority) -> Unit) -> Unit
) {
    val scope = rememberCoroutineScope()
    val mutex = MutatorMutex()

    val refreshPrescriptionsController = rememberRefreshPrescriptionsController(mainScreenViewModel)

    val isRefreshing by refreshPrescriptionsController.isRefreshing
    val refreshState = rememberSwipeRefreshState(isRefreshing)

    suspend fun refresh(
        isUserAction: Boolean,
        profileId: ProfileIdentifier,
        priority: MutatePriority = MutatePriority.Default
    ) {
        if (refreshState.isRefreshing) {
            return
        }
        mutex.mutate(priority) {
            refreshState.isRefreshing = true
            delay(SpinnerDelay) // required for the spinner

            refreshPrescriptionsController.refresh(
                profileId = profileId,
                isUserAction = isUserAction,
                onUserNotAuthenticated = onUserNotAuthenticated,
                onShowCardWall = {
                    if (isUserAction) {
                        scope.launch(Dispatchers.Main) {
                            onShowCardWall()
                        }
                    }
                }
            )
        }
    }

    LaunchedEffect(profileId) {
        // refresh on a profile change
        refresh(isUserAction = false, profileId = profileId)
    }

    SwipeRefresh(
        state = refreshState,
        modifier = Modifier.fillMaxSize(),
        onRefresh = {
            scope.launch { refresh(isUserAction = true, priority = MutatePriority.UserInput, profileId = profileId) }
        },
        indicator = { s, trigger ->
            SwipeRefreshIndicator(
                state = s,
                refreshTriggerDistance = trigger,
                contentColor = AppTheme.colors.primary600
            )
        },
        swipeEnabled = true
    ) {
        content { isUserAction, priority ->
            scope.launch { refresh(isUserAction = isUserAction, priority = priority, profileId = profileId) }
        }
    }
}
