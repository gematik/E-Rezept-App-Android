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

import de.gematik.ti.erp.app.db.entities.enumName
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore

enum class CommunicationProfileV1 {
    ErxCommunicationDispReq, ErxCommunicationReply, Unknown
}

class CommunicationEntityV1 : RealmObject {
    var taskId: String = ""
    var communicationId: String = ""

    var orderId: String = ""

    var _profile: String = CommunicationProfileV1.ErxCommunicationDispReq.toString()

    @delegate:Ignore
    var profile: CommunicationProfileV1 by enumName(::_profile)

    var sentOn: RealmInstant = RealmInstant.MIN
    var sender: String = ""
    var recipient: String = ""
    var payload: String? = null

    var consumed: Boolean = false

    // back reference
    var parent: SyncedTaskEntityV1? = null
}
