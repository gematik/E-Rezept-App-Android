/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.prescription.repository.model

import de.gematik.ti.erp.app.fhir.MedicationDetail
import java.time.LocalDateTime

data class SimpleMedicationDispense(
    val id: String,
    val taskId: String,
    val patientIdentifier: String, // KVNR
    val wasSubstituted: Boolean,
    val dosageInstruction: String?,
    val performer: String, // Telematik-ID
    val whenHandedOver: LocalDateTime,
    val medicationDetail: MedicationDetail
)
