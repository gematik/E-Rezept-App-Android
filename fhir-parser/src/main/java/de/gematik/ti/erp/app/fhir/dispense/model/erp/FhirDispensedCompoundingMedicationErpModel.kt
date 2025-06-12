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
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirMedicationIdentifierErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirMedicationIngredientErpModel
import de.gematik.ti.erp.app.utils.FhirTemporal
import kotlinx.serialization.Serializable

@Serializable
data class FhirDispensedCompoundingMedicationErpModel(
    override val text: String?,
    override val category: String?,
    override val form: String?,
    override val amount: FhirRatioErpModel?,
    override val isVaccine: Boolean?,
    override val lotNumber: String?,
    override val expirationDate: FhirTemporal?,
    // compounding specific
    val contextualData: CompoundingContextualData
) : DispensedMedicationErpModel()

@Serializable
data class CompoundingContextualData(
    val identifier: FhirMedicationIdentifierErpModel?,
    val manufacturingInstructions: String?,
    val packaging: String?,
    val ingredients: List<FhirMedicationIngredientErpModel> = emptyList()
)
