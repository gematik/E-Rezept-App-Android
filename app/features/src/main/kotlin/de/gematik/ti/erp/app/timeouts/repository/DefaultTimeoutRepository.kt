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
import kotlin.time.Duration

class DefaultTimeoutRepository(
    private val localDataSource: TimeoutsLocalDataSource
) : TimeoutRepository {
    override fun setDefaultTimeouts() {
        localDataSource.setDefaultTimeouts()
    }

    override fun areTimeoutsExisting(): Boolean = localDataSource.checkForExistingTimeouts()

    override fun changeInactivityTimeout(duration: Duration) {
        localDataSource.setInactivityTimer(duration)
    }

    override fun changePauseTimeout(duration: Duration) {
        localDataSource.setPauseTimer(duration)
    }

    override fun getInactivityTimeout(): Duration =
        localDataSource.getInactivityTimeout()

    override fun getPauseTimeout(): Duration =
        localDataSource.getPauseTimeout()
}
