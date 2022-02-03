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

import android.os.Parcelable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.squareup.moshi.JsonClass
import de.gematik.ti.erp.app.AppNavTypes
import de.gematik.ti.erp.app.Route
import de.gematik.ti.erp.app.settings.ui.SettingsScrollTo
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class TaskIds(val ids: List<String>) : Parcelable, List<String> by ids

object MainNavigationScreens {
    object Onboarding : Route("Onboarding")
    object ProfileSetup : Route("ProfileSetup")
    object ReturningUserSecureAppOnboarding : Route("ReturningUserSecureAppOnboarding")
    object Settings : Route(
        "Settings",
        navArgument("scrollToSection") {
            type = NavType.EnumType(SettingsScrollTo::class.java)
            defaultValue = SettingsScrollTo.None
        }
    ) {
        fun path(scrollToSection: SettingsScrollTo = SettingsScrollTo.None) =
            path("scrollToSection" to scrollToSection)
    }

    object Camera : Route("Camera")
    object Prescriptions : Route("Prescriptions")
    object PrescriptionDetail :
        Route("PrescriptionDetail", navArgument("taskId") { type = NavType.StringType }) {
        fun path(taskId: String) = path("taskId" to taskId)
    }

    object Messages : Route("Messages")
    object PickUpCode : Route(
        "PickUpCode",
        navArgument("pickUpCodeHR") {
            type = NavType.StringType
            nullable = true
        },
        navArgument("pickUpCodeDMC") {
            type = NavType.StringType
            nullable = true
        }
    ) {
        fun path(pickUpCodeHR: String?, pickUpCodeDMC: String?) =
            path("pickUpCodeHR" to pickUpCodeHR, "pickUpCodeDMC" to pickUpCodeDMC)
    }

    object Pharmacies : Route(
        "Pharmacies",
        navArgument("taskIds") {
            type = AppNavTypes.TaskIdsType
            defaultValue = TaskIds(emptyList())
        }
    ) {
        fun path(taskIds: TaskIds) = path("taskIds" to taskIds)
    }

    object RedeemLocally :
        Route("RedeemLocally", navArgument("taskIds") { type = AppNavTypes.TaskIdsType }) {
        fun path(taskIds: TaskIds) = path("taskIds" to taskIds)
    }

    object CardWall : Route(
        "CardWall",
        navArgument("can") {
            type = NavType.BoolType
            defaultValue = false
        }
    ) {
        fun path(canAvailable: Boolean) = path("can" to canAvailable)
    }

    object InsecureDeviceScreen : Route("InsecureDeviceScreen")
    object DataTermsUpdateScreen : Route("DataTermsUpdateScreen")
    object DataProtection : Route("DataProtection")
    object SafetynetNotOkScreen : Route("SafetynetInfoScreen")
    object EditProfile :
        Route("EditProfile", navArgument("profileId") { type = NavType.IntType }) {
        fun path(profileId: Int) = path("profileId" to profileId)
    }
}

val MainScreenBottomNavigationItems = listOf(
    MainNavigationScreens.Prescriptions,
    MainNavigationScreens.Messages,
    MainNavigationScreens.Pharmacies,
    MainNavigationScreens.Settings
)
