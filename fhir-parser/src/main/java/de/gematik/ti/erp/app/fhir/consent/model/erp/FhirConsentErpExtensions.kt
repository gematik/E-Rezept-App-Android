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

package de.gematik.ti.erp.app.fhir.consent.model.erp

import de.gematik.ti.erp.app.fhir.consent.model.FhirCodeableConceptErp
import de.gematik.ti.erp.app.fhir.consent.model.FhirCodingErp
import de.gematik.ti.erp.app.fhir.consent.model.FhirConsentErpModel
import de.gematik.ti.erp.app.fhir.consent.model.orignal.FhirCodeableConcept
import de.gematik.ti.erp.app.fhir.consent.model.orignal.FhirCoding
import de.gematik.ti.erp.app.fhir.consent.model.orignal.FhirConsentModel

internal fun FhirConsentModel.toErpModel(): FhirConsentErpModel =
    FhirConsentErpModel(
        resourceType = resourceType,
        id = id,
        status = status,
        category = category.map { it.toErp() },
        policyRule = policyRule.toErp(),
        dateTime = this.dateTime,
        scope = scope.toErp()
    )

private fun FhirCodeableConcept.toErp(): FhirCodeableConceptErp =
    FhirCodeableConceptErp(
        coding = coding.map { it.toErp() }
    )

private fun FhirCoding.toErp(): FhirCodingErp =
    FhirCodingErp(
        system = system,
        code = code,
        display = display
    )
