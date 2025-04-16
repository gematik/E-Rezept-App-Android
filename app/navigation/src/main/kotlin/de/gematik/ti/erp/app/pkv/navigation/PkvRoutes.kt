/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.pkv.navigation

import android.os.Bundle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import de.gematik.ti.erp.app.navigation.NavigationRouteNames
import de.gematik.ti.erp.app.navigation.NavigationRoutes
import de.gematik.ti.erp.app.navigation.Routes
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes.getPkvNavigationProfileId
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes.getPkvNavigationTaskId
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier

object PkvRoutes : NavigationRoutes {
    override fun subGraphName() = "invoices"
    const val PKV_NAV_TASK_ID = "taskId"
    const val PKV_NAV_PROFILE_ID = "profileId"

    object InvoiceListScreen : Routes(
        NavigationRouteNames.InvoiceListScreen.name,
        navArgument(PKV_NAV_PROFILE_ID) { type = NavType.StringType }
    ) {
        fun path(profileId: String) = path(PKV_NAV_PROFILE_ID to profileId)
    }

    object InvoiceDetailsScreen :
        Routes(
            NavigationRouteNames.InvoiceDetailsScreen.name,
            navArgument(PKV_NAV_TASK_ID) { type = NavType.StringType },
            navArgument(PKV_NAV_PROFILE_ID) { type = NavType.StringType }
        ) {
        fun path(taskId: String, profileId: String) = path(PKV_NAV_TASK_ID to taskId, PKV_NAV_PROFILE_ID to profileId)
    }

    object InvoiceExpandedDetailsScreen :
        Routes(
            NavigationRouteNames.InvoiceExpandedDetailsScreen.name,
            navArgument(PKV_NAV_TASK_ID) { type = NavType.StringType },
            navArgument(PKV_NAV_PROFILE_ID) { type = NavType.StringType }
        ) {
        fun path(taskId: String, profileId: String) = path(PKV_NAV_TASK_ID to taskId, PKV_NAV_PROFILE_ID to profileId)
    }

    object InvoiceLocalCorrectionScreen :
        Routes(
            NavigationRouteNames.InvoiceLocalCorrectionScreen.name,
            navArgument(PKV_NAV_TASK_ID) { type = NavType.StringType },
            navArgument(PKV_NAV_PROFILE_ID) { type = NavType.StringType }
        ) {
        fun path(taskId: String, profileId: String) = path(PKV_NAV_TASK_ID to taskId, PKV_NAV_PROFILE_ID to profileId)
    }

    object InvoiceShareScreen :
        Routes(
            NavigationRouteNames.InvoiceShareScreen.name,
            navArgument(PKV_NAV_TASK_ID) { type = NavType.StringType },
            navArgument(PKV_NAV_PROFILE_ID) { type = NavType.StringType }
        ) {
        fun path(taskId: String, profileId: String) = path(PKV_NAV_TASK_ID to taskId, PKV_NAV_PROFILE_ID to profileId)
    }

    fun (Bundle?).getPkvNavigationTaskId(): String? = this?.getString(PKV_NAV_TASK_ID)
    fun (Bundle?).getPkvNavigationProfileId(): ProfileIdentifier = requireNotNull(
        this?.getString(
            PKV_NAV_PROFILE_ID
        )
    )
}

data class PkvNavigationArguments(val taskId: String?, val profileId: ProfileIdentifier) {
    companion object {
        fun (Bundle?).getPkvNavigationArguments(): PkvNavigationArguments =
            this?.let { bundle ->
                PkvNavigationArguments(
                    taskId = bundle.getPkvNavigationTaskId(),
                    profileId = requireNotNull(bundle.getPkvNavigationProfileId()) { "ProfileId is required to be sent for this screen" }
                )
            } ?: throw IllegalArgumentException("PKV Bundle is null")
    }
}
