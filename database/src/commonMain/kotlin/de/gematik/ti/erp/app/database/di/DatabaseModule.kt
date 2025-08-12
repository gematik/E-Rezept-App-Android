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

package de.gematik.ti.erp.app.database.di

import de.gematik.ti.erp.app.database.api.TaskLocalDataSource
import de.gematik.ti.erp.app.database.bridge.task.TaskLocalDataSourceBridge
import de.gematik.ti.erp.app.database.realm.v1.task.datasource.TaskLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v2.task.datasource.TaskLocalDataSourceV2
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

/**
 * Provides Kodein bindings for task-related local data sources, supporting both legacy (V1)
 * and new (V2) implementations. In debug mode, it wires a bridge implementation that compares
 * data between V1 and V2 for validation and migration purposes.
 *
 * @param isDebug Indicates whether the app is running in debug mode. If true, a bridge
 * implementation (`LocalDataSourceBridge`) is bound to the default `LocalDataSource`
 * interface to enable runtime comparison between V1 and V2. In release mode, only V1 is expected
 * to be used.
 *
 * @return A Kodein `DI.Module` containing bindings for V1, V2, and bridge data sources.
 */
fun databaseModule(isDebug: Boolean) = DI.Module("databaseModule", allowSilentOverride = true) {
    bindProvider<TaskLocalDataSource>(tag = ModuleTags.TASK_V1) { TaskLocalDataSourceV1(instance()) }
    bindProvider<TaskLocalDataSource>(tag = ModuleTags.TASK_V2) { TaskLocalDataSourceV2(instance()) }
    bindProvider<TaskLocalDataSource> {
        TaskLocalDataSourceBridge(
            instance(tag = ModuleTags.TASK_V1),
            instance(tag = ModuleTags.TASK_V2),
            isDebug
        )
    }
}
