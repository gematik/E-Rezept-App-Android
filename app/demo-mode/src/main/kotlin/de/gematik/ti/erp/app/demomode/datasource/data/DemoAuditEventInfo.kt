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

package de.gematik.ti.erp.app.demomode.datasource.data

import de.gematik.ti.erp.app.fhir.audit.model.erp.FhirAuditEventErpModel
import de.gematik.ti.erp.app.utils.FhirTemporal
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

    internal fun downloadDispense(taskId: String = UUID.randomUUID().toString()) = FhirAuditEventErpModel(
        id = UUID.randomUUID().toString(),
        taskId = taskId,
        description = "Sie haben eine Medikamentenabgabeliste heruntergeladen",
        timestamp = FhirTemporal.Instant(
            Clock.System.now().minus(randomInt.randomDuration())
        ),
        telematikId = "1.2.3.4.5.6.7.8.9.0",
        kvnrNumber = null
    )

    internal fun downloadPrescription(taskId: String = UUID.randomUUID().toString()) =
        FhirAuditEventErpModel(
            id = UUID.randomUUID().toString(),
            taskId = taskId,
            description = "Sie haben ein Rezept heruntergeladen mit taskId $taskId",
            timestamp = FhirTemporal.Instant(
                Clock.System.now().minus(randomInt.randomDuration())
            ),
            telematikId = null,
            kvnrNumber = "X11223344"
        )
}
