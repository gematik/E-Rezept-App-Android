package de.gematik.ti.erp.app.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activeProfile")
data class ActiveProfile(
    @PrimaryKey
    val id: Int = 0,
    val profileName: String,
)
