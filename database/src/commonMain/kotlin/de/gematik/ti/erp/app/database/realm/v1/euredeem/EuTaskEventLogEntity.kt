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
package de.gematik.ti.erp.app.database.realm.v1.euredeem

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

/**
 * Realm entity representing a single EU task lifecycle event.
 *
 * Identifier semantics:
 * - id: An immutable UUID (or unique string) for this log entry. It does NOT equal the `taskId`.
 *       Each event row gets its own id so it can be uniquely addressed or deleted.
 * - taskId: Identifier of the individual task this event belongs to. Grouping all rows by the same
 *           `taskId` yields the chronological history of events that happened to that task.
 * - orderId: Correlation identifier that groups multiple `taskId`s belonging to the same order.
 *            Grouping by `orderId` lets you see all tasks (and their events) inside one order.
 *
 * Practical queries:
 * - All events for a task: filter where `taskId == <task>` to reconstruct its event trail.
 * - All tasks & events for an order: filter where `orderId == <order>` and then group by `taskId`.
 * - From the order perspective you can derive involved tasks; from the task perspective you can
 *   derive every state transition / event recorded irrespective of order grouping.
 *
 * Field notes:
 * - event: String representation built from EuEventType describing what happened.
 * - createdAt: Timestamp (RealmInstant) the event was recorded.
 */
class EuTaskEventLogEntityV1 : RealmObject {
    @PrimaryKey
    var id: String = ""
    var taskId: String = ""
    var orderId: String = ""
    var event: String = "" // Built from EuEventType
    var createdAt: RealmInstant = RealmInstant.now()
    var isUnread: Boolean = true
}
