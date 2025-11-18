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

package de.gematik.ti.erp.app.timeouts.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.defaultInactivityValue
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.defaultPauseValue
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutsLocalDataSource.Companion.DurationEnum
import de.gematik.ti.erp.app.timeouts.presentation.TimeoutsError.Error
import de.gematik.ti.erp.app.timeouts.presentation.TimeoutsError.InactivityError
import de.gematik.ti.erp.app.timeouts.presentation.TimeoutsError.NoError
import de.gematik.ti.erp.app.timeouts.presentation.TimeoutsError.PauseError
import de.gematik.ti.erp.app.timeouts.usecase.GetInactivityMetricUseCase
import de.gematik.ti.erp.app.timeouts.usecase.GetPauseMetricUseCase
import de.gematik.ti.erp.app.timeouts.usecase.SetInactivityMetricUseCase
import de.gematik.ti.erp.app.timeouts.usecase.SetPauseMetricUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration

abstract class TimeoutsScreenViewModel : ViewModel() {

    abstract val inactivityMetricDuration: StateFlow<Duration>
    abstract val pauseMetricDuration: StateFlow<Duration>

    abstract val error: MutableSharedFlow<TimeoutsError>
    abstract suspend fun load()

    abstract fun emitNoError()

    open fun setInactivityMetric(
        value: String,
        unit: DurationEnum
    ) {
        viewModelScope.launch { load() }
        emitNoError()
    }

    open fun setPauseMetric(
        value: String,
        unit: DurationEnum
    ) {
        viewModelScope.launch { load() }
        emitNoError()
    }

    open fun resetToDefaultMetrics() {
        viewModelScope.launch { load() }
        emitNoError()
    }
}

enum class TimeoutsError {
    InactivityError, PauseError, Error, NoError
}

class DefaultTimeoutsScreenViewModel(
    private val getInactivityMetricUseCase: GetInactivityMetricUseCase,
    private val getPauseMetricUseCase: GetPauseMetricUseCase,
    private val setInactivityMetricUseCase: SetInactivityMetricUseCase,
    private val setPauseMetricUseCase: SetPauseMetricUseCase
) : TimeoutsScreenViewModel() {

    private val inactivityMetric = MutableStateFlow(Duration.ZERO)

    private val pauseMetric = MutableStateFlow(Duration.ZERO)

    init {
        viewModelScope.launch { load() }
    }

    override suspend fun load() {
        inactivityMetric.value = getInactivityMetricUseCase()
        pauseMetric.value = getPauseMetricUseCase()
    }

    override val inactivityMetricDuration: StateFlow<Duration> = inactivityMetric
    override val pauseMetricDuration: StateFlow<Duration> = pauseMetric

    override val error = MutableSharedFlow<TimeoutsError>()

    override fun setInactivityMetric(
        value: String,
        unit: DurationEnum
    ) {
        runCatching {
            setInactivityMetricUseCase.invoke(value = value, unit = unit)
        }.fold(
            onSuccess = {
                // call super only in success
                super.setInactivityMetric(value, unit)
            },
            onFailure = {
                error.tryEmit(InactivityError)
            }
        )
    }

    override fun setPauseMetric(
        value: String,
        unit: DurationEnum
    ) {
        runCatching {
            setPauseMetricUseCase.invoke(value = value, unit = unit)
        }.fold(
            onSuccess = {
                // call super only in success
                super.setPauseMetric(value, unit)
            },
            onFailure = {
                error.tryEmit(PauseError)
            }
        )
    }

    override fun resetToDefaultMetrics() {
        runCatching {
            setInactivityMetricUseCase(
                value = defaultInactivityValue.toString(),
                unit = DurationEnum.MINUTES
            )
            setPauseMetricUseCase(
                value = defaultPauseValue.toString(),
                unit = DurationEnum.SECONDS
            )
        }.fold(
            onSuccess = {
                // call super only in success
                super.resetToDefaultMetrics()
            },
            onFailure = {
                error.tryEmit(Error)
            }
        )
    }

    override fun emitNoError() {
        error.tryEmit(NoError)
    }
}
