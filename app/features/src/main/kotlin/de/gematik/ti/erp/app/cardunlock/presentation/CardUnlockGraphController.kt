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

package de.gematik.ti.erp.app.cardunlock.presentation

import android.nfc.Tag
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.card.model.command.UnlockMethod.None
import de.gematik.ti.erp.app.cardunlock.usecase.UnlockEgkState
import de.gematik.ti.erp.app.cardunlock.usecase.UnlockEgkUseCase
import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcHealthCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CardUnlockGraphController(
    private val unlockEgkUseCase: UnlockEgkUseCase
) : Controller() {

    private val _unlockMethod = MutableStateFlow(None)
    private val _can = MutableStateFlow("")
    private val _oldPin = MutableStateFlow("")
    private val _newPin = MutableStateFlow("")
    private val _puk = MutableStateFlow("")

    val unlockMethod: StateFlow<UnlockMethod> = _unlockMethod
    val can: StateFlow<String> = _can
    val oldPin: StateFlow<String> = _oldPin
    val newPin: StateFlow<String> = _newPin
    val puk: StateFlow<String> = _puk

    init {
        reset()
    }

    fun reset() {
        controllerScope.launch {
            _unlockMethod.value = None
            _can.value = ""
            _oldPin.value = ""
            _newPin.value = ""
            _puk.value = ""
        }
    }

    fun setUnlockMethodForGraph(value: UnlockMethod) {
        reset()
        controllerScope.launch {
            _unlockMethod.value = value
        }
    }

    fun setCardAccessNumber(value: String) {
        controllerScope.launch {
            _can.value = value
        }
    }

    fun setOldPin(value: String) {
        controllerScope.launch {
            _oldPin.value = value
        }
    }

    fun setNewPin(value: String) {
        controllerScope.launch {
            _newPin.value = value
        }
    }

    fun setPersonalUnblockingKey(value: String) {
        controllerScope.launch {
            _puk.value = value
        }
    }

    fun unlockEgk(
        unlockMethod: String,
        can: String,
        puk: String,
        oldPin: String,
        newPin: String,
        tag: Flow<Tag>
    ): Flow<UnlockEgkState> = unlockEgkUseCase(
        unlockMethod = unlockMethod,
        can = can,
        puk = puk,
        oldSecret = oldPin,
        newSecret = newPin,
        cardChannel = tag.map { NfcHealthCard.connect(it) }
    ).flowOn(Dispatchers.IO)
}
