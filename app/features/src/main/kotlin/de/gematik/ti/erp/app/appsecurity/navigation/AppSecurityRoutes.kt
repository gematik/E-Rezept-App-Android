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

package de.gematik.ti.erp.app.appsecurity.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import de.gematik.ti.erp.app.navigation.NavigationRouteNames
import de.gematik.ti.erp.app.navigation.NavigationRoutes
import de.gematik.ti.erp.app.navigation.Routes

object AppSecurityRoutes : NavigationRoutes {

    override fun subGraphName() = "app_security"

    const val IntegrityWarningScreenArgument = "isDeviceRiskAccepted"

    object DeviceCheckLoadingScreen : Routes(NavigationRouteNames.DeviceCheckLoadingScreen.name)
    object InsecureDeviceScreen : Routes(NavigationRouteNames.InsecureDeviceScreen.name)

    object IntegrityWarningScreen : Routes(
        path = NavigationRouteNames.IntegrityWarningScreen.name,
        navArgument(IntegrityWarningScreenArgument) { type = NavType.StringType }
    ) {
        fun path(appSecurityResult: String) =
            path(IntegrityWarningScreenArgument to appSecurityResult)
    }
}
