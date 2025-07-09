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

package de.gematik.ti.erp.app.db.entities.v1.invoice

import de.gematik.ti.erp.app.db.entities.Cascading
import de.gematik.ti.erp.app.db.entities.byteArrayBase64
import de.gematik.ti.erp.app.db.entities.temporalAccessorNullable
import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.MedicationRequestEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.OrganizationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.PatientEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.PractitionerEntityV1
import de.gematik.ti.erp.app.utils.FhirTemporal
import io.realm.kotlin.Deleteable
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore

class PKVInvoiceEntityV1 : RealmObject, Cascading {

    var taskId: String = ""
    var accessCode: String = ""

    var timestamp: RealmInstant = RealmInstant.MIN

    var pharmacyOrganization: OrganizationEntityV1? = null
    var practitionerOrganization: OrganizationEntityV1? = null
    var patient: PatientEntityV1? = null
    var practitioner: PractitionerEntityV1? = null
    var medicationRequest: MedicationRequestEntityV1? = null

    var _whenHandedOver: String? = null

    var consumed: Boolean = false

    @delegate:Ignore
    var whenHandedOver: FhirTemporal? by temporalAccessorNullable(::_whenHandedOver)

    var invoice: InvoiceEntityV1? = null

    var _invoiceBinary: String = ""

    @delegate:Ignore
    var invoiceBinary: ByteArray by byteArrayBase64(::_invoiceBinary)

    var _kbvBinary: String = ""

    @delegate:Ignore
    var kbvBinary: ByteArray by byteArrayBase64(::_kbvBinary)

    var _erpPrBinary: String = ""

    @delegate:Ignore
    var erpPrBinary: ByteArray by byteArrayBase64(::_erpPrBinary)

    // back reference
    var parent: ProfileEntityV1? = null

    override fun objectsToFollow(): Iterator<Deleteable> =
        iterator {
            pharmacyOrganization?.let { yield(it) }
            invoice?.let { yield(it) }
            pharmacyOrganization?.let { yield(it) }
            practitionerOrganization?.let { yield(it) }
            practitioner?.let { yield(it) }
            patient?.let { yield(it) }
            medicationRequest?.let { yield(it) }
        }
}
