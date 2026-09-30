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

package de.gematik.ti.erp.app.messages.presentation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.gematik.ti.erp.app.messages.domain.usecase.GetInternalMessagesUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.SetInternalMessageAsReadUseCase
import de.gematik.ti.erp.app.messages.mapper.toInAppMessage
import de.gematik.ti.erp.app.messages.model.InAppMessage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Stable
class InternalMessageDetailScreenController(
    private val application: Application,
    private val getInternalMessagesUseCase: GetInternalMessagesUseCase,
    private val setInternalMessageAsReadUseCase: SetInternalMessageAsReadUseCase
) : AndroidViewModel(application) {
    private val selectedAppLanguage = application.resources.configuration.locales[0].language

    val internalMessages = getInternalMessagesUseCase.invoke(selectedAppLanguage).map {
            internalMessages ->
        internalMessages.map { message -> message.toInAppMessage() }
            .sortedByDescending { it.timeState.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList<InAppMessage>()
    )

    fun consumeAllMessages(onMessagesConsumed: () -> Unit = {}) {
        viewModelScope.launch {
            setInternalMessageAsReadUseCase.invoke()
            onMessagesConsumed()
        }
    }
}

@Composable
fun rememberInternalMessageDetailScreenController(): InternalMessageDetailScreenController {
    val getInternalMessagesUseCase: GetInternalMessagesUseCase by rememberInstance()
    val setInternalMessagesAsReadUseCase: SetInternalMessageAsReadUseCase by rememberInstance()
    val application = LocalContext.current.applicationContext as Application

    return remember() {
        InternalMessageDetailScreenController(
            getInternalMessagesUseCase = getInternalMessagesUseCase,
            setInternalMessageAsReadUseCase = setInternalMessagesAsReadUseCase,
            application = application
        )
    }
}
