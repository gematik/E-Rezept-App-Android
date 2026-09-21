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

import de.gematik.ti.erp.app.fhir.dispense.model.FhirDispenseDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// TODO https://service.gematik.de/browse/ERA-13166 we need one for Synced.Prescription and one for Synced.Diga
/**
 * copied [SyncedTaskData.MedicationDispense]
 */
@Serializable
@SerialName("MedicationDispense")
data class MedicationDispenseErpModel(
    val dispenseId: String? = null,
    val patientIdentifier: String = "",
    val medication: MedicationErpModel? = null,
    val deviceRequest: FhirDispenseDeviceRequestErpModel? = null,
    val wasSubstituted: Boolean = false,
    val dosageInstruction: String? = null,
    val performer: String = "",
    val whenHandedOver: FhirTemporal? = null,
    val pharmacyName: String? = null,
    val euCountryCode: String?
)
