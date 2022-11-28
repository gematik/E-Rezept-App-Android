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

package de.gematik.ti.erp.app.db.entities.v1.task

import de.gematik.ti.erp.app.db.entities.Cascading
import io.realm.kotlin.Deleteable
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject

@Suppress("LongParameterList")
class MedicationRequestEntityV1(
    var medication: MedicationEntityV1?,
    var dateOfAccident: RealmInstant?, // unfalltag
    var location: String?, // unfallbetrieb
    var emergencyFee: Boolean?, // emergency service fee = notfallgebuehr
    var substitutionAllowed: Boolean,
    var dosageInstruction: String?,
    var note: String?,
    var multiplePrescriptionInfo: MultiplePrescriptionInfoEntityV1?,
    var bvg: Boolean,
    var additionalFee: String?
) : RealmObject, Cascading {
    constructor() : this(
        medication = null,
        dateOfAccident = null,
        location = null,
        emergencyFee = null,
        substitutionAllowed = false,
        dosageInstruction = null,
        note = null,
        multiplePrescriptionInfo = null,
        bvg = false,
        additionalFee = null
    )

    override fun objectsToFollow(): Iterator<Deleteable> = iterator {
        medication?.let { yield(it) }
        multiplePrescriptionInfo?.let { yield(it) }
    }
}
