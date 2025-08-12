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

package de.gematik.ti.erp.app.pkv.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.rememberSaveable
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.consent.model.ConsentState
import de.gematik.ti.erp.app.consent.model.ConsentState.Companion.isConsentGranted
import de.gematik.ti.erp.app.consent.model.ConsentState.Companion.isNotGranted
import de.gematik.ti.erp.app.consent.usecase.GetConsentUseCase
import de.gematik.ti.erp.app.consent.usecase.GrantConsentUseCase
import de.gematik.ti.erp.app.consent.usecase.RevokeConsentUseCase
import de.gematik.ti.erp.app.consent.usecase.SaveGrantConsentDrawerShownUseCase
import de.gematik.ti.erp.app.core.complexAutoSaver
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Stable
class ConsentController(
    private val getConsentUseCase: GetConsentUseCase,
    private val grantConsentUseCase: GrantConsentUseCase,
    private val revokeConsentUseCase: RevokeConsentUseCase,
    private val saveGrantConsentDrawerShownUseCase: SaveGrantConsentDrawerShownUseCase
) : Controller() {

    private val _consentState by lazy {
        MutableStateFlow<ConsentState>(
            ConsentState.ValidState.UnknownConsent
        )
    }

    val consentState: StateFlow<ConsentState> = _consentState

    val isConsentGranted: StateFlow<Boolean> = consentState.map { it.isConsentGranted() }
        .stateIn(controllerScope, started = SharingStarted.Eagerly, initialValue = false)

    val isConsentNotGranted by lazy {
        consentState.map { it.isNotGranted() }
            .stateIn(
                controllerScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = false
            )
    }

    fun getChargeConsent(profileId: ProfileIdentifier) {
        _consentState.value = ConsentState.ValidState.Loading
        controllerScope.launch {
            getConsentUseCase(profileId)
                .first().apply {
                    _consentState.value = this as ConsentState
                }
        }
    }

    fun grantChargeConsent(profileId: ProfileIdentifier) {
        controllerScope.launch {
            grantConsentUseCase(profileId).first().apply {
                _consentState.value = this as ConsentState
            }
        }
    }

    fun revokeChargeConsent(profileId: ProfileIdentifier) {
        controllerScope.launch {
            revokeConsentUseCase(profileId)
                .first().apply {
                    _consentState.value = this as ConsentState
                }
        }
    }

    fun saveConsentDrawerShown(profileId: ProfileIdentifier) {
        controllerScope.launch {
            saveGrantConsentDrawerShownUseCase.invoke(profileId)
        }
    }
}

@Composable
fun rememberConsentController(): ConsentController {
    val getConsentUseCase by rememberInstance<GetConsentUseCase>()
    val grantConsentUseCase by rememberInstance<GrantConsentUseCase>()
    val revokeConsentUseCase by rememberInstance<RevokeConsentUseCase>()
    val saveGrantConsentDrawerShownUseCase by rememberInstance<SaveGrantConsentDrawerShownUseCase>()

    return rememberSaveable(saver = complexAutoSaver()) {
        ConsentController(
            getConsentUseCase = getConsentUseCase,
            grantConsentUseCase = grantConsentUseCase,
            revokeConsentUseCase = revokeConsentUseCase,
            saveGrantConsentDrawerShownUseCase = saveGrantConsentDrawerShownUseCase
        )
    }
}
