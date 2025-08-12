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

import de.gematik.ti.erp.app.api.FhirPagination
import de.gematik.ti.erp.app.fhir.dispense.parser.TaskMedicationDispenseParser
import de.gematik.ti.erp.app.fhir.prescription.parser.TaskBundleSeparationParser
import de.gematik.ti.erp.app.fhir.prescription.parser.TaskEPrescriptionMedicalDataParser
import de.gematik.ti.erp.app.fhir.prescription.parser.TaskEPrescriptionMetadataParser
import de.gematik.ti.erp.app.fhir.prescription.parser.TaskEPrescriptionParsers
import de.gematik.ti.erp.app.fhir.prescription.parser.TaskEntryParser
import de.gematik.ti.erp.app.prescription.repository.DefaultTaskRepository
import de.gematik.ti.erp.app.prescription.repository.RealmLegacyTaskLocalDataSource
import de.gematik.ti.erp.app.prescription.repository.TaskRemoteDataSource
import de.gematik.ti.erp.app.prescription.repository.TaskRepository
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

val taskModule = DI.Module("taskModule") {
    bindProvider { TaskRemoteDataSource(instance()) }
    bindProvider { RealmLegacyTaskLocalDataSource(instance()) }
    bindProvider { TaskEntryParser() }
    bindProvider { TaskBundleSeparationParser() }
    bindProvider { TaskEPrescriptionMetadataParser() }
    bindProvider { TaskEPrescriptionMedicalDataParser() }
    bindProvider { TaskMedicationDispenseParser() }
    bindProvider { FhirPagination() } // Later we move this to a more generic module, when we use it more widely
    bindProvider {
        TaskEPrescriptionParsers(
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
}

val taskRepositoryModule = DI.Module("taskRepositoryModule", allowSilentOverride = true) {
    bindProvider<TaskRepository> {
        DefaultTaskRepository(
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
}
