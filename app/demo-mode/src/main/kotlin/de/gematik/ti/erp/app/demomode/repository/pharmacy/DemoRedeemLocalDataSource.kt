/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.demomode.repository.pharmacy

import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource
import de.gematik.ti.erp.app.redeem.repository.datasource.RedeemLocalDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class DemoRedeemLocalDataSource(
    private val dataSource: DemoModeDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : RedeemLocalDataSource {

    override suspend fun markAsRedeemed(taskId: String) {
        withContext(dispatcher) {
            dataSource.scannedTasks.value = dataSource.scannedTasks.updateAndGet {
                val scannedTasks = it.toMutableList()
                val index = scannedTasks.indexOfFirst { item -> item.taskId == taskId }
                scannedTasks[index] = scannedTasks[index].copy(redeemedOn = Clock.System.now())
                scannedTasks
            }
        }
    }
}
