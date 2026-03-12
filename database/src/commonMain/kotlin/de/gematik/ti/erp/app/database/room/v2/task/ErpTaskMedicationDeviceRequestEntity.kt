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

package de.gematik.ti.erp.app.database.room.v2.task

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import de.gematik.ti.erp.app.database.room.v2.task.util.InstantConverter
import kotlinx.datetime.Instant

@Entity(
    tableName = "task_med_device_requests",
    foreignKeys = [
        ForeignKey( // accident_info
            entity = ErpAccidentInfoEntity::class,
            parentColumns = ["accidentInfoId"],
            childColumns = ["accidentInfoId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
@TypeConverters(InstantConverter::class)
data class ErpTaskMedicationDeviceRequestEntity(
    @PrimaryKey val deviceRequestId: String,

    val accidentInfoId: String,
    val intent: String,
    val status: String,
    val pzn: String,
    val appName: String,

    // Using booleans is much friendlier to query & validate:
    val isSelfUse: Boolean,
    val isNew: Boolean,

    // DateTime → Instant
    val authoredOn: Instant
)
