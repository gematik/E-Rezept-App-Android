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

package de.gematik.ti.erp.app.cardwall.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.idp.model.HealthInsuranceData
import de.gematik.ti.erp.app.idp.model.UniversalLinkIdp.Companion.toUniversalLinkIdp
import de.gematik.ti.erp.app.idp.model.error.GematikResponseError
import de.gematik.ti.erp.app.idp.usecase.GetHealthInsuranceAppIdpsUseCase
import de.gematik.ti.erp.app.idp.usecase.GetUniversalLinkForHealthInsuranceAppsUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.SwitchProfileToPKVUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance
import java.net.URI

private fun whiteSpaceRegex(): Regex = "\\s+".toRegex()

interface ExternalAuthenticatorListController {
    val healthInsuranceDataList: StateFlow<UiState<List<HealthInsuranceData>>>

    val authorizationWithExternalAppInBackgroundEvent: ComposableEvent<Boolean>

    val redirectUriEvent: ComposableEvent<Pair<URI, HealthInsuranceData>>

    val redirectUriGematikErrorEvent: ComposableEvent<GematikResponseError>

    val redirectUriErrorEvent: ComposableEvent<String?>
    fun getHealthInsuranceAppList()
    fun startAuthorizationWithExternal(
        profileId: ProfileIdentifier,
        healthInsuranceData: HealthInsuranceData
    )

    fun switchToPKV(profileId: ProfileIdentifier)
    fun filterList(searchWord: String)
    fun unFilterList()
}

class DefaultExternalAuthenticatorListController(
    private val getHealthInsuranceAppIdpsUseCase: GetHealthInsuranceAppIdpsUseCase,
    private val getUniversalLinkUseCase: GetUniversalLinkForHealthInsuranceAppsUseCase,
    private val switchProfileToPKVUseCase: SwitchProfileToPKVUseCase,
    private val scope: CoroutineScope
) : ExternalAuthenticatorListController {

    private val originalHealthInsuranceDataList = mutableListOf<HealthInsuranceData>()

    override val healthInsuranceDataList = MutableStateFlow<UiState<List<HealthInsuranceData>>>(UiState.Loading())

    override val authorizationWithExternalAppInBackgroundEvent = ComposableEvent<Boolean>()

    override val redirectUriEvent = ComposableEvent<Pair<URI, HealthInsuranceData>>()

    override val redirectUriGematikErrorEvent = ComposableEvent<GematikResponseError>()

    override val redirectUriErrorEvent = ComposableEvent<String?>()

    @Requirement(
        "O.Auth_4#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Business logic to get the list of health insurance companies."
    )
    override fun getHealthInsuranceAppList() {
        healthInsuranceDataList.value = UiState.Loading()
        scope.launch {
            runCatching {
                getHealthInsuranceAppIdpsUseCase.invoke()
            }.fold(
                onSuccess = {
                    originalHealthInsuranceDataList.addAll(it)
                    healthInsuranceDataList.value = UiState.Data(it)
                },
                onFailure = {
                    healthInsuranceDataList.value = UiState.Error(it)
                }
            )
        }
    }

    override fun startAuthorizationWithExternal(
        profileId: ProfileIdentifier,
        healthInsuranceData: HealthInsuranceData
    ) {
        scope.launch {
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

    override fun switchToPKV(profileId: ProfileIdentifier) {
        scope.launch {
            switchProfileToPKVUseCase.invoke(profileId)
        }
    }

    override fun filterList(searchWord: String) {
        searchWord.split(whiteSpaceRegex())
        val stringList = searchWord.split(whiteSpaceRegex())
        val filteredList = originalHealthInsuranceDataList.filter { src ->
            stringList.all { src.name.contains(it, ignoreCase = true) }
        }.toMutableList()
        healthInsuranceDataList.value = UiState.Data(filteredList)
    }

    override fun unFilterList() {
        healthInsuranceDataList.value = UiState.Data(originalHealthInsuranceDataList)
    }
}

@Composable
fun rememberExternalAuthenticatorListController(): ExternalAuthenticatorListController {
    val getHealthInsuranceAppIdpsUseCase: GetHealthInsuranceAppIdpsUseCase by rememberInstance()
    val getUniversalLinkUseCase: GetUniversalLinkForHealthInsuranceAppsUseCase by rememberInstance()
    val switchProfileToPKVUseCase: SwitchProfileToPKVUseCase by rememberInstance()
    val scope = rememberCoroutineScope()
    return remember {
        DefaultExternalAuthenticatorListController(
            getHealthInsuranceAppIdpsUseCase = getHealthInsuranceAppIdpsUseCase,
            getUniversalLinkUseCase = getUniversalLinkUseCase,
            switchProfileToPKVUseCase = switchProfileToPKVUseCase,
            scope = scope
        )
    }
}
