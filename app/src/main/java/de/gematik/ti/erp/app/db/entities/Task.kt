/*
 * Copyright (c) 2021 gematik GmbH
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
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * @param authoredOn  this is actually the authoredOn value of the medication request, cause this is what we want to display
 */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = arrayOf("name"),
            childColumns = arrayOf("profileName"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    tableName = "tasks"
)
data class Task(
    @ColumnInfo(name = "taskId")
    @PrimaryKey
    val taskId: String,
    @ColumnInfo(index = true)
    val profileName: String,
    val accessCode: String,
    val lastModified: OffsetDateTime? = null,

    val organization: String? = null, // an organization can contain multiple authors
    val medicationText: String? = null,
    val expiresOn: LocalDate? = null,
    val acceptUntil: LocalDate? = null,
    val authoredOn: OffsetDateTime? = null,

    // synced only
    val status: String? = null,

    // scan only
    val scannedOn: OffsetDateTime? = null,
    val scanSessionEnd: OffsetDateTime? = null,
    val nrInScanSession: Int? = null, // serial number of scanned tasks (e.g. 1, 2, ... 5)
    val scanSessionName: String? = null,
    val redeemedOn: OffsetDateTime? = null,

    @ColumnInfo(typeAffinity = BLOB)
    val rawKBVBundle: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Task

        if (taskId != other.taskId) return false
        if (accessCode != other.accessCode) return false
        if (lastModified != other.lastModified) return false
        if (organization != other.organization) return false
        if (medicationText != other.medicationText) return false
        if (expiresOn != other.expiresOn) return false
        if (acceptUntil != other.acceptUntil) return false
        if (authoredOn != other.authoredOn) return false
        if (scannedOn != other.scannedOn) return false
        if (scanSessionEnd != other.scanSessionEnd) return false
        if (nrInScanSession != other.nrInScanSession) return false
        if (scanSessionName != other.scanSessionName) return false
        if (redeemedOn != other.redeemedOn) return false
        if (rawKBVBundle != null) {
            if (other.rawKBVBundle == null) return false
            if (!rawKBVBundle.contentEquals(other.rawKBVBundle)) return false
        } else if (other.rawKBVBundle != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = taskId.hashCode()
        result = 31 * result + accessCode.hashCode()
        result = 31 * result + (lastModified?.hashCode() ?: 0)
        result = 31 * result + (organization?.hashCode() ?: 0)
        result = 31 * result + (medicationText?.hashCode() ?: 0)
        result = 31 * result + (expiresOn?.hashCode() ?: 0)
        result = 31 * result + (acceptUntil?.hashCode() ?: 0)
        result = 31 * result + (authoredOn?.hashCode() ?: 0)
        result = 31 * result + (scannedOn?.hashCode() ?: 0)
        result = 31 * result + (scanSessionEnd?.hashCode() ?: 0)
        result = 31 * result + (nrInScanSession ?: 0)
        result = 31 * result + (scanSessionName?.hashCode() ?: 0)
        result = 31 * result + (redeemedOn?.hashCode() ?: 0)
        result = 31 * result + (rawKBVBundle?.contentHashCode() ?: 0)
        return result
    }
}
