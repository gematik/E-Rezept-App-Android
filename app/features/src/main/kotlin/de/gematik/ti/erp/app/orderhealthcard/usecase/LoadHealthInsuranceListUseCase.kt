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

package de.gematik.ti.erp.app.orderhealthcard.usecase

import android.content.Context
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.orderhealthcard.presentation.HealthInsuranceCompany
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream

class LoadHealthInsuranceListUseCase(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val companiesLock = Mutex()

    @Volatile
    private var cachedCompanies: List<HealthInsuranceCompany>? = null

    private suspend fun loadCompanies(): List<HealthInsuranceCompany> {
        cachedCompanies?.let { return it }

        return companiesLock.withLock {
            cachedCompanies?.let { return it }

            // Explicitly switch to the IO dispatcher
            withContext(dispatcher) {
                val inputStream = context.resources
                    .openRawResourceFd(R.raw.health_insurance_contacts)
                    .createInputStream()

                val loaded = loadHealthInsuranceContactsFromJSON(inputStream)
                    .sortedBy { it.name.lowercase() }

                cachedCompanies = loaded
                loaded
            }
        }
    }

    operator fun invoke(): Flow<List<HealthInsuranceCompany>> = flow {
        emit(loadCompanies())
    }
}

fun loadHealthInsuranceContactsFromJSON(
    jsonInput: InputStream
): List<HealthInsuranceCompany> =
    Json.decodeFromString(jsonInput.bufferedReader().readText())
