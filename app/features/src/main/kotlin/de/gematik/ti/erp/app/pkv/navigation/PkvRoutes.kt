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

package de.gematik.ti.erp.app.pkv.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import de.gematik.ti.erp.app.navigation.NavigationRouteNames
import de.gematik.ti.erp.app.navigation.NavigationRoutes
import de.gematik.ti.erp.app.navigation.Routes

object PkvRoutes : NavigationRoutes {
    override fun subGraphName() = "invoices"
    const val TaskId = "taskId"
    const val ProfileId = "profileId"
    object InvoiceListScreen : Routes(
        NavigationRouteNames.InvoiceListScreen.name,
        navArgument(ProfileId) { type = NavType.StringType }
    ) {
        fun path(profileId: String) = path(ProfileId to profileId)
    }
    object InvoiceDetailsScreen :
        Routes(
            NavigationRouteNames.InvoiceDetailsScreen.name,
            navArgument(TaskId) { type = NavType.StringType },
            navArgument(ProfileId) { type = NavType.StringType }
        ) {
        fun path(taskId: String, profileId: String) = path(TaskId to taskId, ProfileId to profileId)
    }
    object InvoiceExpandedDetailsScreen :
        Routes(
            NavigationRouteNames.InvoiceExpandedDetailsScreen.name,
            navArgument(TaskId) { type = NavType.StringType },
            navArgument(ProfileId) { type = NavType.StringType }
        ) {
        fun path(taskId: String, profileId: String) = path(TaskId to taskId, ProfileId to profileId)
    }
    object InvoiceLocalCorrectionScreen :
        Routes(
            NavigationRouteNames.InvoiceLocalCorrectionScreen.name,
            navArgument(TaskId) { type = NavType.StringType },
            navArgument(ProfileId) { type = NavType.StringType }
        ) {
        fun path(taskId: String, profileId: String) = path(TaskId to taskId, ProfileId to profileId)
    }
    object InvoiceShareScreen :
        Routes(
            NavigationRouteNames.InvoiceShareScreen.name,
            navArgument(TaskId) { type = NavType.StringType },
            navArgument(ProfileId) { type = NavType.StringType }
        ) {
        fun path(taskId: String, profileId: String) = path(TaskId to taskId, ProfileId to profileId)
    }
}

data class PkvNavigationArguments(val taskId: String, val profileId: String)
