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

/**
 * Screen is an abstract class that is used to as a screen object that is used
 * for navigation.
 * @param navController is required to navigate to other screens
 * @param navBackStackEntry is required to obtain args that we might get that is
 * required for the screens
 * Method [Content] is the one used to the host the composable and show the content
 * The internal method [computeScreenTrackingProperty] is used to track the screen by used the route
 * variable that is used for accessing the Screen
 */
@Suppress("UnnecessaryAbstractClass")
abstract class Screen {

    abstract val navController: NavController

    abstract val navBackStackEntry: NavBackStackEntry

    /**
     * The composable content that is shown in the screen should be placed in [Content]
     */
    @Composable
    abstract fun Content()
}
