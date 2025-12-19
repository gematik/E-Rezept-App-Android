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
package de.gematik.ti.erp.app.eurezept.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.eurezept.domain.usecase.DeleteEuAccessCodeUseCase
import de.gematik.ti.erp.app.profiles.presentation.GetActiveProfileController
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.extract
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

enum class EuAccessCodeDeleteState {
    Initial,
    Error,
    Loading,
    Success
}

class EuDeleteAccessCodeBottomSheetScreenController(
    private val deleteEuAccessCodeUseCase: DeleteEuAccessCodeUseCase,
    getActiveProfileUseCase: GetActiveProfileUseCase
) : GetActiveProfileController(getActiveProfileUseCase) {

    private val _deleteState = MutableStateFlow(EuAccessCodeDeleteState.Initial)
    val deleteState = _deleteState.asStateFlow()

    fun deleteAccessCode() {
        controllerScope.launch {
            activeProfile.extract()?.let { activeProfile ->
                deleteEuAccessCodeUseCase.invoke(
                    activeProfile.id,
                    inProgress = {
                        _deleteState.value = EuAccessCodeDeleteState.Loading
                    },
                    failed = { error ->
                        Napier.e(error) { "Error while deleting access code" }
                        _deleteState.value = EuAccessCodeDeleteState.Error
                    },
                    completed = {
                        _deleteState.value = EuAccessCodeDeleteState.Success
                    }
                )
            }
        }
    }
}

@Composable
internal fun rememberDeleteAccessCodeBottomSheetScreenController(): EuDeleteAccessCodeBottomSheetScreenController {
    val deleteEuAccessCodeUseCase by rememberInstance<DeleteEuAccessCodeUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    return remember {
        EuDeleteAccessCodeBottomSheetScreenController(
            deleteEuAccessCodeUseCase = deleteEuAccessCodeUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase
        )
    }
}
