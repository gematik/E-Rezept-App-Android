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

package de.gematik.ti.erp.app.medicationplan.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.medicationplan.model.ProfileWithSchedules
import de.gematik.ti.erp.app.medicationplan.usecase.GetActiveProfileWithSchedulesUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.kodein.di.compose.rememberInstance

class MedicationPlanNotificationScreenController(
    private val loadProfilesWithSchedulesUseCase: GetActiveProfileWithSchedulesUseCase,
    private val now: Instant = Clock.System.now()

) : Controller() {
    private val _profilesWithSchedules:
        MutableStateFlow<UiState<List<ProfileWithSchedules>>> =
            MutableStateFlow(UiState.Loading())
    val profilesWithSchedules: StateFlow<UiState<List<ProfileWithSchedules>>> =
        _profilesWithSchedules

    init {
        controllerScope.launch {
            runCatching {
                loadProfilesWithSchedulesUseCase(now.toLocalDateTime(TimeZone.currentSystemDefault()))
            }.fold(
                onSuccess = { profileWithSchedules ->
                    if (profileWithSchedules.first().isEmpty()) {
                        _profilesWithSchedules.value = UiState.Empty()
                    } else {
                        _profilesWithSchedules.value = UiState.Data(profileWithSchedules.first())
                    }
                },
                onFailure = {
                    _profilesWithSchedules.value = UiState.Error(it)
                }
            )
        }
    }
}

@Composable
fun rememberMedicationPlanNotificationScreenController(): MedicationPlanNotificationScreenController {
    val loadProfilesWithSchedulesUseCase by
    rememberInstance<GetActiveProfileWithSchedulesUseCase>()
    return remember {
        MedicationPlanNotificationScreenController(
            loadProfilesWithSchedulesUseCase = loadProfilesWithSchedulesUseCase
        )
    }
}
