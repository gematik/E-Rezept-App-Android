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

package de.gematik.ti.erp.app.migration.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.database.migration.DataMigrator
import de.gematik.ti.erp.app.database.migration.MigrationProgress
import de.gematik.ti.erp.app.database.migration.MigrationStep
import de.gematik.ti.erp.app.migration.usecase.ClearMigratedPharmacyAndShippingInfoUseCase
import de.gematik.ti.erp.app.migration.usecase.CompleteMigrationUseCase
import de.gematik.ti.erp.app.migration.usecase.StartMigrationUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance
import kotlin.getValue

class DataMigrationViewModel(
    private val dataMigrator: DataMigrator,
    private val startMigrationUseCase: StartMigrationUseCase,
    private val completeMigrationUseCase: CompleteMigrationUseCase,
    private val clearMigratedPharmacyAndShippingInfoUseCase: ClearMigratedPharmacyAndShippingInfoUseCase
) : Controller() {

    val uiState: StateFlow<UiState<MigrationProgress>> = dataMigrator.progress
        .map { progress ->
            progress.error?.let { error ->
                if (error.second == MigrationStep.SHIPPING_INFO || error.second == MigrationStep.PHARMACY) {
                    UiState.Data(progress)
                } else {
                    UiState.Error(error = error.first, progress)
                }
            } ?: UiState.Loading(progress)
        }
        .stateIn(controllerScope, SharingStarted.WhileSubscribed(), UiState.Loading())

    init {
        startMigration()
        completeMigration()
    }

    fun startMigration() {
        controllerScope.launch {
            runCatching {
                startMigrationUseCase()
            }.onFailure { e ->
                Napier.e(e) { "Migration failed" }
            }
        }
    }

    fun completeMigration() {
        controllerScope.launch {
            dataMigrator.progress.collect {
                if (it.isFinished && it.error == null) {
                    completeMigrationUseCase()
                }
            }
        }
    }

    fun recoverMigrationFailure() {
        controllerScope.launch {
            uiState.value.data?.error?.second?.let { migrationStep ->
                if (migrationStep == MigrationStep.SHIPPING_INFO || migrationStep == MigrationStep.PHARMACY) {
                    clearMigratedPharmacyAndShippingInfoUseCase()
                    completeMigrationUseCase()
                }
            }
        }
    }
}

@Composable
fun rememberDataMigrationViewModel(): DataMigrationViewModel {
    val dataMigrator: DataMigrator by rememberInstance()
    val startMigrationUseCase: StartMigrationUseCase by rememberInstance()
    val completeMigrationUseCase: CompleteMigrationUseCase by rememberInstance()
    val clearMigratedPharmacyAndShippingInfoUseCase: ClearMigratedPharmacyAndShippingInfoUseCase by rememberInstance()
    return remember {
        DataMigrationViewModel(
            dataMigrator = dataMigrator,
            startMigrationUseCase = startMigrationUseCase,
            completeMigrationUseCase = completeMigrationUseCase,
            clearMigratedPharmacyAndShippingInfoUseCase = clearMigratedPharmacyAndShippingInfoUseCase
        )
    }
}
