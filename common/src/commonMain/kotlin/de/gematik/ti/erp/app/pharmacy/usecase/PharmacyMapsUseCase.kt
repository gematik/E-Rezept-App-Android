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

package de.gematik.ti.erp.app.pharmacy.usecase

import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.model.PharmacyMapsSearchDataErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyDetailsErpModel
import de.gematik.ti.erp.app.pharmacy.usecase.mapper.toModel
import de.gematik.ti.erp.app.pharmacy.usecase.mapper.toPharmacyFilter
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PharmacyMapsUseCase(
    private val repository: PharmacyRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    @Suppress("MagicNumber")
    suspend operator fun invoke(
        searchData: PharmacyMapsSearchDataErpModel,
        forcedRadius: Double? = null
    ): List<PharmacyDetailsErpModel> =
        withContext(dispatcher) {
            try {
                val initialResult = repository.searchPharmacies(
                    filter = searchData.toPharmacyFilter(forcedRadius)
                ).getOrThrow()

                initialResult.entries.toModel(type = initialResult.type)
            } catch (e: Throwable) {
                Napier.e { "silent exception ${e.stackTraceToString()}" }
                emptyList()
            }
        }
}
