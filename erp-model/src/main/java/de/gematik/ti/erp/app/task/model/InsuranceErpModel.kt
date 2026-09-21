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

package de.gematik.ti.erp.app.task.model

import io.github.aakira.napier.Napier
import kotlinx.serialization.Serializable

/**
 * copied [SyncedTaskData.InsuranceInformation]
 */
@Serializable
data class InsuranceErpModel(
    val name: String? = null,
    val status: String? = null,
    val identifierNumber: String? = null,
    val coverageType: InsuranceErpModelCoverageType
)

// TODO: Check if we need one more mapping here
/**
 * copied [SyncedTaskData.CoverageType]
 */
enum class InsuranceErpModelCoverageType {
    GKV, // Gesetzliche Krankenversicherung
    PKV, // Private Krankenversicherung
    BG, // Berufsgenossenschaft
    SEL, // Selbstzahler
    SOZ, // Sozialamt
    GPV, // Gesetzliche Pflegeversicherung
    PPV, // Private Pflegeversicherung
    BEI, // Beihilfe
    UK, // Unfallkasse
    UNKNOWN
    ;

    companion object {
        fun mapTo(value: String?): InsuranceErpModelCoverageType =
            try {
                valueOf(value ?: UNKNOWN.toString())
            } catch (e: Throwable) {
                Napier.e { "error on parsing ${e.message}" }
                UNKNOWN
            }
    }
}
