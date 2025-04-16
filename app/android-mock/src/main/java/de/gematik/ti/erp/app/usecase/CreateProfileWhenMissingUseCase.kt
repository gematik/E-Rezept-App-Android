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

package de.gematik.ti.erp.app.usecase

import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class CreateProfileWhenMissingUseCase(
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        if (profileRepository.profiles().first().isEmpty()) {
            settingsRepository.saveOnboardingData(
                authentication = SettingsData.Authentication(
                    deviceSecurity = false,
                    password = SettingsData.Authentication.Password("password"),
                    failedAuthenticationAttempts = 0
                ),
                profileName = "Test",
                now = fixedInstant()
            )
        }
    }

    @Suppress("MagicNumber")
    private fun fixedInstant(): Instant {
        // Define the date and time, October 03, 2023, 00:00:00
        val year = 2023
        val month = 10
        val day = 3
        val hour = 0 // Midnight
        val minute = 0
        val second = 0

        // Create a LocalDateTime
        val localDateTime = LocalDateTime(year, month, day, hour, minute, second)

        // Convert LocalDateTime to Instant assuming UTC timezone
        return localDateTime.toInstant(TimeZone.UTC)
    }
}
