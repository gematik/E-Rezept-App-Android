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

package de.gematik.ti.erp.app.cardwall.presentation

import de.gematik.ti.erp.app.base.SharedController
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CardWallGraphController : SharedController() {
    private val _profileId = MutableStateFlow("")
    private val _can = MutableStateFlow("")
    private val _pin = MutableStateFlow("")
    private val _altPairing: MutableStateFlow<AltPairingProvider.AuthResult?> = MutableStateFlow(null)

    val profileId: StateFlow<ProfileIdentifier> = _profileId
    val can: StateFlow<String> = _can
    val pin: StateFlow<String> = _pin
    val altPairing: StateFlow<AltPairingProvider.AuthResult?> = _altPairing

    init {
        reset()
    }

    fun reset() {
        controllerScope.launch {
            _profileId.value = ""
            _can.value = ""
            _pin.value = ""
            _altPairing.value = null
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
    fun setAltPairing(value: AltPairingProvider.AuthResult?) {
        controllerScope.launch {
            _altPairing.value = value
        }
    }
}
