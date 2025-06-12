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

package de.gematik.ti.erp.app.fhir.dispense.model.erp

import de.gematik.ti.erp.app.fhir.common.model.erp.support.FhirRatioErpModel
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants.DispenseMedicationVersionType
import de.gematik.ti.erp.app.fhir.constant.dispense.FhirMedicationDispenseConstants.MedicationCategory
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirMedicationIdentifierErpModel
import de.gematik.ti.erp.app.utils.FhirTemporal
import kotlinx.serialization.Serializable

@Serializable
data class DispensedPznMedicationErpModel(
    override val text: String?,
    override val category: String?,
    override val form: String?,
    override val amount: FhirRatioErpModel?,
    override val isVaccine: Boolean?,
    override val lotNumber: String?,
    override val expirationDate: FhirTemporal?,
    // pzn-specific
    val contextualData: PznContextualData
) : DispensedMedicationErpModel()

@Serializable
data class PznContextualData(
    val identifier: FhirMedicationIdentifierErpModel?,
    val normSizeCode: String?
)

internal fun DispenseMedicationVersionType.toCategoryVersionMapper() =
    when (this) {
        DispenseMedicationVersionType.Pzn102,
        DispenseMedicationVersionType.Compounding102,
        DispenseMedicationVersionType.Ingredient102,
        DispenseMedicationVersionType.FreeText102 -> MedicationCategory.Version102

        DispenseMedicationVersionType.Pzn110,
        DispenseMedicationVersionType.Compounding110,
        DispenseMedicationVersionType.Ingredient110,
        DispenseMedicationVersionType.FreeText110 -> MedicationCategory.Version110

        DispenseMedicationVersionType.Epa14 -> MedicationCategory.Version14
    }
