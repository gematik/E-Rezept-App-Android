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

package de.gematik.ti.erp.app.cardwall.presentation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.cardwall.model.GidNavigationData
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.idp.model.UniversalLinkIdp
import de.gematik.ti.erp.app.idp.model.error.GematikResponseError
import de.gematik.ti.erp.app.idp.usecase.GetUniversalLinkForHealthInsuranceAppsUseCase
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.IsProfilePKVUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.viewmodel.rememberGraphScopedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance
import java.net.URI

class CardWallSharedViewModel(
    private val getUniversalLinkUseCase: GetUniversalLinkForHealthInsuranceAppsUseCase,
    private val isProfilePKVUseCase: IsProfilePKVUseCase
) : ViewModel() {
    private val _profileId = MutableStateFlow("")
    private val _can = MutableStateFlow("")
    private val _pin = MutableStateFlow("")
    private val _saveCredentials: MutableStateFlow<SaveCredentialsController.AuthResult?> = MutableStateFlow(null)
    private val _profileIsPkv: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _scannedCan = MutableStateFlow<String?>(null)
    val profileId: StateFlow<ProfileIdentifier> = _profileId
    val can: StateFlow<String> = _can
    val pin: StateFlow<String> = _pin
    val saveCredentials: StateFlow<SaveCredentialsController.AuthResult?> = _saveCredentials
    val scannedCan: StateFlow<String?> = _scannedCan.asStateFlow()
    val profileIsPkv: StateFlow<Boolean> = _profileIsPkv

    val authorizationWithExternalAppInBackgroundEvent = ComposableEvent<Boolean>()
    val redirectUriEvent = ComposableEvent<Pair<URI, GidNavigationData>>()
    val redirectUriGematikErrorEvent = ComposableEvent<GematikResponseError>()
    val redirectUriErrorEvent = ComposableEvent<String?>()

    fun resetPin() {
        viewModelScope.launch {
            _pin.update { "" }
        }
    }

    fun setProfileId(value: ProfileIdentifier) {
        viewModelScope.launch {
            _profileId.update { value }
            _profileIsPkv.update { isProfilePKVUseCase.invoke(value) }
        }
    }

    fun setCardAccessNumber(value: String) {
        viewModelScope.launch {
            _can.update { value }
        }
    }

    fun setPersonalIdentificationNumber(value: String) {
        viewModelScope.launch {
            _pin.update { value }
        }
    }

    fun setSaveCredentials(value: SaveCredentialsController.AuthResult?) {
        viewModelScope.launch {
            _saveCredentials.update { value }
        }
    }

    fun setScannedCan(value: String?) {
        viewModelScope.launch {
            _scannedCan.update { value }
        }
    }

    fun startAuthorizationWithExternal(
        gidNavigationData: GidNavigationData
    ) {
        viewModelScope.launch {
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
                    profileId = gidNavigationData.profileId
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

@Composable
internal fun cardWallSharedViewModel(
    navController: NavController,
    entry: NavBackStackEntry
): CardWallSharedViewModel {
    val getLinkUseCase by rememberInstance<GetUniversalLinkForHealthInsuranceAppsUseCase>()
    val isPkvUseCase by rememberInstance<IsProfilePKVUseCase>()
    return rememberGraphScopedViewModel(
        navController = navController,
        navEntry = entry,
        graphRoute = CardWallRoutes.subGraphName()
    ) {
        CardWallSharedViewModel(
            getUniversalLinkUseCase = getLinkUseCase,
            isProfilePKVUseCase = isPkvUseCase
        )
    }
}
