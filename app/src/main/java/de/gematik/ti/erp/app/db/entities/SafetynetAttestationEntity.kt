package de.gematik.ti.erp.app.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "safetynetattestations")
data class SafetynetAttestationEntity(
    @PrimaryKey
    val id: Int = 0,
    val jws: String,
    val ourNonce: ByteArray
)
