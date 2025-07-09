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

package de.gematik.ti.erp.app.prescription

import de.gematik.ti.erp.app.base.usecase.DownloadAllResourcesUseCase
import de.gematik.ti.erp.app.base.usecase.GetLastSuccessfulRefreshedTimeUseCase
import de.gematik.ti.erp.app.prescription.repository.DefaultPrescriptionRepository
import de.gematik.ti.erp.app.prescription.repository.DownloadResourcesStateRepository
import de.gematik.ti.erp.app.prescription.repository.PrescriptionLocalDataSource
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRemoteDataSource
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.prescription.ui.TwoDCodeProcessor
import de.gematik.ti.erp.app.prescription.ui.TwoDCodeScanner
import de.gematik.ti.erp.app.prescription.ui.TwoDCodeValidator
import de.gematik.ti.erp.app.prescription.usecase.ArchiveExpiredDigasUseCase
import de.gematik.ti.erp.app.prescription.usecase.DeletePrescriptionUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetActivePrescriptionsUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetArchivedDigasUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetArchivedPrescriptionsUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetDownloadResourcesDetailStateUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetDownloadResourcesSnapshotStateUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetPrescriptionByTaskIdUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetTaskIdsUseCase
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.prescription.usecase.RedeemScannedTaskUseCase
import de.gematik.ti.erp.app.prescription.usecase.UpdateScannedTaskNameUseCase
import de.gematik.ti.erp.app.redeem.usecase.GetReadyPrescriptionsByTaskIdsUseCase
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

val prescriptionModule =
    DI.Module("prescriptionModule") {
        bindProvider { TwoDCodeProcessor() }
        bindProvider { TwoDCodeScanner(instance()) }
        bindProvider { TwoDCodeValidator() }
        bindSingleton { PrescriptionLocalDataSource(instance()) }
        bindSingleton { PrescriptionRemoteDataSource(instance()) }
        bindSingleton { PrescriptionUseCase(instance(), instance(), instance()) }
        bindProvider { GetLastSuccessfulRefreshedTimeUseCase(instance()) }
        bindProvider {
            DownloadAllResourcesUseCase(
                taskRepository = instance(),
                communicationRepository = instance(),
                invoicesRepository = instance(),
                profileRepository = instance(),
                settingsRepository = instance(),
                stateRepository = instance(),
                networkStatusTracker = instance()
            )
        }
        bindProvider { GetActivePrescriptionsUseCase(instance()) }
        bindProvider { GetArchivedPrescriptionsUseCase(instance()) }
        bindProvider { DeletePrescriptionUseCase(instance(), instance()) }
        bindProvider { UpdateScannedTaskNameUseCase(instance()) }
        bindProvider { RedeemScannedTaskUseCase(instance()) }
        bindProvider { GetPrescriptionByTaskIdUseCase(instance()) }
        bindProvider { GetReadyPrescriptionsByTaskIdsUseCase(instance()) }
        bindProvider { GetTaskIdsUseCase(instance()) }
        bindProvider { GetDownloadResourcesDetailStateUseCase(instance()) }
        bindProvider { GetDownloadResourcesSnapshotStateUseCase(instance()) }
        bindSingleton { GetArchivedDigasUseCase(instance()) }
        bindSingleton { ArchiveExpiredDigasUseCase(instance()) }
    }

val prescriptionRepositoryModule =
    DI.Module("prescriptionRepositoryModule", allowSilentOverride = true) {
        bindProvider<PrescriptionRepository> { DefaultPrescriptionRepository(instance(), instance(), instance()) }
        bindSingleton { DownloadResourcesStateRepository() }
    }
