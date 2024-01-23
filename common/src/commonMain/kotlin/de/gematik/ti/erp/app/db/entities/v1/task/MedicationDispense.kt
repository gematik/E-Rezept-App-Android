/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.db.entities.v1.task

import de.gematik.ti.erp.app.db.entities.Cascading
import de.gematik.ti.erp.app.db.entities.temporalAccessorNullable
import de.gematik.ti.erp.app.utils.FhirTemporal
import io.realm.kotlin.Deleteable
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore

class MedicationDispenseEntityV1 : RealmObject, Cascading {
    var dispenseId: String = ""
    var patientIdentifier: String = "" // KVNR
    var medication: MedicationEntityV1? = null
    var wasSubstituted: Boolean = false
    var dosageInstruction: String? = null
    var performer: String = "" // Telematik-ID

    var _handedOverOn: String? = null

    @delegate:Ignore
    var handedOverOn: FhirTemporal? by temporalAccessorNullable(::_handedOverOn)

    override fun objectsToFollow(): Iterator<Deleteable> = iterator {
        medication?.let { yield(it) }
    }
}
