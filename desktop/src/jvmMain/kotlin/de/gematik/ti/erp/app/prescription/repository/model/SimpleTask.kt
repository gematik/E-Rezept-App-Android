/*
 * Copyright (c) 2023 gematik GmbH
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

import org.hl7.fhir.r4.model.Bundle as FhirBundle
import java.time.LocalDate
import java.time.LocalDateTime

data class SimpleTask(
    val taskId: String,
    val lastModified: LocalDateTime? = null,

    val organization: String, // an organization can contain multiple authors
    val medicationText: String?,
    val expiresOn: LocalDate,
    val acceptUntil: LocalDate,
    val authoredOn: LocalDateTime,

    // synced only
    val status: String? = null,

    val rawKBVBundle: FhirBundle
)
