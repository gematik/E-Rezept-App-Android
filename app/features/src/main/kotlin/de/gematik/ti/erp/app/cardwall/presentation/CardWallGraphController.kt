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

package de.gematik.ti.erp.app.cardwall.presentation

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.authentication.model.GidNavigationData
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.idp.model.UniversalLinkIdp
import de.gematik.ti.erp.app.idp.model.error.GematikResponseError
import de.gematik.ti.erp.app.idp.usecase.GetUniversalLinkForHealthInsuranceAppsUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.SwitchProfileToPKVUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.URI

class CardWallGraphController(
    private val getUniversalLinkUseCase: GetUniversalLinkForHealthInsuranceAppsUseCase,
    private val switchProfileToPKVUseCase: SwitchProfileToPKVUseCase
) : Controller() {
    private val _profileId = MutableStateFlow("")
    private val _can = MutableStateFlow("")
    private val _pin = MutableStateFlow("")
    private val _saveCredentials: MutableStateFlow<SaveCredentialsController.AuthResult?> = MutableStateFlow(null)

    val profileId: StateFlow<ProfileIdentifier> = _profileId
    val can: StateFlow<String> = _can
    val pin: StateFlow<String> = _pin
    val saveCredentials: StateFlow<SaveCredentialsController.AuthResult?> = _saveCredentials

    val authorizationWithExternalAppInBackgroundEvent = ComposableEvent<Boolean>()
    val redirectUriEvent = ComposableEvent<Pair<URI, GidNavigationData>>()
    val redirectUriGematikErrorEvent = ComposableEvent<GematikResponseError>()
    val redirectUriErrorEvent = ComposableEvent<String?>()

    init {
        reset()
    }

    override fun onCleared() {
        super.onCleared()
        reset()
    }

    fun reset() {
        controllerScope.launch {
            _profileId.value = ""
            _can.value = ""
            _pin.value = ""
            _saveCredentials.value = null
        }
    }

    fun resetPin() {
        controllerScope.launch {
            _pin.value = ""
        }
    }

    fun setProfileId(value: ProfileIdentifier) {
        controllerScope.launch {
            _profileId.value = value
        }
    }
    fun setCardAccessNumber(value: String) {
        controllerScope.launch {
            _can.value = value
        }
    }
    fun setPersonalIdentificationNumber(value: String) {
        controllerScope.launch {
            _pin.value = value
        }
    }
    fun setSaveCredentials(value: SaveCredentialsController.AuthResult?) {
        controllerScope.launch {
            _saveCredentials.value = value
        }
    }

    fun switchToPKV(profileId: ProfileIdentifier) {
        controllerScope.launch {
            switchProfileToPKVUseCase.invoke(profileId)
        }
    }

    fun startAuthorizationWithExternal(
        gidNavigationData: GidNavigationData
    ) {
        controllerScope.launch {
            authorizationWithExternalAppInBackgroundEvent.trigger(true)
            @Requirement(
                "O.Auth_4#5",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "Start the GID login process with the health insurance company."
            )
            getUniversalLinkUseCase.invoke(
                universalLinkIdp = UniversalLinkIdp(
                    authenticatorName = gidNavigationData.authenticatorName,
                    authenticatorId = gidNavigationData.authenticatorId,
                    profileId = gidNavigationData.profileId,
                    isGid = true
                )
            ).fold(
                onSuccess = { redirectUri ->
                    authorizationWithExternalAppInBackgroundEvent.trigger(false)
                    redirectUriEvent.trigger(redirectUri to gidNavigationData)
                },
                onFailure = {
                    authorizationWithExternalAppInBackgroundEvent.trigger(false)
                    when (it) {
                        is GematikResponseError -> redirectUriGematikErrorEvent.trigger(it)
                        else -> redirectUriErrorEvent.trigger(it.message)
                    }
                }
            )
        }
    }
}
