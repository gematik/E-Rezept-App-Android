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

package de.gematik.ti.erp.app.pushnotifications.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.profile.model.ProfilePushNotificationSettings
import de.gematik.ti.erp.app.profile.model.ProfilePushNotificationType
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.GetProfilePushNotificationSettingsUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.SaveProfilePushNotificationSettingUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class ProfilePushNotificationsController(
    profileId: ProfileIdentifier,
    getProfilePushNotificationSettingsUseCase: GetProfilePushNotificationSettingsUseCase,
    private val saveProfilePushNotificationSettingUseCase: SaveProfilePushNotificationSettingUseCase
) : Controller() {

    private val selectedProfileId = profileId

    val notificationSettings: StateFlow<ProfilePushNotificationSettings> =
        getProfilePushNotificationSettingsUseCase(profileId)
            .stateIn(
                controllerScope,
                SharingStarted.WhileSubscribed(),
                ProfilePushNotificationSettings()
            )

    fun onToggle(type: ProfilePushNotificationType, enabled: Boolean) {
        controllerScope.launch {
            saveProfilePushNotificationSettingUseCase(
                profileId = selectedProfileId,
                notificationType = type,
                enabled = enabled
            )
        }
    }
}

@Composable
fun rememberProfilePushNotificationsController(
    profileId: ProfileIdentifier
): ProfilePushNotificationsController {
    val getProfilePushNotificationSettingsUseCase by rememberInstance<GetProfilePushNotificationSettingsUseCase>()
    val saveProfilePushNotificationSettingUseCase by rememberInstance<SaveProfilePushNotificationSettingUseCase>()

    return remember(profileId) {
        ProfilePushNotificationsController(
            profileId = profileId,
            getProfilePushNotificationSettingsUseCase = getProfilePushNotificationSettingsUseCase,
            saveProfilePushNotificationSettingUseCase = saveProfilePushNotificationSettingUseCase
        )
    }
}
