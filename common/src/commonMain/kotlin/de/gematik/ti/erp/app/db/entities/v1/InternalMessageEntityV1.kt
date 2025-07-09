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

package de.gematik.ti.erp.app.db.entities.v1

import de.gematik.ti.erp.app.db.entities.enumName
import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationProfileV1
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore

class InternalMessageEntityV1 : RealmObject {
    var id: String = ""
    var sender: String = ""
    var text: String = ""
    var time: RealmInstant = RealmInstant.now()
    var tag: String = ""
    var isUnread: Boolean = true
    var _messageProfile: String = CommunicationProfileV1.InApp.toString()

    @delegate:Ignore
    var messageProfile: CommunicationProfileV1 by enumName(::_messageProfile)
    var version: String = ""
    var languageCode: String = ""
}

// Todo Remove in 1.30.0
class InternalMessageEntity : RealmObject {
    var lastVersion: String? = null
    var lastUpdatedVersion: String? = null
    var showWelcomeMessage: Boolean? = null
    var welcomeMessageTimeStamp: RealmInstant? = null
    var inAppMessageEntity: RealmList<InAppMessageEntity> = realmListOf()
}
