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

package de.gematik.ti.erp.app.timeouts.repository

import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutsLocalDataSource
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.defaultInactivityMetric
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.defaultInactivityValue
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.defaultPauseMetric
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.defaultPauseValue
import kotlin.time.Duration
import kotlin.time.toDuration

class DefaultTimeoutRepository(
    private val localDataSource: TimeoutsLocalDataSource
) : TimeoutRepository {
    override fun setDefaultTimeouts() {
        localDataSource.setDefaultTimeouts()
    }

    override fun areTimeoutsExisting(): Boolean =
        (localDataSource.getPauseTimeout() != null && localDataSource.getInactivityTimeout() != null)

    override fun changeInactivityTimeout(duration: Duration) {
        localDataSource.setInactivityTimer(duration)
    }

    override fun changePauseTimeout(duration: Duration) {
        localDataSource.setPauseTimer(duration)
    }

    override fun getInactivityTimeout(): Duration =
        localDataSource.getInactivityTimeout() ?: defaultInactivityValue.toDuration(defaultInactivityMetric)

    override fun getPauseTimeout(): Duration =
        localDataSource.getPauseTimeout() ?: defaultPauseValue.toDuration(defaultPauseMetric)
}
