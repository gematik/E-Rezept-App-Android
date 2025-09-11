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

package de.gematik.ti.erp.app.medicationplan.usecase

import de.gematik.ti.erp.app.medicationplan.alarm.MedicationPlanNotificationScheduler
import de.gematik.ti.erp.app.medicationplan.medicationSchedule1
import de.gematik.ti.erp.app.medicationplan.medicationSchedule2
import de.gematik.ti.erp.app.medicationplan.profile1
import de.gematik.ti.erp.app.medicationplan.profile2
import de.gematik.ti.erp.app.medicationplan.repository.DefaultMedicationPlanRepository
import de.gematik.ti.erp.app.medicationplan.repository.MedicationPlanLocalDataSource
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test

class GetActiveProfileWithSchedulesUseCaseTest {
    private val dispatcher = StandardTestDispatcher()
    private val medicationPlanLocalDataSource: MedicationPlanLocalDataSource = mockk()
    private val profileRepository: ProfileRepository = mockk()
    private lateinit var defaultMedicationPlanRepository: DefaultMedicationPlanRepository
    private lateinit var useCase: GetActiveProfileWithSchedulesUseCase
    private val scheduler: MedicationPlanNotificationScheduler = mockk()

    @BeforeTest
    fun setup() {
        MockKAnnotations.init(this)
        defaultMedicationPlanRepository = DefaultMedicationPlanRepository(
            localDataSource = medicationPlanLocalDataSource,
            scheduler = scheduler
        )
        useCase = GetActiveProfileWithSchedulesUseCase(
            profileRepository = profileRepository,
            medicationPlanRepository = defaultMedicationPlanRepository
        )
    }

    @Test
    fun `invoke with current date time - sorts ProfileWithSchedules correctly`() {
        val now = LocalDateTime.parse("2024-01-01T12:00:00")
        coEvery { profileRepository.profiles() } returns flowOf(listOf(profile1, profile2))
        coEvery { medicationPlanLocalDataSource.getAllMedicationSchedules() } returns
            flowOf(listOf(medicationSchedule1, medicationSchedule2))

        runTest(dispatcher) {
            val result = useCase.invoke(now).first()
            assertEquals(2, result.size)
            val firstProfileWithSchedules = result[0]
            assertEquals("PROFILE_ID2", firstProfileWithSchedules.profile.id) // Profile 2 should be first
            val firstNotifications = firstProfileWithSchedules.medicationSchedules[0].notifications
            assertEquals("3", firstNotifications[0].id)
            assertEquals("4", firstNotifications[1].id)

            val secondProfileWithSchedules = result[1]
            assertEquals("PROFILE_ID1", secondProfileWithSchedules.profile.id)
            val secondNotifications = secondProfileWithSchedules.medicationSchedules[0].notifications
            assertEquals("2", secondNotifications[0].id)
            assertEquals("1", secondNotifications[1].id) // overflow
        }
    }
}
