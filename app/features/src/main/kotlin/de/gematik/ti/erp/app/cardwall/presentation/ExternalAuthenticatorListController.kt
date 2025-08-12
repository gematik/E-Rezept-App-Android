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
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.idp.model.HealthInsuranceData
import de.gematik.ti.erp.app.idp.model.UniversalLinkIdp.Companion.toUniversalLinkIdp
import de.gematik.ti.erp.app.idp.model.error.GematikResponseError
import de.gematik.ti.erp.app.idp.usecase.GetHealthInsuranceAppIdpsUseCase
import de.gematik.ti.erp.app.idp.usecase.GetUniversalLinkForHealthInsuranceAppsUseCase
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.IsProfilePKVUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance
import java.net.URI

internal fun whiteSpaceRegex(): Regex = "\\s+".toRegex()

interface ExternalAuthenticatorListController {
    val authorizationWithExternalAppInBackgroundEvent: ComposableEvent<Boolean>
    val redirectUriEvent: ComposableEvent<Pair<URI, HealthInsuranceData>>
    val redirectUriGematikErrorEvent: ComposableEvent<GematikResponseError>
    val redirectUriErrorEvent: ComposableEvent<String?>

    val healthInsuranceDataList: StateFlow<UiState<List<HealthInsuranceData>>>
    val searchValue: StateFlow<String>

    fun getHealthInsuranceAppList(profileIsPkv: Boolean)
    fun startAuthorizationWithExternal(
        profileId: ProfileIdentifier,
        healthInsuranceData: HealthInsuranceData
    )
    fun onFilterList(searchWord: String)
    fun onRemoveFilterList()
}

class DefaultExternalAuthenticatorListController(
    private val getHealthInsuranceAppIdpsUseCase: GetHealthInsuranceAppIdpsUseCase,
    private val getUniversalLinkUseCase: GetUniversalLinkForHealthInsuranceAppsUseCase,
    private val isProfilePKVUseCase: IsProfilePKVUseCase,
    private val profileId: ProfileIdentifier
) : ExternalAuthenticatorListController, Controller() {
    override val authorizationWithExternalAppInBackgroundEvent = ComposableEvent<Boolean>()
    override val redirectUriEvent = ComposableEvent<Pair<URI, HealthInsuranceData>>()
    override val redirectUriGematikErrorEvent = ComposableEvent<GematikResponseError>()
    override val redirectUriErrorEvent = ComposableEvent<String?>()

    private val _originalHealthInsuranceDataList = mutableListOf<HealthInsuranceData>()
    private val _searchValue: MutableStateFlow<String> = MutableStateFlow("")

    override val searchValue: StateFlow<String> = _searchValue
    override val healthInsuranceDataList = MutableStateFlow<UiState<List<HealthInsuranceData>>>(
        UiState.Loading()
    )

    init {
        controllerScope.launch {
            getHealthInsuranceAppList(isProfilePKVUseCase.invoke(profileId))
        }
    }

    @Requirement(
        "O.Auth_4#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Business logic to get the list of health insurance companies."
    )
    override fun getHealthInsuranceAppList(profileIsPkv: Boolean) {
        healthInsuranceDataList.update { UiState.Loading() }
        controllerScope.launch {
            runCatching {
                getHealthInsuranceAppIdpsUseCase.invoke()
            }.fold(
                onSuccess = { healthInsuranceList ->
                    val filteredList = healthInsuranceList.filter { it.isPKV == profileIsPkv }
                    _originalHealthInsuranceDataList.addAll(filteredList)
                    if (filteredList.isEmpty()) {
                        healthInsuranceDataList.update { UiState.Empty() }
                    } else {
                        healthInsuranceDataList.update { UiState.Data(filteredList) }
                    }
                },
                onFailure = { error ->
                    healthInsuranceDataList.update { UiState.Error(error) }
                }
            )
        }
    }

    override fun startAuthorizationWithExternal(
        profileId: ProfileIdentifier,
        healthInsuranceData: HealthInsuranceData
    ) {
        controllerScope.launch {
            authorizationWithExternalAppInBackgroundEvent.trigger(true)
            @Requirement(
                "O.Auth_4#5",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "Start the GID login process with the health insurance company."
            )
            getUniversalLinkUseCase.invoke(
                universalLinkIdp = healthInsuranceData.toUniversalLinkIdp(profileId)
            ).fold(
                onSuccess = { redirectUri ->
                    authorizationWithExternalAppInBackgroundEvent.trigger(false)
                    redirectUriEvent.trigger(redirectUri to healthInsuranceData)
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

    override fun onFilterList(searchWord: String) {
        _searchValue.update { searchWord }
        val stringList = searchWord.split(whiteSpaceRegex())
        val filteredList = _originalHealthInsuranceDataList.filter { src ->
            stringList.all { src.name.contains(it, ignoreCase = true) }
        }.toMutableList()
        if (filteredList.isEmpty()) {
            healthInsuranceDataList.update { UiState.Empty() }
        } else {
            healthInsuranceDataList.update { UiState.Data(filteredList) }
        }
    }

    override fun onRemoveFilterList() {
        _searchValue.update { "" }
        healthInsuranceDataList.update { UiState.Data(_originalHealthInsuranceDataList) }
    }
}

@Composable
fun rememberExternalAuthenticatorListController(profileId: ProfileIdentifier): ExternalAuthenticatorListController {
    val getHealthInsuranceAppIdpsUseCase: GetHealthInsuranceAppIdpsUseCase by rememberInstance()
    val getUniversalLinkUseCase: GetUniversalLinkForHealthInsuranceAppsUseCase by rememberInstance()
    val isProfilePKVUseCase by rememberInstance<IsProfilePKVUseCase>()
    return remember {
        DefaultExternalAuthenticatorListController(
            getHealthInsuranceAppIdpsUseCase = getHealthInsuranceAppIdpsUseCase,
            getUniversalLinkUseCase = getUniversalLinkUseCase,
            isProfilePKVUseCase = isProfilePKVUseCase,
            profileId = profileId
        )
    }
}
