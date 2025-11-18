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

import de.gematik.ti.erp.app.database.realm.utils.enumName
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore

enum class CommunicationProfileV1 {
    ErxCommunicationDispReq, ErxCommunicationReply, Unknown, InApp, EuOrder
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
