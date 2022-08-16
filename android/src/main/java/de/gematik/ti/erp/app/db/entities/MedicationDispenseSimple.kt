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
import androidx.room.Entity
import androidx.room.PrimaryKey

@Deprecated("Remove with Realm migration")
@Entity(tableName = "medicationDispense")
data class MedicationDispenseSimple(

    @ColumnInfo(name = "taskId")
    @PrimaryKey
    val taskId: String,
    val patientIdentifier: String, // KVNR
    val uniqueIdentifier: String, // PZN
    val wasSubstituted: Boolean,
    val text: String?,
    val type: String?,
    val dosageInstruction: String,
    val performer: String, // Telematik-ID
    val whenHandedOver: String // OffsetDateTime
)
