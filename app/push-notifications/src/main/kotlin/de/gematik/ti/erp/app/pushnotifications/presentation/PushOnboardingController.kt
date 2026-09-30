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
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.AcceptPushNotificationPermissionUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.DeclinePushNotificationPermissionUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.ShouldShowPushPermissionPromptUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class PushOnboardingController(
    private val profileId: ProfileIdentifier,
    private val shouldShowPushPermissionPromptUseCase: ShouldShowPushPermissionPromptUseCase,
    private val acceptPushNotificationPermissionUseCase: AcceptPushNotificationPermissionUseCase,
    private val declinePushNotificationPermissionUseCase: DeclinePushNotificationPermissionUseCase
) : Controller() {

    private val _showConsentDialog = MutableStateFlow(false)

    val showConsentDialog: StateFlow<Boolean> = _showConsentDialog.asStateFlow()
    val showEnableNotificationsEvent = ComposableEvent<Unit>()

    private var onboardingHandled = false

    fun checkFirstLogin() {
        if (onboardingHandled) return
        onboardingHandled = true
        controllerScope.launch {
            if (shouldShowPushPermissionPromptUseCase(profileId)) {
                _showConsentDialog.value = true
            }
        }
    }

    fun onConsentConfirmed() {
        _showConsentDialog.value = false
    }

    fun onConsentAccepted() {
        controllerScope.launch {
            acceptPushNotificationPermissionUseCase(profileId)
            showEnableNotificationsEvent.trigger(Unit)
        }
    }

    fun onConsentDeclined() {
        _showConsentDialog.value = false
        controllerScope.launch {
            declinePushNotificationPermissionUseCase(profileId)
        }
    }
}

@Composable
fun rememberPushOnboardingController(
    profileId: ProfileIdentifier
): PushOnboardingController {
    val shouldShowPushPermissionPromptUseCase by rememberInstance<ShouldShowPushPermissionPromptUseCase>()
    val acceptPushNotificationPermissionUseCase by rememberInstance<AcceptPushNotificationPermissionUseCase>()
    val declinePushNotificationPermissionUseCase by rememberInstance<DeclinePushNotificationPermissionUseCase>()

    return remember(profileId) {
        PushOnboardingController(
            profileId = profileId,
            shouldShowPushPermissionPromptUseCase = shouldShowPushPermissionPromptUseCase,
            acceptPushNotificationPermissionUseCase = acceptPushNotificationPermissionUseCase,
            declinePushNotificationPermissionUseCase = declinePushNotificationPermissionUseCase
        )
    }
}
