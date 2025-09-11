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

import kotlinx.serialization.Serializable

@Serializable
data class FhirTaskKbvMedicationProfileErpModel(
    val type: ErpMedicationProfileType,
    val version: ErpMedicationProfileVersion
) {
    companion object {
        fun restoreTaskKbvMedicationProfileErpModel(
            typeString: String?,
            versionString: String?
        ): FhirTaskKbvMedicationProfileErpModel {
            val type = runCatching {
                if (!typeString.isNullOrBlank()) ErpMedicationProfileType.valueOf(typeString)
                else ErpMedicationProfileType.Unknown
            }.getOrElse { ErpMedicationProfileType.Unknown }

            val version = runCatching {
                if (!versionString.isNullOrBlank()) ErpMedicationProfileVersion.valueOf(versionString)
                else ErpMedicationProfileVersion.Unknown
            }.getOrElse { ErpMedicationProfileVersion.Unknown }

            return FhirTaskKbvMedicationProfileErpModel(type, version)
        }
    }
}

@Serializable
enum class ErpMedicationProfileType {
    PZN,
    FreeText,
    Compounding,
    Ingredient,
    Unknown;
}

@Serializable
enum class ErpMedicationProfileVersion {
    V_102,
    V_110,
    V_12,
    V_13,
    Unknown;
}
