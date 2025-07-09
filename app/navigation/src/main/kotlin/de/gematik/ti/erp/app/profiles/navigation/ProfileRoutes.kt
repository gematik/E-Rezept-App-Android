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

package de.gematik.ti.erp.app.profiles.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import de.gematik.ti.erp.app.navigation.NavigationRouteNames
import de.gematik.ti.erp.app.navigation.NavigationRoutes
import de.gematik.ti.erp.app.navigation.Routes

object ProfileRoutes : NavigationRoutes {
    override fun subGraphName() = "profile"
    const val PROFILE_NAV_PROFILE_ID = "PROFILE_ID_PROFILE_NAVIGATION"

    object ProfileScreen : Routes(
        NavigationRouteNames.ProfileScreen.name,
        navArgument(PROFILE_NAV_PROFILE_ID) { type = NavType.StringType }
    ) {
        fun path(profileId: String) = path(PROFILE_NAV_PROFILE_ID to profileId)
    }

    object ProfileAuditEventsScreen : Routes(
        NavigationRouteNames.ProfileAuditEventsScreen.name,
        navArgument(PROFILE_NAV_PROFILE_ID) { type = NavType.StringType }
    ) {
        fun path(profileId: String) = path(PROFILE_NAV_PROFILE_ID to profileId)
    }

    object ProfilePairedDevicesScreen : Routes(
        NavigationRouteNames.ProfilePairedDevicesScreen.name,
        navArgument(PROFILE_NAV_PROFILE_ID) { type = NavType.StringType }
    ) {
        fun path(profileId: String) = path(PROFILE_NAV_PROFILE_ID to profileId)
    }

    object ProfileEditPictureScreen : Routes(
        NavigationRouteNames.ProfileEditPictureScreen.name,
        navArgument(PROFILE_NAV_PROFILE_ID) { type = NavType.StringType }
    ) {
        fun path(profileId: String) = path(PROFILE_NAV_PROFILE_ID to profileId)
    }

    object ProfileImageCropperScreen : Routes(
        NavigationRouteNames.ProfileImageCropperScreen.name,
        navArgument(PROFILE_NAV_PROFILE_ID) { type = NavType.StringType }
    ) {
        fun path(profileId: String) = path(PROFILE_NAV_PROFILE_ID to profileId)
    }

    object ProfileImageEmojiScreen : Routes(
        NavigationRouteNames.ProfileImageEmojiScreen.name,
        navArgument(PROFILE_NAV_PROFILE_ID) { type = NavType.StringType }
    ) {
        fun path(profileId: String) = path(PROFILE_NAV_PROFILE_ID to profileId)
    }

    object ProfileImageCameraScreen : Routes(
        NavigationRouteNames.ProfileImageCameraScreen.name,
        navArgument(PROFILE_NAV_PROFILE_ID) { type = NavType.StringType }
    ) {
        fun path(profileId: String) = path(PROFILE_NAV_PROFILE_ID to profileId)
    }

    object ProfileEditPictureBottomSheetScreen : Routes(
        NavigationRouteNames.ProfileEditPictureBottomSheetScreen.name,
        navArgument(PROFILE_NAV_PROFILE_ID) { type = NavType.StringType }
    ) {
        fun path(profileId: String) = path(PROFILE_NAV_PROFILE_ID to profileId)
    }

    object ProfileEditNameBottomSheetScreen : Routes(
        NavigationRouteNames.ProfileEditNameBottomSheetScreen.name,
        navArgument(PROFILE_NAV_PROFILE_ID) { type = NavType.StringType }
    ) {
        fun path(profileId: String) = path(PROFILE_NAV_PROFILE_ID to profileId)
    }

    object ProfileAddNameBottomSheetScreen : Routes(
        NavigationRouteNames.ProfileAddNameBottomSheetScreen.name
    )

    fun getProfileId(entry: NavBackStackEntry): String =
        requireNotNull(entry.arguments?.getString(PROFILE_NAV_PROFILE_ID)) { "profile-id cannot be null for this navigation" }
}
