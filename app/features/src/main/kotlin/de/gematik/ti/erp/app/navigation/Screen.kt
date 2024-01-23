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

package de.gematik.ti.erp.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.contentsquare.android.compose.analytics.TriggeredOnResume
import de.gematik.ti.erp.app.analytics.mapper.ContentSquareMapper
import io.github.aakira.napier.Napier

abstract class Screen {

    abstract val navController: NavController

    abstract val navBackStackEntry: NavBackStackEntry

    val trackerMapper: ContentSquareMapper = ContentSquareMapper()

    @Composable
    abstract fun Content()

    @Composable
    fun trackScreen(screenName: String?) {
        TriggeredOnResume {
            // TODO: Track everytime by checking if it exists
            Napier.d { "TODO: Tracking logger $screenName" }
            // Contentsquare.send(screenName)
        }
    }

    companion object {
        @Composable
        fun Screen.track(route: String): Screen {
            val routeEnum = when {
                route.contains("?") -> route.split(("?"))[0]
                else -> route
            }
            val trackedScreenName = trackerMapper.map(NavigationRouteNames.valueOf(routeEnum))
            trackScreen(trackedScreenName)
            return this
        }
    }
}
