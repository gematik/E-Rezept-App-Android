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

package de.gematik.ti.erp.app.fhir.audit.parser

import de.gematik.ti.erp.app.data.bundle_audit_events_1_1
import de.gematik.ti.erp.app.data.bundle_audit_events_1_2
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class AuditEventsParserTest {

    private val parser = AuditEventsParser()

    @Test
    fun `test parse audit event version 1_2`() {
        val resource = Json.parseToJsonElement(bundle_audit_events_1_2)
        val erpModel = parser.extract(resource)
        assertEquals(1, erpModel?.auditEvents?.size)
        assertEquals("9361863d-fec0-4ba9-8776-7905cf1b0cfa", erpModel?.auditEvents?.first()?.id)
        assertEquals("160.123.456.789.123.58", erpModel?.auditEvents?.first()?.taskId)
        assertEquals("1-SMC-B-Testkarte-883110000095957", erpModel?.auditEvents?.first()?.telematikId)
        assertEquals(null, erpModel?.auditEvents?.first()?.kvnrNumber)
        assertEquals("bca172dc-495c-4e19-9c7b-7977739d9ce1", erpModel?.bundleId)
    }

    @Test
    fun `test parse audit event version 1_1`() {
        val resource = Json.parseToJsonElement(bundle_audit_events_1_1)
        val erpModel = parser.extract(resource)
        assertEquals(50, erpModel?.auditEvents?.size)
        assertEquals("01eb7f56-6820-a140-abdb-34aa9f2ab6ea", erpModel?.auditEvents?.first()?.id)
        assertEquals("160.000.000.024.934.42", erpModel?.auditEvents?.last()?.taskId)
        assertEquals(null, erpModel?.auditEvents?.first()?.telematikId)
        assertEquals("X764228532", erpModel?.auditEvents?.last()?.kvnrNumber)
        assertEquals("bca172dc-495c-4e19-9c7b-7977739d9ce1", erpModel?.bundleId)
    }
}
