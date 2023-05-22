/*
 * Copyright (c) 2023 gematik GmbH
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
import kotlinx.serialization.Serializable
import de.gematik.ti.erp.app.Route
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.prescription.detail.ui.model.PopUpName
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.parcelize.Parcelize

@Parcelize
@Serializable
data class TaskIds(val ids: List<String>) : Parcelable, List<String> by ids

object MainNavigationScreens {
    object Onboarding : Route("onboarding")
    object Settings : Route("settings")
    object Camera : Route("main_scanner")
    object Prescriptions : Route("main")
    object Archive : Route("main_prescriptionArchive")
    object PrescriptionDetail :
        Route(
            "prescriptionDetail",
            navArgument("taskId") { type = NavType.StringType }
        ) {
        fun path(taskId: String) = path("taskId" to taskId)
    }

    object Orders : Route("orders")

    object Messages : Route(
        "orders_detail",
        navArgument("orderId") { type = NavType.StringType }
    ) {
        fun path(orderId: String) =
            Messages.path("orderId" to orderId)
    }

    object Pharmacies : Route("pharmacySearch")

    object Redeem : Route("redeem_methodSelection")

    object ProfileImageCropper : Route(
        "profile_editPicture_imageCropper",
        navArgument("profileId") { type = NavType.StringType }
    ) {
        fun path(profileId: String) = path("profileId" to profileId)
    }

    object CardWall : Route(
        "cardWall_introduction",
        navArgument("profileId") { type = NavType.StringType }
    ) {
        fun path(profileId: ProfileIdentifier) = path("profileId" to profileId)
    }

    object InsecureDeviceScreen : Route("main_deviceSecurity")
    object MlKitIntroScreen : Route("mlKit")
    object MlKitInformationScreen : Route("mlKit_information")
    object DataProtection : Route("settings_dataProtection")
    object IntegrityNotOkScreen : Route("main_integrityWarning")
    object EditProfile :
        Route("profile", navArgument("profileId") { type = NavType.StringType }) {
        fun path(profileId: String) = path("profileId" to profileId)
    }
    object Terms : Route("settings_termsOfUse")
    object Imprint : Route("settings_legalNotice")
    object OpenSourceLicences : Route("settings_openSourceLicence")
    object AdditionalLicences : Route("settings_additionalLicence")
    object AllowAnalytics : Route("settings_productImprovements_complyTracking")
    object Password : Route("settings_authenticationMethods_setAppPassword")
    object Debug : Route("debug")
    object OrderHealthCard : Route("contactInsuranceCompany")

    object UnlockEgk : Route(
        "healthCardPassword_introduction",
        navArgument("unlockMethod") { type = NavType.StringType }
    ) {
        fun path(unlockMethod: UnlockMethod) = path("unlockMethod" to unlockMethod.name)
    }
}

val MainScreenBottomNavigationItems = listOf(
    MainNavigationScreens.Prescriptions,
    MainNavigationScreens.Orders,
    MainNavigationScreens.Pharmacies,
    MainNavigationScreens.Settings
)

object MainScreenBottomPopUpNames {
    object EditProfilePicture : PopUpName("main_editProfilePicture")
    object EditProfileName : PopUpName("main_editName")
    object AddProfile : PopUpName("main_createProfile")
    object Welcome : PopUpName("main_welcomeDrawer")
}
