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

package de.gematik.ti.erp.app.db.entities.v1.task

import de.gematik.ti.erp.app.db.entities.Cascading
import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import io.realm.kotlin.Deleteable
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject

class ScannedTaskEntityV1 : RealmObject, Cascading {
    var taskId: String = ""
    var accessCode: String = ""
    var name: String? = ""
    var index: Int = 0
    var scannedOn: RealmInstant = RealmInstant.MIN
    var redeemedOn: RealmInstant? = null
    var communications: RealmList<CommunicationEntityV1> = realmListOf()

    // back reference
    var parent: ProfileEntityV1? = null
    override fun objectsToFollow(): Iterator<Deleteable> =
        iterator {
            yield(communications)
        }
}
