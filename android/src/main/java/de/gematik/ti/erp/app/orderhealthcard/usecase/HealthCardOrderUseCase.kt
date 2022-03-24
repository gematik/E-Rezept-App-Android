/*
 * Copyright (c) 2022 gematik GmbH
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
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
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
        loadHealthInsuranceContactsFromJSON(
            context.resources.openRawResourceFd(R.raw.health_insurance_contacts).createInputStream()
        ).sortedBy { it.name.lowercase() }
    }

    fun healthInsuranceOrderContacts() = flow {
        emit(companies)
    }
}

fun loadHealthInsuranceContactsFromJSON(jsonInput: InputStream): List<HealthCardOrderUseCaseData.HealthInsuranceCompany> {
    val type = Types.newParameterizedType(
        List::class.java,
        HealthCardOrderUseCaseData.HealthInsuranceCompany::class.java
    )
    val moshiAdapter = Moshi.Builder().add(EmptyStringToNullAdapter).build().adapter<List<HealthCardOrderUseCaseData.HealthInsuranceCompany>>(type)
    return moshiAdapter.fromJson(jsonInput.bufferedReader().readText()) as List<HealthCardOrderUseCaseData.HealthInsuranceCompany>
}

object EmptyStringToNullAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): String? {
        return when (reader.peek()) {
            JsonReader.Token.STRING -> {
                val nextString = reader.nextString()
                if (nextString.equals("")) {
                    null
                } else {
                    nextString
                }
            }
            JsonReader.Token.NUMBER -> {
                error("${reader.nextLong()} was not a string")
            }
            else -> null
        }
    }
}
