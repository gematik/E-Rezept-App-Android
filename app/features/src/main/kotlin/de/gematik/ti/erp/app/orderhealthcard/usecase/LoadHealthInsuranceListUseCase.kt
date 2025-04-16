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

package de.gematik.ti.erp.app.orderhealthcard.usecase

import android.content.Context
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.orderhealthcard.presentation.HealthInsuranceCompany
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import java.io.InputStream

class LoadHealthInsuranceListUseCase(
    private val context: Context
) {
    private val companies: List<HealthInsuranceCompany> by lazy {
        loadHealthInsuranceContactsFromJSON(
            context.resources.openRawResourceFd(R.raw.health_insurance_contacts).createInputStream()
        ).sortedBy { it.name.lowercase() }
    }

    operator fun invoke(): Flow<List<HealthInsuranceCompany>> = flow {
        emit(companies)
    }
}

fun loadHealthInsuranceContactsFromJSON(
    jsonInput: InputStream
): List<HealthInsuranceCompany> =
    Json.decodeFromString(jsonInput.bufferedReader().readText())
