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

package de.gematik.ti.erp.app.orderhealthcard.usecase

import android.content.Context
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.orderhealthcard.usecase.model.HealthCardOrderUseCaseData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.InputStream

class HealthCardOrderUseCase(
    private val context: Context
) {
    private val companies: List<HealthCardOrderUseCaseData.HealthInsuranceCompany> by lazy {
        loadHealthInsuranceContactsFromJSON(
            context.resources.openRawResourceFd(R.raw.health_insurance_contacts).createInputStream()
        ).sortedBy { it.name.lowercase() }
    }

    val healthInsuranceOrderContacts: Flow<List<HealthCardOrderUseCaseData.HealthInsuranceCompany>>
        get() = flow {
            emit(companies)
        }
}

fun loadHealthInsuranceContactsFromJSON(
    jsonInput: InputStream
): List<HealthCardOrderUseCaseData.HealthInsuranceCompany> =
    Json.decodeFromString(jsonInput.bufferedReader().readText())
