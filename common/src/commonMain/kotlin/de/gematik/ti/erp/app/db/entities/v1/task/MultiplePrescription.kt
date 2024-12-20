/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.db.entities.v1.task

import de.gematik.ti.erp.app.db.entities.Cascading
import io.realm.kotlin.Deleteable
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject

class MultiplePrescriptionInfoEntityV1(
    var indicator: Boolean,
    var numbering: RatioEntityV1?,
    var start: RealmInstant?,
    var end: RealmInstant?
) : RealmObject, Cascading {
    constructor() : this(indicator = false, numbering = null, start = RealmInstant.MIN, end = RealmInstant.MIN)

    override fun objectsToFollow(): Iterator<Deleteable> = iterator {
        numbering?.let { yield(it) }
    }
}
