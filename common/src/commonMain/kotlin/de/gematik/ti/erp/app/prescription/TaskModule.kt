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

package de.gematik.ti.erp.app.prescription

import de.gematik.ti.erp.app.fhir.prescription.parser.TaskBundleSeparationParser
import de.gematik.ti.erp.app.fhir.prescription.parser.TaskEntryParser
import de.gematik.ti.erp.app.fhir.prescription.parser.TaskKbvParser
import de.gematik.ti.erp.app.fhir.prescription.parser.TaskMetadataParser
import de.gematik.ti.erp.app.prescription.repository.DefaultTaskRepository
import de.gematik.ti.erp.app.prescription.repository.TaskLocalDataSource
import de.gematik.ti.erp.app.prescription.repository.TaskRemoteDataSource
import de.gematik.ti.erp.app.prescription.repository.TaskRepository
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

val taskModule = DI.Module("taskModule") {
    bindProvider { TaskRemoteDataSource(instance()) }
    bindProvider { TaskLocalDataSource(instance()) }
    bindProvider { TaskEntryParser() }
    bindProvider { TaskBundleSeparationParser() }
    bindProvider { TaskMetadataParser() }
    bindProvider { TaskKbvParser() }
}

val taskRepositoryModule = DI.Module("taskRepositoryModule", allowSilentOverride = true) {
    bindProvider<TaskRepository> {
        DefaultTaskRepository(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
}
