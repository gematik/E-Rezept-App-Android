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

@file:Suppress("unused")

package de.gematik.ti.erp.app.database.bridge.task

import de.gematik.ti.erp.app.database.api.TaskLocalDataSource
import de.gematik.ti.erp.app.database.realm.v1.task.datasource.TaskLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v2.task.datasource.TaskLocalDataSourceV2
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A debug-only bridge implementation of [TaskLocalDataSource] that proxies calls to both
 * [TaskLocalDataSourceV1] (legacy database) and [TaskLocalDataSourceV2] (new schema).
 *
 * This class is intended for transitional periods where both database schemas coexist.
 * In debug builds, it compares results from V1 and V2 to validate consistency during migration.
 *
 * Specifically, it logs discrepancies between V1 and V2 for the `lastTaskModifiedAt(...)` method,
 * helping developers catch mismatches early. The logs are emitted via [Napier] and can later
 * be integrated into a dedicated debug screen.
 *
 * In release builds, the bridge behaves like V1 by default, or can be adapted to behave like V2,
 * depending on the binding logic in DI.
 *
 * @param taskLocalDataSourceV1 Reference to the legacy Realm V1 data source.
 * @param taskLocalDataSourceV2 Reference to the new Realm V2 data source.
 * @param isDebug Flag indicating whether debug-mode logging and comparison should be enabled.
 * @param dispatcher Dispatcher used for launching background comparison tasks (default: [Dispatchers.IO]).
 */
internal class TaskLocalDataSourceBridge(
    private val taskLocalDataSourceV1: TaskLocalDataSourceV1,
    private val taskLocalDataSourceV2: TaskLocalDataSourceV2,
    private val isDebug: Boolean,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TaskLocalDataSource {

    private val scope = CoroutineScope(dispatcher)

    // to follow
}
