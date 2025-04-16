/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.datasource

import de.gematik.ti.erp.app.datasource.data.MockPrescriptionInfo
import de.gematik.ti.erp.app.datasource.data.MockProfileInfo.mockProfile01
import de.gematik.ti.erp.app.model.MockProfileLinkedCommunication
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import kotlinx.coroutines.flow.MutableStateFlow

const val INDEX_OUT_OF_BOUNDS = -1

class MockDataSource {
// todo: include pharmacies data source here in future!

    val profiles: MutableStateFlow<MutableList<ProfilesData.Profile>> =
        MutableStateFlow(mutableListOf(mockProfile01))

    val syncedTasks: MutableStateFlow<MutableList<SyncedTaskData.SyncedTask>> =
        MutableStateFlow(
            mutableListOf(
                MockPrescriptionInfo.MockSyncedPrescription.syncedTask(
                    mockProfile01.id,
                    status = SyncedTaskData.TaskStatus.Ready,
                    index = 0
                ),
                MockPrescriptionInfo.MockSyncedPrescription.syncedTask(
                    mockProfile01.id,
                    status = SyncedTaskData.TaskStatus.InProgress,
                    index = 1
                )
            )
        )

    val scannedTasks: MutableStateFlow<MutableList<ScannedTaskData.ScannedTask>> =
        MutableStateFlow(
            mutableListOf(
                MockPrescriptionInfo.MockScannedPrescription.mockScannedTask01,
                MockPrescriptionInfo.MockScannedPrescription.mockScannedTask02
            )
        )

    val communications: MutableStateFlow<MutableList<MockProfileLinkedCommunication>> =
        MutableStateFlow(mutableListOf())
}
