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

package de.gematik.ti.erp.app.navigation

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.core.IntentHandler
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbarScaffold
import kotlinx.coroutines.CoroutineScope

/**
 * Screen is an abstract class that is used to as a screen object that is used
 * for navigation.
 * [navController] is required to navigate to other screens
 * [navBackStackEntry] is required to obtain args that we might get that is
 * required for the screens
 * Method [Content] is the one used to the host the composable and show the content
 * The internal method [de.gematik.ti.erp.app.analytics.tracker.Tracker.computeScreenTrackingProperty] is
 * used to track the screen by used the route
 * variable that is used for accessing the Screen
 */
@Suppress("UnnecessaryAbstractClass")
abstract class Screen {

    abstract val navController: NavController

    abstract val navBackStackEntry: NavBackStackEntry

    val context: Context
        @Composable
        get() = LocalContext.current

    val dialog: DialogScaffold
        @Composable
        get() = LocalDialog.current

    val snackbar: SnackbarHostState
        @Composable
        get() = LocalSnackbarScaffold.current

    val listState: LazyListState
        @Composable
        get() = rememberLazyListState()

    @OptIn(ExperimentalMaterial3Api::class)
    val pullToRefreshState: PullToRefreshState
        @Composable
        get() = rememberPullToRefreshState()

    val uiScope: CoroutineScope
        @Composable
        get() = rememberCoroutineScope()

    val intentHandler: IntentHandler
        @Composable
        get() = LocalIntentHandler.current

    /**
     * The composable content that is shown in the screen should be placed in [Content]
     */
    @Suppress("LongMethod")
    @Composable
    abstract fun Content()
}
