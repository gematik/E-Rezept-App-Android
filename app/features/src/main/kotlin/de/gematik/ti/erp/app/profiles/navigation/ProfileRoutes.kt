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

package de.gematik.ti.erp.app.profiles.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import de.gematik.ti.erp.app.navigation.NavigationRouteNames
import de.gematik.ti.erp.app.navigation.NavigationRoutes
import de.gematik.ti.erp.app.navigation.Routes

object ProfileRoutes : NavigationRoutes {
    override fun subGraphName() = "profile"
    const val ProfileId = "profileId"
    object ProfileScreen : Routes(
        NavigationRouteNames.ProfileScreen.name,
        navArgument(ProfileId) { type = NavType.StringType }
    ) {
        fun path(profileId: String) = path(ProfileId to profileId)
    }
    object ProfileTokenScreen : Routes(
        NavigationRouteNames.ProfileTokenScreen.name,
        navArgument(ProfileId) { type = NavType.StringType }
    ) {
        fun path(profileId: String) = path(ProfileId to profileId)
    }
    object ProfileAuditEventsScreen : Routes(
        NavigationRouteNames.ProfileAuditEventsScreen.name,
        navArgument(ProfileId) { type = NavType.StringType }
    ) {
        fun path(profileId: String) = path(ProfileId to profileId)
    }
    object ProfilePairedDevicesScreen : Routes(
        NavigationRouteNames.ProfilePairedDevicesScreen.name,
        navArgument(ProfileId) { type = NavType.StringType }
    ) {
        fun path(profileId: String) = path(ProfileId to profileId)
    }
    object ProfileEditPictureScreen : Routes(
        NavigationRouteNames.ProfileEditPictureScreen.name,
        navArgument(ProfileId) { type = NavType.StringType }
    ) {
        fun path(profileId: String) = path(ProfileId to profileId)
    }
    object ProfileImageCropperScreen : Routes(
        NavigationRouteNames.ProfileImageCropperScreen.name,
        navArgument(ProfileId) { type = NavType.StringType }
    ) {
        fun path(profileId: String) = path(ProfileId to profileId)
    }
}
