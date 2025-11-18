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

package de.gematik.ti.erp.app.database.realm.v1.task.entity

import de.gematik.ti.erp.app.database.realm.utils.Cascading
import de.gematik.ti.erp.app.database.realm.utils.temporalAccessorNullable
import de.gematik.ti.erp.app.database.realm.v1.AddressEntityV1
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import io.realm.kotlin.Deleteable
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore

class MedicationDispenseEntityV1 : RealmObject, Cascading {
    var dispenseId: String = ""
    var patientIdentifier: String = "" // KVNR
    var medication: MedicationEntityV1? = null
    var deviceRequest: DeviceRequestDispenseEntityV1? = null
    var wasSubstituted: Boolean = false
    var dosageInstruction: String? = null
    var performer: String = "" // Telematik-ID
    var pharmacyName: String? = null
    var address: AddressEntityV1? = null
    var _handedOverOn: String? = null

    @delegate:Ignore
    var handedOverOn: FhirTemporal? by temporalAccessorNullable(::_handedOverOn)

    override fun objectsToFollow(): Iterator<Deleteable> = iterator {
        medication?.let { yield(it) }
        deviceRequest?.let { yield(it) }
        address?.let { yield(it) }
    }
}
