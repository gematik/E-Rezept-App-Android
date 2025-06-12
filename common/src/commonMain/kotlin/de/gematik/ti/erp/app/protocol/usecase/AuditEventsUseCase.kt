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

package de.gematik.ti.erp.app.protocol.usecase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import de.gematik.ti.erp.app.fhir.audit.model.erp.FhirAuditEventErpModel
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.protocol.repository.AuditEventsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

const val AuditEventsInitialResultsPerPage = 50
const val AuditEventsNextResultsPerPage = 25

class AuditEventsUseCase(
    private val auditRepository: AuditEventsRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(profileId: ProfileIdentifier): Flow<PagingData<FhirAuditEventErpModel>> {
        return Pager(
            PagingConfig(
                pageSize = AuditEventsNextResultsPerPage,
                initialLoadSize = AuditEventsInitialResultsPerPage,
                maxSize = AuditEventsInitialResultsPerPage * 2
            ),
            pagingSourceFactory = {
                AuditEventPagingSource(
                    profileId = profileId,
                    auditRepository = auditRepository
                )
            }
        ).flow.flowOn(dispatcher)
    }
}
