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

package de.gematik.ti.erp.app.messages.domain.usecase

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.messages.domain.repository.ChangeLogLocalDataSource
import de.gematik.ti.erp.app.messages.repository.InternalMessagesRepository
import kotlinx.coroutines.flow.firstOrNull

class UpdateInternalMessagesUseCase(
    private val internalMessagesRepository: InternalMessagesRepository,
    private val changeLogDataSource: ChangeLogLocalDataSource,
    private val buildConfigInformation: BuildConfigInformation
) {
    suspend operator fun invoke() {
        val currentVersion = buildConfigInformation.versionName().substringBefore("-")
        val lastUpdatedVersion = internalMessagesRepository.getLastUpdatedVersion().firstOrNull()

        when {
            lastUpdatedVersion.isNullOrBlank() -> { // fresh install
                // create a welcome message to save in the DB with the current version of the app
                internalMessagesRepository.saveInternalMessage(
                    changeLogDataSource.createWelcomeMessage(currentVersion)
                )

                @Requirement(
                    "O.Resi_9#1",
                    sourceSpecification = "BSI-eRp-ePA",
                    rationale = "Show warning for deprecated Android devices, if present",
                    codeLines = 5
                )
                @Requirement(
                    "A_19178#1",
                    sourceSpecification = "gemSpec_eRp_FdV",
                    rationale = "Show warning for deprecated Android devices, if present",
                    codeLines = 5
                )
                if (changeLogDataSource.shouldShowSecurityWarningMessage()) {
                    internalMessagesRepository.saveInternalMessage(
                        changeLogDataSource.createSecurityWarningMessage(currentVersion)
                    )
                }
            }

            lastUpdatedVersion.isSecurityWarningMigration() -> {
                getNewChangeLogEntries(
                    lastUpdatedVersion,
                    currentVersion
                )
                internalMessagesRepository.saveInternalMessage(
                    changeLogDataSource.createSecurityWarningMessage(currentVersion)
                )
            }

            else -> {
                // app update
                getNewChangeLogEntries(
                    lastUpdatedVersion,
                    currentVersion
                )
            }
        }
    }

    private fun String.isSecurityWarningMigration(): Boolean {
        return this.let { it <= "1.32.0" } && changeLogDataSource.shouldShowSecurityWarningMessage()
    }

    private suspend fun getNewChangeLogEntries(
        lastUpdatedVersion: String,
        currentVersion: String
    ) {
        val changeLogEntries = changeLogDataSource.getChangeLogsAsInternalMessage()
        changeLogEntries.filter {
            it.version > lastUpdatedVersion && it.version <= currentVersion
        }.forEach {
            internalMessagesRepository.saveInternalMessage(
                it
            )
        }
    }
}
