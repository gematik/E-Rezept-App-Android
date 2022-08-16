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

package de.gematik.ti.erp.app.db.daos

import androidx.room.Dao
import androidx.room.Query
import de.gematik.ti.erp.app.db.entities.Communication
import de.gematik.ti.erp.app.db.entities.MedicationDispenseSimple
import de.gematik.ti.erp.app.db.entities.ProfileEntity
import de.gematik.ti.erp.app.db.entities.Settings
import de.gematik.ti.erp.app.db.entities.Task

@Dao
interface MigrationDao {
    @Query("SELECT * FROM settings")
    fun getSettings(): Settings?

    @Query("SELECT * FROM profiles")
    fun getProfiles(): List<ProfileEntity>

    @Query("SELECT * FROM communications")
    fun getCommunications(): List<Communication>

    @Query("SELECT * FROM tasks")
    fun getTasks(): List<Task>

    @Query("SELECT * FROM medicationDispense")
    fun getMedicationDispenses(): List<MedicationDispenseSimple>
}
