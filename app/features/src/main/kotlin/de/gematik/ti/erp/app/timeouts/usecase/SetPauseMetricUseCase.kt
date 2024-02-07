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

package de.gematik.ti.erp.app.timeouts.usecase

import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutsLocalDataSource.Companion.DurationEnum
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutsLocalDataSource.Companion.toDuration
import de.gematik.ti.erp.app.timeouts.repository.TimeoutRepository

class SetPauseMetricUseCase(
    private val repository: TimeoutRepository
) {
    operator fun invoke(
        value: String,
        unit: DurationEnum
    ): Result<Unit> = (value.toIntOrNull() to unit.name).toDuration()?.let {
        repository.changePauseTimeout(duration = it)
        Result.success(Unit)
    } ?: run {
        Result.failure(IllegalStateException("value cannot be set, unreal values"))
    }
}