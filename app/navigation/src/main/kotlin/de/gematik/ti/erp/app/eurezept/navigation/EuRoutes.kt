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

package de.gematik.ti.erp.app.eurezept.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import de.gematik.ti.erp.app.navigation.NavigationRouteNames
import de.gematik.ti.erp.app.navigation.NavigationRoutes
import de.gematik.ti.erp.app.navigation.Routes

object EuRoutes : NavigationRoutes {
    override fun subGraphName(): String = "eu"

    const val EU_NAV_TASK_ID = "taskId"
    const val EU_NAV_REDEEM_BUTTON = "euShowRedeemButton"

    object EuConsentScreen : Routes(
        NavigationRouteNames.EuConsentScreen.name,
        navArgument(EU_NAV_TASK_ID) {
            type = NavType.StringType
            defaultValue = ""
            nullable = true
        }
    ) {
        fun path(taskId: String? = null) = if (taskId.isNullOrEmpty()) {
            route
        } else {
            path(EU_NAV_TASK_ID to taskId)
        }
    }

    object EuRedeemScreen : Routes(
        NavigationRouteNames.EuRedeemScreen.name,
        navArgument(EU_NAV_TASK_ID) {
            type = NavType.StringType
            defaultValue = ""
            nullable = true
        }
    ) {
        fun path(taskId: String? = null): String {
            val result = if (taskId.isNullOrEmpty()) {
                route
            } else {
                path(EU_NAV_TASK_ID to taskId)
            }
            return result
        }
    }

    object EuCountrySelectionScreen : Routes(NavigationRouteNames.EuCountrySelectionScreen.name)

    object EuPrescriptionSelectionScreen : Routes(NavigationRouteNames.EuPrescriptionSelectionScreen.name)

    object EuAvailabilityScreen : Routes(NavigationRouteNames.EuAvailabilityScreen.name)

    object EuInstructionsScreen : Routes(
        NavigationRouteNames.EuInstructionsScreen.name,
        navArgument(EU_NAV_REDEEM_BUTTON) {
            type = NavType.BoolType
            defaultValue = false
        }
    ) {
        fun path(showRedeemButton: Boolean = false): String {
            return path(EU_NAV_REDEEM_BUTTON to showRedeemButton)
        }
    }

    object EuRedemptionCodeScreen : Routes(NavigationRouteNames.EuRedemptionCodeScreen.name)
}
