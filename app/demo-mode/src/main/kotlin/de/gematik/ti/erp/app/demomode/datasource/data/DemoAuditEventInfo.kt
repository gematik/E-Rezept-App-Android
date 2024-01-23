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

package de.gematik.ti.erp.app.demomode.datasource.data

import de.gematik.ti.erp.app.protocol.model.AuditEventData
import kotlinx.datetime.Clock
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.DurationUnit.DAYS
import kotlin.time.DurationUnit.HOURS
import kotlin.time.DurationUnit.MINUTES
import kotlin.time.toDuration

object DemoAuditEventInfo {

    private val randomDurationUnit = listOf(MINUTES, HOURS, DAYS)
    private val randomInt = Random.nextInt(10, 60)

    private fun Int.randomDuration(): Duration {
        val randomIndex = (randomDurationUnit.indices).random()
        val unit = randomDurationUnit[randomIndex]
        return this.toDuration(unit)
    }

    internal fun downloadDispense(taskId: String = UUID.randomUUID().toString()) = AuditEventData.AuditEvent(
        auditId = UUID.randomUUID().toString(),
        taskId = taskId,
        description = "Max Mustermann dowloaded a medication dispense list",
        timestamp = Clock.System.now().minus(randomInt.randomDuration())
    )

    internal fun downloadPrescription(taskId: String = UUID.randomUUID().toString()) =
        AuditEventData.AuditEvent(
            auditId = UUID.randomUUID().toString(),
            taskId = taskId,
            description = "Max Mustermann dowloaded a prescription $taskId",
            timestamp = Clock.System.now().minus(randomInt.randomDuration())
        )
}
