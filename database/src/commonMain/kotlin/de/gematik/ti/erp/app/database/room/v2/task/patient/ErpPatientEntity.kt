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

package de.gematik.ti.erp.app.database.room.v2.task.patient

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskEntity
import de.gematik.ti.erp.app.database.room.v2.task.util.AddressEmbeddable

@Entity(
    tableName = "patient",
    foreignKeys = [
        ForeignKey(
            entity = ErpTaskEntity::class,
            parentColumns = ["taskId"],
            childColumns = ["taskId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId")]
)
data class ErpPatientEntity(
    @PrimaryKey
    val patientId: String,
    val taskId: String,
    val name: String?,
    /** Date of birth stored as FhirTemporal.formattedString() – column named 'dob' per design doc. */
    val dob: String?,
    val insuranceIdentifier: String?,
    @Embedded val address: AddressEmbeddable? = null
)
