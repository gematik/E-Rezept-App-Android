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

package de.gematik.ti.erp.app.analytics.usecase

import android.content.Context
import androidx.compose.runtime.Immutable
import de.gematik.ti.erp.app.features.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.InputStream

class AnalyticsUseCase( // TODO delete when new Alert/PopUp-Tracking is implemented
    private val context: Context
) {
    private val screenNames: List<AnalyticsUseCaseData.AnalyticsScreenName> by lazy {
        loadAnalyticsScreenNamesFromJSON(
            context.resources.openRawResourceFd(R.raw.analytics_identifier).createInputStream()
        )
    }

    val screenNamesFlow: Flow<List<AnalyticsUseCaseData.AnalyticsScreenName>>
        get() = flow {
            emit(screenNames)
        }
}

private fun loadAnalyticsScreenNamesFromJSON(
    jsonInput: InputStream
): List<AnalyticsUseCaseData.AnalyticsScreenName> =
    Json.parseToJsonElement(jsonInput.bufferedReader().readText()).jsonArray.map {
        val key = it.jsonObject.keys.first()
        print(key)
        val name = it.jsonObject.values.first().jsonObject["name"]!!.jsonPrimitive.content
        print(name)
        AnalyticsUseCaseData.AnalyticsScreenName(key, name)
    }

object AnalyticsUseCaseData {
    @Immutable
    @Serializable
    data class AnalyticsScreenName(
        val key: String,
        val name: String
    )
}
