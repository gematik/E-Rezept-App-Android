/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.gematik.ti.erp.app.core.BaseViewModel
import de.gematik.ti.erp.app.db.entities.CommunicationProfile
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import de.gematik.ti.erp.app.messages.usecase.MessageUseCase
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

data class RedeemEvent(
    val taskIds: List<String>,
    val isFullDetail: Boolean
)

sealed class ErrorEvent {
    object NetworkNotAvailable : ErrorEvent()
    data class ServerCommunicationFailedWhileRefreshing(val code: Int) : ErrorEvent()
    object FatalTruststoreState : ErrorEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val demoUseCase: DemoUseCase,
    private val messageUseCase: MessageUseCase
) : BaseViewModel() {

    private val _onRedeemEvent = MutableSharedFlow<RedeemEvent>()
    val onRedeemEvent: Flow<RedeemEvent>
        get() = _onRedeemEvent

    private val _onErrorEvent = MutableSharedFlow<ErrorEvent>()
    val onErrorEvent: Flow<ErrorEvent>
        get() = _onErrorEvent

    fun unreadMessagesAvailable() =
        messageUseCase.unreadCommunicationsAvailable(CommunicationProfile.ErxCommunicationReply)

    fun onClickRecipeCard(recipe: PrescriptionUseCaseData.Recipe) {
        viewModelScope.launch {
            _onRedeemEvent.emit(
                when (recipe) {
                    is PrescriptionUseCaseData.Recipe.Synced -> {
                        RedeemEvent(recipe.prescriptions.map { it.taskId }, true)
                    }
                    is PrescriptionUseCaseData.Recipe.Scanned -> {
                        RedeemEvent(recipe.prescriptions.map { it.taskId }, false)
                    }
                }
            )
        }
    }

    fun onClickRecipeScannedCard(recipes: List<PrescriptionUseCaseData.Recipe.Scanned>) {
        viewModelScope.launch {

            _onRedeemEvent.emit(
                RedeemEvent(
                    recipes.flatMap { prescription ->
                        prescription.prescriptions.map { it.taskId }
                    },
                    false
                )
            )
        }
    }

    fun onClickRecipeSyncedCard(recipes: List<PrescriptionUseCaseData.Recipe.Synced>) {
        viewModelScope.launch {
            _onRedeemEvent.emit(
                RedeemEvent(
                    recipes.flatMap { prescription ->
                        prescription.prescriptions.map { it.taskId }
                    },
                    true
                )
            )
        }
    }

    suspend fun onError(error: ErrorEvent) {
        _onErrorEvent.emit(error)
    }

    fun onDeactivateDemoMode() {
        demoUseCase.deactivateDemoMode()
    }
}
