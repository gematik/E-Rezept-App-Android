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

package de.gematik.ti.erp.app.database.room.v2.task.medicationrequest

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Embedded
import androidx.room.TypeConverters
import androidx.room.Upsert
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskEntity
import de.gematik.ti.erp.app.database.room.v2.task.util.InstantConverter

/**
 * Stores medication-request fields from the KBV prescription bundle.
 * Corresponds to [MedicationRequestEntityV1] in the Realm schema.
 * Shown as a red-dashed (TODO) entity in the v2 design doc.
 *
 * Full mapping is tracked under ERA-13163.
 */
@Entity(
    tableName = "medication_requests",
    foreignKeys = [
        ForeignKey(
            entity = ErpTaskEntity::class,
            parentColumns = ["taskId"],
            childColumns = ["taskId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ErpMedicationEntity::class,
            parentColumns = ["medicationId"],
            childColumns = ["medicationId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("taskId"),
        Index("medicationId")
    ]
)
@TypeConverters(InstantConverter::class)
data class ErpMedicationRequestEntity(
    @PrimaryKey
    val medicationRequestId: String,
    val taskId: String,

    // ── Core fields (from design-doc diagram) ──────────────────────────────────
    val emergencyFee: Boolean? = null,
    val quantity: Int = 0,
    val note: String? = null,
    /** Accident workplace / Unfallbetrieb. */
    val location: String? = null,

    // ── Extended fields (from BundleParser / Realm MedicationRequestEntityV1) ──
    /** Authored-on date stored as FhirTemporal.formattedString(). */
    val authoredOn: String? = null,
    val substitutionAllowed: Boolean = false,
    val dosageInstruction: String? = null,
    /** BVG / isSer flag from FHIR 1.2+. */
    val bvg: Boolean = false,
    /** Additional fee code (e.g. "0", "1", "2"). */
    val additionalFee: String? = null,

    // ── Accident fields ───────────────────────────────────────────────────────
    /** Date of accident stored as FhirTemporal.formattedString(). */
    val dateOfAccident: String? = null,
    /** Accident type (e.g. "Unfall", "Arbeitsunfall", "Berufskrankheit", "None"). */
    val accidentType: String = "None",

    // ── Teratogenic fields ────────────────────────────────────────────────────
    @Embedded(prefix = "teratogenic_")
    val teratogenicPrescription: ErpTeratogenicPrescriptionEmbeddable? = null,

    // ── FK ────────────────────────────────────────────────────────────────────
    /** References the medication nested within this request. */
    val medicationId: String? = null
)

@Dao
interface MedicationRequestDao {
    @Upsert
    suspend fun upsert(entity: ErpMedicationRequestEntity)

    @Query("SELECT * FROM medication_requests WHERE medicationRequestId = :id LIMIT 1")
    suspend fun getById(id: String): ErpMedicationRequestEntity?

    @Query("DELETE FROM medication_requests")
    suspend fun clearAll()
}
