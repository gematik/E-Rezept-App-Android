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

package de.gematik.ti.erp.app.mainscreen.navigation

import android.os.Parcelable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import de.gematik.ti.erp.app.analytics.PopUpName
import de.gematik.ti.erp.app.navigation.Routes
import de.gematik.ti.erp.app.settings.navigation.SettingsNavigationScreens
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class TaskIds(val ids: List<String>) : Parcelable, List<String> by ids

object MainNavigationScreens {
    object Onboarding : Routes("onboarding")
    object Prescriptions : Routes("main")
    object Archive : Routes("main_prescriptionArchive")

    object Orders : Routes("orders")

    object Messages : Routes(
        "orders_detail",
        navArgument("orderId") { type = NavType.StringType }
    ) {
        fun path(orderId: String) =
            Messages.path("orderId" to orderId)
    }

    object Pharmacies : Routes("pharmacySearch")

    object Redeem : Routes("redeem_methodSelection")

    object Debug : Routes("debug")
    object OrderHealthCard : Routes("contactInsuranceCompany")
}

val MainScreenBottomNavigationItems = listOf(
    MainNavigationScreens.Prescriptions,
    MainNavigationScreens.Pharmacies,
    MainNavigationScreens.Orders,
    SettingsNavigationScreens.SettingsScreen
)

object MainScreenBottomPopUpNames {
    object EditProfilePicture : PopUpName("main_editProfilePicture")
    object EditProfileName : PopUpName("main_editName")
    object AddProfile : PopUpName("main_createProfile")
    object Welcome : PopUpName("main_welcomeDrawer")
    object GrantConsent : PopUpName("main_grantConsentDrawer")
}
