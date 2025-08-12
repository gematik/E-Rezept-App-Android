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

package de.gematik.ti.erp.app.fhir.prescription.model

import de.gematik.ti.erp.app.fhir.support.FhirRatioErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// Represents a metadata (task-only info) bundle inside a task
@Serializable
data class FhirTaskMetaDataPayloadErpModel(val value: JsonElement)

// Represents a KBV bundle inside a task
@Serializable
data class FirTaskKbvPayloadErpModel(val value: JsonElement)

@Serializable
data class FhirMultiplePrescriptionInfoErpModel(
    val indicator: Boolean,
    val numbering: FhirRatioErpModel?,
    val start: FhirTemporal?,
    val end: FhirTemporal?
)

@Serializable
enum class FhirTaskMedicationCategoryErpModel(val code: String, val description: String) {
    ARZNEI_UND_VERBAND_MITTEL("00", "Arznei- und Verbandmittel"),
    BTM("01", "Betäubungsmittel (BTM)"),
    AMVV("02", "Arzneimittelverschreibungsverordnung (AMVV)"),
    SONSTIGES("03", "SONSTIGES"),
    UNKNOWN("", "Unknown");

    companion object {
        fun fromCode(code: String?): FhirTaskMedicationCategoryErpModel {
            return entries.find { it.code == code } ?: UNKNOWN
        }
    }
}

// https://simplifier.net/packages/hl7.fhir.r4.core/4.0.1/files/81826
@Serializable
sealed class RequestIntent {
    abstract val code: String

    @Serializable
    data object Proposal : RequestIntent() {
        override val code: String = "proposal"
    }

    @Serializable
    data object Plan : RequestIntent() {
        override val code: String = "plan"
    }

    @Serializable
    data object Directive : RequestIntent() {
        override val code: String = "directive"
    }

    @Serializable
    data object Order : RequestIntent() {
        override val code: String = "order"
    }

    @Serializable
    data object OriginalOrder : RequestIntent() {
        override val code: String = "original-order"
    }

    @Serializable
    data object ReflexOrder : RequestIntent() {
        override val code: String = "reflex-order"
    }

    @Serializable
    data object FillerOrder : RequestIntent() {
        override val code: String = "filler-order"
    }

    @Serializable
    data object InstanceOrder : RequestIntent() {
        override val code: String = "instance-order"
    }

    @Serializable
    data object Option : RequestIntent() {
        override val code: String = "option"
    }

    @Serializable
    data class UnknownIntent(val value: String) : RequestIntent() {
        override val code: String = value
    }

    companion object {
        private val knownIntents: Map<String, RequestIntent> by lazy {
            listOf(
                Proposal, Plan, Directive, Order, OriginalOrder, ReflexOrder,
                FillerOrder, InstanceOrder, Option
            ).associateBy { it.code }
        }

        fun fromCode(code: String?): RequestIntent {
            return code?.let {
                knownIntents[code] ?: UnknownIntent(code)
            } ?: UnknownIntent("unknown")
        }
    }
}
