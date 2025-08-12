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

package de.gematik.ti.erp.app.fhir.common.model.erp

import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension.Companion.findExtensionByUrl
import de.gematik.ti.erp.app.fhir.support.AccidentTypeRequestFrom
import de.gematik.ti.erp.app.fhir.support.FhirAccidentInformationErpModel
import de.gematik.ti.erp.app.fhir.support.FhirTaskAccidentType
import de.gematik.ti.erp.app.fhir.support.FhirTaskAccidentType.Companion.getFhirTaskAccidentByType
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import kotlinx.datetime.LocalDate

private object FhirAccidentExtensionUrls {
    const val ACCIDENT_EXTENSION_URL_102 = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Accident"
    const val ACCIDENT_EXTENSION_URL_110 = "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Accident"
}

private const val ACCIDENT_DATE = "unfalltag"
private const val ACCIDENT_LOCATION = "unfallbetrieb"
private const val ACCIDENT_TYPE = "unfallkennzeichen"

internal fun List<FhirExtension>.toAccidentInformation(
    from: AccidentTypeRequestFrom
) = FhirAccidentInformationErpModel(
    type = when (from) {
        AccidentTypeRequestFrom.MedicationRequest -> findAccidentType()
        AccidentTypeRequestFrom.DeviceRequest -> findAccidentTypeFromCode()
    },
    date = findAccidentDate(),
    location = findAccidentLocation()
)

internal fun List<FhirExtension>.accidentInformationExtension() =
    firstOrNull { it.url == FhirAccidentExtensionUrls.ACCIDENT_EXTENSION_URL_110 }
        ?: firstOrNull { it.url == FhirAccidentExtensionUrls.ACCIDENT_EXTENSION_URL_102 }

internal fun List<FhirExtension>.findAccidentDate() =
    findExtensionByUrl(ACCIDENT_DATE)?.valueDate?.let { FhirTemporal.LocalDate(LocalDate.parse(it)) }

internal fun List<FhirExtension>.findAccidentLocation() =
    findExtensionByUrl(ACCIDENT_LOCATION)?.valueString

internal fun List<FhirExtension>.findAccidentType() =
    findExtensionByUrl(ACCIDENT_TYPE)?.valueString?.let { getFhirTaskAccidentByType(it) } ?: findAccidentTypeFromCode()

private fun List<FhirExtension>.findAccidentTypeFromCode() =
    findExtensionByUrl(ACCIDENT_TYPE)?.valueCoding?.code?.let { getFhirTaskAccidentByType(it) } ?: FhirTaskAccidentType.None
