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

package de.gematik.ti.erp.app.redeem.repository.datasource

import de.gematik.ti.erp.app.db.entities.v1.task.ScannedTaskEntityV1
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.db.toRealmInstant
import de.gematik.ti.erp.app.db.tryWrite
import io.realm.kotlin.Realm
import kotlinx.datetime.Clock

class DefaultRedeemLocalDataSource(private val realm: Realm) : RedeemLocalDataSource {

    override suspend fun markAsRedeemed(taskId: String) {
        realm.tryWrite<Unit> {
            queryFirst<ScannedTaskEntityV1>("taskId = $0", taskId)?.apply {
                this.redeemedOn = Clock.System.now().toRealmInstant()
            }
        }
    }
}
