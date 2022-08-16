/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.db.entities

import androidx.room.ColumnInfo
import androidx.room.ColumnInfo.BLOB
import androidx.room.Entity
import androidx.room.PrimaryKey

@Deprecated("Remove with Realm migration")
@Entity(tableName = "tasks")
data class Task(
    @ColumnInfo(name = "taskId")
    @PrimaryKey
    val taskId: String,
    @ColumnInfo(index = true)
    val profileName: String,
    val accessCode: String? = null,
    val lastModified: String? = null, // Instant
    val expiresOn: String? = null, // LocalDate
    val acceptUntil: String? = null, // LocalDate
    val authoredOn: String? = null, // OffsetDateTime

    // synced only
    val status: TaskStatus? = null,

    // scan only
    val scannedOn: String? = null, // OffsetDateTime
    val scanSessionEnd: String? = null, // OffsetDateTime
    val nrInScanSession: Int? = null, // serial number of scanned tasks (e.g. 1, 2, ... 5)
    val redeemedOn: String? = null, // OffsetDateTime

    @ColumnInfo(typeAffinity = BLOB)
    val rawKBVBundle: ByteArray? = null
)

enum class TaskStatus {
    Ready, InProgress, Completed, Other;

    companion object {
        fun fromFhirTask(status: org.hl7.fhir.r4.model.Task.TaskStatus) =
            when (status) {
                org.hl7.fhir.r4.model.Task.TaskStatus.READY -> Ready
                org.hl7.fhir.r4.model.Task.TaskStatus.INPROGRESS -> InProgress
                org.hl7.fhir.r4.model.Task.TaskStatus.COMPLETED -> Completed
                else -> Other
            }
    }
}
