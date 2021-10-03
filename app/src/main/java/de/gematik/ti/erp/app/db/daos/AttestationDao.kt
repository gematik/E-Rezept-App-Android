package de.gematik.ti.erp.app.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.gematik.ti.erp.app.db.entities.SafetynetAttestationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttestationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttestation(attestationEntity: SafetynetAttestationEntity)

    @Query("SELECT * FROM safetynetattestations")
    fun getAllAttestations(): Flow<List<SafetynetAttestationEntity>>
}
