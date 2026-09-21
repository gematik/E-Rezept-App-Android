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

package de.gematik.ti.erp.app.database.room.v2.task.util

/**
 * Shared embeddable address columns, inlined into each parent entity's table via Room's @Embedded.
 * All fields are nullable so Room can return null when all address columns are absent.
 */
data class AddressEmbeddable(
    val line1: String? = null,
    val line2: String? = null,
    val postalCode: String? = null,
    val city: String? = null,
    /** Free-form extra address info (e.g. "c/o", floor) – absent from the design-doc diagram
     * but present in FHIR and AddressErpModel. */
    val additionalAddressInformation: String? = null
)
