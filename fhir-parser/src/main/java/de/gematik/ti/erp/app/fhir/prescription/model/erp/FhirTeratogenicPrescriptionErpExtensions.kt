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

package de.gematik.ti.erp.app.fhir.prescription.model.erp

import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension.Companion.findExtensionByUrl
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTeratogenicPrescriptionErpModel

private object FhirTeratogenicExtensionUrls {
    const val TERATOGENIC_EXTENSION_URL_140 = "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Teratogenic"
}

private const val OFF_LABEL = "Off-Label"
private const val GEBAERFAEHIGE_FRAU = "GebaerfaehigeFrau"
private const val EINHALTUNG_SICHERHEITSMASSNAHMEN = "EinhaltungSicherheitsmassnahmen"
private const val AUSHAENDIGUNG_INFORMATIONSMATERIALIEN = "AushaendigungInformationsmaterialien"
private const val ERKLAERUNG_SACHKENNTNIS = "ErklaerungSachkenntnis"

internal fun List<FhirExtension>.findTeratogenicPrescription(): FhirExtension? = findExtensionByUrl(FhirTeratogenicExtensionUrls.TERATOGENIC_EXTENSION_URL_140)

internal fun List<FhirExtension>.toTeratogenicPrescription(): FhirTeratogenicPrescriptionErpModel =
    FhirTeratogenicPrescriptionErpModel(
        offLabel = findExtensionByUrl(OFF_LABEL)?.valueBoolean ?: false,
        gebaerfaehigeFrau = findExtensionByUrl(GEBAERFAEHIGE_FRAU)?.valueBoolean ?: false,
        einhaltungSicherheitsmassnahmen = findExtensionByUrl(EINHALTUNG_SICHERHEITSMASSNAHMEN)?.valueBoolean ?: false,
        aushaendigungInformationsmaterialien = findExtensionByUrl(AUSHAENDIGUNG_INFORMATIONSMATERIALIEN)?.valueBoolean ?: false,
        erklaerungSachkenntnis = findExtensionByUrl(ERKLAERUNG_SACHKENNTNIS)?.valueBoolean ?: false
    )
