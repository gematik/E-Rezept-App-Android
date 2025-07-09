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

import de.gematik.ti.erp.app.medicationplan.model.MedicationNotification
import de.gematik.ti.erp.app.medicationplan.model.ProfileWithSchedules
import de.gematik.ti.erp.app.medicationplan.repository.MedicationPlanRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.mapper.toModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalTime
import java.time.Duration

class LoadProfilesWithSchedulesUseCase(
    private val medicationPlanRepository: MedicationPlanRepository,
    private val profileRepository: ProfileRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(
        now: LocalDateTime? = null
    ): Flow<List<ProfileWithSchedules>> = combine(
        medicationPlanRepository.loadAllMedicationSchedules(),
        profileRepository.profiles()
    ) { schedules, profiles ->
        val filteredSchedules = schedules.filter { it.isActive }
        if (now != null) { // profiles with active schedules sorted by closest notification
            filteredSchedules.groupBy { it.profileId }
                .map { (profileId, schedulesForProfile) ->
                    val profile = profiles.first { it.id == profileId }
                    val scheduleWithSortedNotifications = schedulesForProfile.map { schedule ->
                        schedule.copy(
                            notifications = sortNotificationsByClosestTime(
                                now,
                                schedule.notifications
                            )
                        )
                    }
                    ProfileWithSchedules(
                        profile.toModel(),
                        scheduleWithSortedNotifications
                    )
                }.sortedBy { profileWithSchedules ->
                    profileWithSchedules.medicationSchedules
                        .flatMap { it.notifications }
                        .minOf { notification ->
                            Duration.between(
                                now.time.toJavaLocalTime(),
                                notification.time.toJavaLocalTime()
                            )
                        }.abs()
                }
        } else { // all profiles with active/inactive schedules
            schedules.groupBy { it.profileId }
                .map { (profileId, schedulesForProfile) ->
                    val profile = profiles.first { it.id == profileId }
                    ProfileWithSchedules(
                        profile.toModel(),
                        schedulesForProfile
                    )
                }
        }
    }.flowOn(dispatcher)

    private fun sortNotificationsByClosestTime(
        now: LocalDateTime,
        notifications: List<MedicationNotification>
    ): List<MedicationNotification> {
        val localTime = now.time
        val first = notifications.minBy {
            Duration.between(localTime.toJavaLocalTime(), it.time.toJavaLocalTime()).abs()
        }

        val next = notifications.filter { it.id != first.id }.sortedBy {
            val diff = Duration.between(localTime.toJavaLocalTime(), it.time.toJavaLocalTime())
            if (diff < Duration.ZERO) {
                diff.plusDays(1)
            } else {
                diff
            }
        }
        return listOf(first).plus(next)
    }
}
