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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.demomode.repository.prescriptions

import de.gematik.ti.erp.app.api.ResourcePaging.ResourceResult
import de.gematik.ti.erp.app.prescription.repository.TaskRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class DemoTaskRepository(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TaskRepository {
    override suspend fun downloadTasks(profileId: ProfileIdentifier): Result<Int> =
        withContext(dispatcher) {
            delay(500)
            Result.success(0)
        }

    override suspend fun downloadResource(
        profileId: ProfileIdentifier,
        timestamp: String?,
        count: Int?
    ): Result<ResourceResult<Int>> = Result.success(ResourceResult(0, 0))

    override suspend fun syncedUpTo(profileId: ProfileIdentifier): Instant? = null
}
