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

package de.gematik.ti.erp.app.db.entities.v1.medicationplan

import de.gematik.ti.erp.app.db.entities.Cascading
import de.gematik.ti.erp.app.db.entities.v1.task.RatioEntityV1
import io.realm.kotlin.Deleteable
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject

class MedicationScheduleEntityV1 : RealmObject, Cascading {
    var start: String = ""
    var end: String = ""
    var isActive: Boolean = false
    var amount: RatioEntityV1? = null
    var profileId: String = ""
    var title: String = ""
    var body: String = ""
    var taskId: String = ""
    var notifications: RealmList<MedicationNotificationEntityV1> = realmListOf()

    override fun objectsToFollow(): Iterator<Deleteable> =
        iterator {
            yield(notifications)
            amount?.let { yield(it) }
        }
}
