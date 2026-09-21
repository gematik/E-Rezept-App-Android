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

package de.gematik.ti.erp.app.database.migration

import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.database.api.AppAuthenticationLocalDataSource
import de.gematik.ti.erp.app.database.api.CommunicationLocalDataSource
import de.gematik.ti.erp.app.database.api.IdpConfigurationLocalDataSource
import de.gematik.ti.erp.app.database.api.InternalMessagesLocalDataSource
import de.gematik.ti.erp.app.database.api.MedicationPlanLocalDataSource
import de.gematik.ti.erp.app.database.api.ProfileLocalDataSource
import de.gematik.ti.erp.app.database.api.SettingsLocalDataSource
import de.gematik.ti.erp.app.database.api.ShippingInfoLocalDataSource
import de.gematik.ti.erp.app.database.api.TrustStoreLocalDataSource
import de.gematik.ti.erp.app.database.api.UserAuthenticationLocalDataSource
import de.gematik.ti.erp.app.database.api.eurezept.EuTaskLocalDataSource
import de.gematik.ti.erp.app.database.api.invoice.InvoiceLocalDataSource
import de.gematik.ti.erp.app.database.api.pharmacy.PharmacyLocalDataSource
import de.gematik.ti.erp.app.database.api.pharmacy.PharmacySearchAccessTokenLocalDataSource
import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update

class DefaultDataMigrator(
    private val profileV1: ProfileLocalDataSource,
    private val profileV2: ProfileLocalDataSource,
    private val userAuthV1: UserAuthenticationLocalDataSource,
    private val userAuthV2: UserAuthenticationLocalDataSource,
    private val pharmacyV1: PharmacyLocalDataSource,
    private val pharmacyV2: PharmacyLocalDataSource,
    private val searchTokenV1: PharmacySearchAccessTokenLocalDataSource,
    private val searchTokenV2: PharmacySearchAccessTokenLocalDataSource,
    private val internalMessageV1: InternalMessagesLocalDataSource,
    private val internalMessageV2: InternalMessagesLocalDataSource,
    private val settingsV1: SettingsLocalDataSource,
    private val settingsV2: SettingsLocalDataSource,
    private val invoiceV1: InvoiceLocalDataSource,
    private val invoiceV2: InvoiceLocalDataSource,
    private val taskV1: TaskLocalDataSource,
    private val taskV2: TaskLocalDataSource,
    private val communicationV1: CommunicationLocalDataSource,
    private val communicationV2: CommunicationLocalDataSource,
    private val idpConfigV1: IdpConfigurationLocalDataSource,
    private val idpConfigV2: IdpConfigurationLocalDataSource,
    private val trustStoreV1: TrustStoreLocalDataSource,
    private val trustStoreV2: TrustStoreLocalDataSource,
    private val shippingInfoV1: ShippingInfoLocalDataSource,
    private val shippingInfoV2: ShippingInfoLocalDataSource,
    private val appAuthV1: AppAuthenticationLocalDataSource,
    private val appAuthV2: AppAuthenticationLocalDataSource,
    private val euTaskV1: EuTaskLocalDataSource,
    private val euTaskV2: EuTaskLocalDataSource,
    private val medicationPlanV1: MedicationPlanLocalDataSource,
    private val medicationPlanV2: MedicationPlanLocalDataSource
) : DataMigrator {
    private val _progress = MutableStateFlow(MigrationProgress())
    override val progress: StateFlow<MigrationProgress> = _progress.asStateFlow()

    override suspend fun migrate() {
        val migrators = listOf(
            SettingsMigrator(settingsV1, settingsV2),
            AppAuthMigrator(appAuthV1, appAuthV2),

            IdpConfigMigrator(idpConfigV1, idpConfigV2),
            TrustStoreMigrator(trustStoreV1, trustStoreV2),

            ProfileMigrator(profileV1, profileV2, userAuthV1, userAuthV2),

            TaskMigrator(taskV1, taskV2, profileV1),
            MedicationPlanMigrator(medicationPlanV1, medicationPlanV2, profileV2, taskV2),

            CommunicationMigrator(communicationV1, communicationV2, profileV1),
            InvoiceMigrator(invoiceV1, invoiceV2, profileV1),
            EuTaskMigrator(euTaskV1, euTaskV2),
            InternalMessageMigrator(internalMessageV1, internalMessageV2),

            PharmacyMigrator(pharmacyV1, pharmacyV2, searchTokenV1, searchTokenV2),
            ShippingInfoMigrator(shippingInfoV1, shippingInfoV2)
        )

        _progress.update {
            it.copy(
                groupsTotal = migrators.size
            )
        }

        val completed = mutableListOf<MigrationStep>()
        migrators.forEachIndexed { index, migrator ->
            _progress.update {
                it.copy(
                    currentStep = migrator.step,
                    groupsDone = index,
                    completedSteps = completed.toList()
                )
            }
            try {
                migrator.migrate()
                completed.add(migrator.step)
            } catch (e: Throwable) {
                Napier.e { "Migration failed for step ${migrator.step}: ${e.message}" }
                _progress.update {
                    it.copy(
                        error = Pair(e, migrator.step)
                    )
                }
            }
        }
        _progress.update {
            it.copy(
                isFinished = true,
                groupsDone = migrators.size,
                completedSteps = completed.toList(),
                currentStep = null
            )
        }
    }

    override suspend fun isMigrationRequired(): Boolean {
        return profileV1.loadProfiles().first().isNotEmpty()
    }

    private interface TableMigrator {
        val step: MigrationStep

        suspend fun migrate()
    }

    private class ProfileMigrator(
        private val v1: ProfileLocalDataSource,
        private val v2: ProfileLocalDataSource,
        private val authV1: UserAuthenticationLocalDataSource,
        private val authV2: UserAuthenticationLocalDataSource
    ) : TableMigrator {
        override val step = MigrationStep.PROFILE

        override suspend fun migrate() {
            val profiles = v1.loadProfiles().first()
            profiles.forEach { profile ->
                v2.saveProfile(profile)
                if (profile.active) {
                    v2.activateProfile(profile.id)
                }
                val auth = authV1.getUserAuthenticationForProfile(profile.id).firstOrNull()
                auth?.let { authV2.saveUserAuthenticationForProfile(profile.id, it) }
            }
        }
    }

    private class PharmacyMigrator(
        private val v1: PharmacyLocalDataSource,
        private val v2: PharmacyLocalDataSource,
        private val tokenV1: PharmacySearchAccessTokenLocalDataSource,
        private val tokenV2: PharmacySearchAccessTokenLocalDataSource
    ) : TableMigrator {
        override val step = MigrationStep.PHARMACY

        override suspend fun migrate() {
            v1.loadPharmacies().first().forEach { pharmacy ->
                if (v1.isPharmacyInFavorites(pharmacy).first()) {
                    v2.markPharmacyAsFavourite(pharmacy)
                }
                if (v1.isPharmacyOftenUsed(pharmacy).first()) {
                    v2.markPharmacyAsOftenUsed(pharmacy)
                }
            }
            tokenV1.searchAccessToken.firstOrNull()?.let { token ->
                tokenV2.saveToken(token.accessToken, token.lastUpdate)
            }
        }
    }

    private class InternalMessageMigrator(
        private val v1: InternalMessagesLocalDataSource,
        private val v2: InternalMessagesLocalDataSource
    ) : TableMigrator {
        override val step = MigrationStep.INTERNAL_MESSAGE

        override suspend fun migrate() {
            v1.getInternalMessages().first().forEach { message ->
                v2.saveInternalMessage(message)
            }
        }
    }

    private class SettingsMigrator(
        private val v1: SettingsLocalDataSource,
        private val v2: SettingsLocalDataSource
    ) : TableMigrator {
        override val step = MigrationStep.SETTINGS

        override suspend fun migrate() {
            val settings = v1.loadSettings().first()
            v2.saveTheme(settings.theme)
            v2.saveAllowTracking(settings.trackingAllowed)
            v2.saveAllowScreenshots(settings.screenShotsAllowed)
            v2.saveZoomEnabled(settings.zoomEnabled)
            if (settings.userHasAcceptedInsecureDevice) v2.acceptInsecureDevice()
            if (settings.userHasAcceptedIntegrityNotOk) v2.acceptIntegrityNotOk()
            if (settings.welcomeDrawerShown) v2.saveWelcomeDrawerShown()
            settings.latestAppVersion.let { v2.saveLatestAppVersion(it) }
            settings.onboardingShownIn?.let { v2.saveOnboardingShownIn(it) }
        }
    }

    private class InvoiceMigrator(
        private val v1: InvoiceLocalDataSource,
        private val v2: InvoiceLocalDataSource,
        private val profileV1: ProfileLocalDataSource
    ) : TableMigrator {
        override val step = MigrationStep.INVOICE

        override suspend fun migrate() {
            val profiles = profileV1.loadProfiles().first()
            profiles.forEach { profile ->
                v1.loadInvoices(profile.id).first().forEach { invoice ->
                    v2.saveInvoice(invoice)
                }
            }
        }
    }

    private class TaskMigrator(
        private val v1: TaskLocalDataSource,
        private val v2: TaskLocalDataSource,
        private val profileV1: ProfileLocalDataSource
    ) : TableMigrator {
        override val step = MigrationStep.TASK

        override suspend fun migrate() {
            val profiles = profileV1.loadProfiles().first()
            profiles.forEach { profile ->
                val tasks = v1.loadTaskListByProfileId(profile.id).first()
                tasks.forEach { task ->
                    v2.saveTask(task)
                }
            }
        }
    }

    private class CommunicationMigrator(
        private val v1: CommunicationLocalDataSource,
        private val v2: CommunicationLocalDataSource,
        private val profileV1: ProfileLocalDataSource
    ) : TableMigrator {
        override val step = MigrationStep.COMMUNICATION

        override suspend fun migrate() {
            val profiles = profileV1.loadProfiles().first()
            profiles.forEach { profile ->
                val communications = mutableListOf<CommunicationErpModel>()
                communications.addAll(v1.loadDispReqCommunicationsByProfileId(profile.id).first())
                communications.addAll(v1.loadRepliedCommunicationsByProfileId(profile.id).first())
                if (communications.isNotEmpty()) {
                    v2.saveCommunications(communications)
                }
            }
        }
    }

    private class IdpConfigMigrator(
        private val v1: IdpConfigurationLocalDataSource,
        private val v2: IdpConfigurationLocalDataSource
    ) : TableMigrator {
        override val step = MigrationStep.IDP_CONFIG

        override suspend fun migrate() {
            v1.getIdpConfiguration()?.let { v2.saveIdpConfiguration(it) }
        }
    }

    private class TrustStoreMigrator(
        private val v1: TrustStoreLocalDataSource,
        private val v2: TrustStoreLocalDataSource
    ) : TableMigrator {
        override val step = MigrationStep.TRUST_STORE

        override suspend fun migrate() {
            v1.loadUntrusted().first()?.let {
                v2.saveCertificateAndOcspLists(it.certListJson, it.ocspListJson)
            }
        }
    }

    private class ShippingInfoMigrator(
        private val v1: ShippingInfoLocalDataSource,
        private val v2: ShippingInfoLocalDataSource
    ) : TableMigrator {
        override val step = MigrationStep.SHIPPING_INFO

        override suspend fun migrate() {
            v1.getShippingInfo()?.let { v2.saveShippingInfo(it) }
        }
    }

    private class AppAuthMigrator(
        private val v1: AppAuthenticationLocalDataSource,
        private val v2: AppAuthenticationLocalDataSource
    ) : TableMigrator {
        override val step = MigrationStep.APP_AUTH

        override suspend fun migrate() {
            val model = v1.getAppAuthenticationErpModel().first()
            v2.initialiseAppAuthenticationEntity(model)
        }
    }

    private class EuTaskMigrator(
        private val v1: EuTaskLocalDataSource,
        private val v2: EuTaskLocalDataSource
    ) : TableMigrator {
        override val step = MigrationStep.EU_TASK

        override suspend fun migrate() {
            v1.observeAllEuOrders().first().forEach { order ->
                v2.importMigratedOrder(order)
            }
        }
    }

    private class MedicationPlanMigrator(
        private val v1: MedicationPlanLocalDataSource,
        private val v2: MedicationPlanLocalDataSource,
        private val profileV2: ProfileLocalDataSource,
        private val taskV2: TaskLocalDataSource
    ) : TableMigrator {
        override val step = MigrationStep.MEDICATION_PLAN

        override suspend fun migrate() {
            val existingProfileIds = profileV2.loadProfiles().first().map { it.id }.toSet()
            val existingTaskIds = taskV2.loadATaskIdStringList().first().toSet()
            v1.getAllMedicationSchedules().first().forEach { schedule ->
                if (existingProfileIds.contains(schedule.profileId) && existingTaskIds.contains(schedule.taskId)) {
                    v2.importMigratedSchedule(schedule)
                }
            }
        }
    }
}
