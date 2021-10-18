/*
 * Copyright (c) 2021 gematik GmbH
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
import dagger.hilt.android.qualifiers.ApplicationContext
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.orderhealthcard.usecase.model.HealthCardOrderUseCaseData
import kotlinx.coroutines.flow.flow
import java.io.InputStream
import javax.inject.Inject

class HealthCardOrderUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val companies: List<HealthCardOrderUseCaseData.HealthInsuranceCompany> by lazy {
        loadHealthInsuranceContactsFromCSV(
            context.resources.openRawResourceFd(R.raw.health_insurance_contacts).createInputStream()
        ).sortedBy { it.name.lowercase() }
    }

    fun healthInsuranceOrderContacts() = flow {
        emit(companies)
    }
}

fun loadHealthInsuranceContactsFromCSV(csv: InputStream): List<HealthCardOrderUseCaseData.HealthInsuranceCompany> {
    return csv.bufferedReader().useLines { lines ->
        lines.mapIndexedNotNull { index, line ->
            if (index > 0) {
                // ignore header

                val attrs = line.split(";").map {
                    if (it.isBlank()) {
                        null
                    } else {
                        it
                    }
                }

                HealthCardOrderUseCaseData.HealthInsuranceCompany(
                    name = requireNotNull(attrs[0]),
                    healthCardAndPinPhone = attrs[1],
                    healthCardAndPinMail = attrs[2],
                    healthCardAndPinUrl = attrs[3],
                    pinPhone = attrs[4],
                    pinMail = attrs[5],
                    pinUrl = attrs[6]
                )
            } else {
                null
            }
        }.toList()
    }
}
