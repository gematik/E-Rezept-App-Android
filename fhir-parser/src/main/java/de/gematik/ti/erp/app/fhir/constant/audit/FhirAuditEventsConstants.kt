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

package de.gematik.ti.erp.app.fhir.constant.audit

object FhirAuditEventsConstants {

    internal const val AUDIT_EVENT_VERSION_1_1_PRESCRIPTION_IDENTIFIER = "https://gematik.de/fhir/NamingSystem/PrescriptionID"
    internal const val AUDIT_EVENT_VERSION_1_2_PRESCRIPTION_IDENTIFIER = "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId"
    internal const val AUDIT_EVENT_TELEMATIK_ID_IDENTIFIER = "https://gematik.de/fhir/sid/telematik-id"
    internal const val AUDIT_EVENT_VERSION_1_1_KVNR_IDENTIFIER = "http://fhir.de/NamingSystem/gkv/kvid-10"
    internal const val AUDIT_EVENT_VERSION_1_2_KVNR_IDENTIFIER = "http://fhir.de/sid/gkv/kvid-10"

    internal fun String.sanitizeAsAuditEventDescription(): String {
        return removeSurrounding(
            "<div xmlns=\"http://www.w3.org/1999/xhtml\">",
            "</div>"
        )
    }
}
