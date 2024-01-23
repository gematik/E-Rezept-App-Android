/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.prescription.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.prescription.ui.model.ScanData
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.transformLatest
import kotlinx.datetime.Clock
import org.kodein.di.compose.rememberInstance

private data class ScanWorkflow(
    val info: ScanData.Info? = null,
    val state: ScanData.ScanState? = null,
    val code: ScannedCode,
    val coordinates: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScanWorkflow

        if (info != other.info) return false
        if (state != other.state) return false
        if (code != other.code) return false
        if (!coordinates.contentEquals(other.coordinates)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = info?.hashCode() ?: 0
        result = 31 * result + (state?.hashCode() ?: 0)
        result = 31 * result + code.hashCode()
        result = 31 * result + coordinates.contentHashCode()
        return result
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ScanPrescriptionController(
    private val prescriptionUseCase: PrescriptionUseCase,
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    // private val profilesUseCase: ProfilesUseCase,
    val scanner: TwoDCodeScanner,
    val processor: TwoDCodeProcessor,
    private val validator: TwoDCodeValidator,
    dispatchers: DispatchProvider
) {

    private val scannedCodes = MutableStateFlow(listOf<ValidScannedCode>())
    var vibration = MutableSharedFlow<ScanData.VibrationPattern>()
        private set

    private val emptyScanWorkflow = ScanWorkflow(
        code = ScannedCode("", Clock.System.now()),
        coordinates = FloatArray(0),
        state = ScanData.ScanState.Final
    )

    private var existingTaskIds = scannedCodes.map {
        it.flatMap { validCode ->
            validCode.urls.mapNotNull { url ->
                TwoDCodeValidator.taskPattern.matchEntire(url)?.groupValues?.get(1)
            }
        } + prescriptionUseCase.getAllTasksWithTaskIdOnly().first()
    }

    private val stateFlow = scannedCodes.map { codes ->
        val totalNrOfPrescriptions = codes.sumOf { it.urls.size }
        val totalNrOfCodes = codes.size

        ScanData.State(
            snackBar = ScanData.ActionBar(totalNrOfPrescriptions, totalNrOfCodes)
        )
    }

    val state
        @Composable
        get() = stateFlow.collectAsStateWithLifecycle(ScanData.defaultState)

    private val scanOverlayFlow = flow {
        val batchFlow = scanner.batch.mapNotNull { batch ->
            processor.process(batch)?.let { result ->
                val (json, coords) = result
                Pair(
                    batch.averageScanTime,
                    ScanWorkflow(
                        code = ScannedCode(json, Clock.System.now()),
                        coordinates = coords
                    )
                )
            }
        }.transformLatest { (avgTime, scanWorkflow) ->
            emit(scanWorkflow)
            delay(avgTime)
            emit(emptyScanWorkflow)
        }

        val windowedBatchFlow = flow {
            var prev = emptyScanWorkflow
            batchFlow.collect {
                emit(listOf(prev, it))
                prev = it
            }
        }

        windowedBatchFlow.transform { (prev, next) ->
            if (prev.code.json != next.code.json || next == emptyScanWorkflow) {
                emit(next)
            }
        }.transformLatest {
            if (it == emptyScanWorkflow) {
                emit(emptyScanWorkflow)
                return@transformLatest
            }
            val state = it.copy(state = ScanData.ScanState.Hold)

            emit(state) // hold
            delay(1000L)
            emit(it.copy(state = ScanData.ScanState.Save)) // saved
            delay(3000L)
            emit(it.copy(state = ScanData.ScanState.Final)) // final
        }.map {
            if (it.state == ScanData.ScanState.Hold) {
                vibration.emit(ScanData.VibrationPattern.Focused)
            }

            if (it.state == ScanData.ScanState.Save) {
                val validCode = validateScannedCode(it.code)

                if (validCode == null) {
                    vibration.emit(ScanData.VibrationPattern.Error)
                    it.copy(
                        info = ScanData.Info.ErrorNotValid,
                        state = ScanData.ScanState.Error
                    )
                } else if (!addScannedCode(validCode)) {
                    vibration.emit(ScanData.VibrationPattern.Error)
                    it.copy(
                        info = ScanData.Info.ErrorDuplicated,
                        state = ScanData.ScanState.Error
                    )
                } else {
                    vibration.emit(ScanData.VibrationPattern.Saved)
                    it.copy(info = ScanData.Info.Scanned(validCode.urls.size))
                }
            } else {
                it
            }
        }.collect {
            emit(
                ScanData.OverlayState(
                    area = if (it != emptyScanWorkflow) it.coordinates else null,
                    state = it.state ?: ScanData.ScanState.Hold,
                    info = it.info ?: ScanData.Info.Focus
                )
            )
        }
    }.flowOn(dispatchers.default)

    val overlayState
        @Composable
        get() = scanOverlayFlow.collectAsStateWithLifecycle(ScanData.defaultOverlayState)

    private fun validateScannedCode(scannedCode: ScannedCode): ValidScannedCode? =
        validator.validate(scannedCode)

    suspend fun addScannedCode(validCode: ValidScannedCode): Boolean {
        val existingTaskIds = existingTaskIds.take(1).toCollection(mutableListOf()).first()

        val uniqueUrls = validCode.urls.filter { url ->
            val taskId = TwoDCodeValidator.taskPattern.matchEntire(url)?.groupValues?.get(1)
            taskId !in existingTaskIds
        }

        return if (uniqueUrls.isEmpty()) {
            false
        } else {
            scannedCodes.value += validCode.copy(urls = uniqueUrls)
            true
        }
    }

    suspend fun saveToDatabase() {
        getActiveProfileUseCase().collectLatest { profile ->
            prescriptionUseCase.saveScannedCodes(
                profile.id,
                scannedCodes.value
            )
        }
    }
}

@Composable
fun rememberScanPrescriptionController(): ScanPrescriptionController {
    val prescriptionUseCase by rememberInstance<PrescriptionUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val scanner by rememberInstance<TwoDCodeScanner>()
    val processor by rememberInstance<TwoDCodeProcessor>()
    val validator by rememberInstance<TwoDCodeValidator>()
    val dispatchers by rememberInstance<DispatchProvider>()

    return remember {
        ScanPrescriptionController(
            prescriptionUseCase = prescriptionUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            scanner = scanner,
            processor = processor,
            validator = validator,
            dispatchers = dispatchers
        )
    }
}
