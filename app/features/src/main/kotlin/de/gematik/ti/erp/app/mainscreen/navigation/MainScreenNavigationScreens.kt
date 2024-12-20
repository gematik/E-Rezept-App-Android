/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.mainscreen.navigation

import android.os.Parcelable
import de.gematik.ti.erp.app.analytics.PopUpName
import de.gematik.ti.erp.app.messages.navigation.MessagesRoutes
import de.gematik.ti.erp.app.navigation.Routes
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes
import de.gematik.ti.erp.app.prescription.navigation.PrescriptionRoutes
import de.gematik.ti.erp.app.settings.navigation.SettingsNavigationScreens
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class TaskIds(val ids: List<String>) : Parcelable, List<String> by ids

object MainNavigationScreens {
    object Debug : Routes("debug")
}

val MainScreenBottomNavigationItems = listOf(
    PrescriptionRoutes.PrescriptionsScreen,
    PharmacyRoutes.PharmacyStartScreen,
    MessagesRoutes.MessageListScreen,
    SettingsNavigationScreens.SettingsScreen
)

object MainScreenBottomPopUpNames {
    object EditProfilePicture : PopUpName("main_editProfilePicture")
    object EditProfileName : PopUpName("main_editName")
    object AddProfile : PopUpName("main_createProfile")
    object Welcome : PopUpName("main_welcomeDrawer")
    object GrantConsent : PopUpName("main_grantConsentDrawer")
}
