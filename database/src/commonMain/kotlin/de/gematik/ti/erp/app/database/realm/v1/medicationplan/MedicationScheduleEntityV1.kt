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

package de.gematik.ti.erp.app.database.realm.v1.medicationplan

import de.gematik.ti.erp.app.database.realm.utils.Cascading
import de.gematik.ti.erp.app.database.realm.v1.task.entity.RatioEntityV1
import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationScheduleDurationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationScheduleIntervalEntityV1
import io.realm.kotlin.Deleteable
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class MedicationScheduleEntityV1 : RealmObject, Cascading {
    @PrimaryKey
    var taskId: String = ""
    var amount: RatioEntityV1? = null

    var isActive: Boolean = false
    var profileId: String = ""

    var duration: MedicationScheduleDurationEntityV1? = null
    var interval: MedicationScheduleIntervalEntityV1? = null

    var title: String = ""
    var body: String = ""

    var notifications: RealmList<MedicationScheduleNotificationEntityV1> = realmListOf()

    override fun objectsToFollow(): Iterator<Deleteable> =
        iterator {
            yield(notifications)
            duration?.let { yield(it) }
            interval?.let { yield(it) }
            amount?.let { yield(it) }
        }
}
