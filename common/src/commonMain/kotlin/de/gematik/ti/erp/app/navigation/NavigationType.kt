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

package de.gematik.ti.erp.app.navigation

import io.github.aakira.napier.Napier
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalSerializationApi::class)
val json = Json {
    ignoreUnknownKeys = false
    isLenient = true
    prettyPrint = false // enable this to see the json that is being converted
    allowStructuredMapKeys = true
    encodeDefaults = true
    explicitNulls = true
    coerceInputValues = true
    allowSpecialFloatingPointValues = true
    allowStructuredMapKeys = true
    useAlternativeNames = true
    coerceInputValues = true
    decodeEnumsCaseInsensitive = true
    classDiscriminator = "#class"
}

/**
 * This NavigationType allows us to parse a [T] type object into a JSON string
 * @return JSON String
 *
 * @throws Throwable at compile time if the object is not marked as @Serializable
 * @throws SerializationException in case of any encoding-specific error
 * @throws IllegalArgumentException if the encoded input does not comply format's specification
 */
@OptIn(ExperimentalContracts::class)
inline fun <reified T> T.toNavigationString(): String {
    contract {
        returns() implies (this@toNavigationString is Serializable)
    }
    return json.encodeToString<T>(this)
}

/**
 * Deserializes the given JSON [value] string into a corresponding [T] type representation.
 *
 * @throws [SerializationException] if the given string is not a valid JSON
 *
 * IMPORTANT: Please check if you need a specific PolymorphicSerializer.
 * References given below
 * @see de.gematik.ti.erp.app.utils.FhirTemporalSerializer
 * @see de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionData.MedicationInterfaceSerializer
 */
inline fun <reified T> fromNavigationString(value: String): T {
    try {
        val parsedValue = json.parseToJsonElement(value)
        return json.decodeFromJsonElement<T>(parsedValue)
    } catch (e: SerializationException) {
        Napier.e {
            """
                Check if value classes are used. 
                [kotlinx.serialization] does not work 
                well for polymorphic serialization with value classes. 
                Try changing them to data classes.
            """.trimIndent()
        }
        throw e
    }
}
