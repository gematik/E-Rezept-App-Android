/*
 * Copyright (c) 2021 gematik GmbH
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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.db.entities.Task
import de.gematik.ti.erp.app.prescription.ui.model.ScanScreen
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import javax.inject.Inject

private data class ScanWorkflow(
    val info: ScanScreen.Info? = null,
    val state: ScanScreen.ScanState? = null,
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
@HiltViewModel
class ScanPrescriptionViewModel @Inject constructor(
    private val prescriptionUseCase: PrescriptionUseCase,
    val scanner: TwoDCodeScanner,
    val processor: TwoDCodeProcessor,
    private val validator: TwoDCodeValidator,
    private val dispatchProvider: DispatchProvider
) : ViewModel() {

    private val scannedCodes = MutableStateFlow(listOf<ValidScannedCode>())
    var vibration = MutableSharedFlow<ScanScreen.VibrationPattern>()
        private set

    private val emptyScanWorkflow = ScanWorkflow(
        code = ScannedCode("", OffsetDateTime.now()),
        coordinates = FloatArray(0),
        state = ScanScreen.ScanState.Final
    )

    private var existingTaskIds = scannedCodes.map {
        it.flatMap { validCode ->
            validCode.urls.mapNotNull { url ->
                TwoDCodeValidator.taskPattern.matchEntire(url)?.groupValues?.get(1)
            }
        } + prescriptionUseCase.getAllTasksWithTaskIdOnly()
    }

    fun screenState() = scannedCodes.map { codes ->
        val totalNrOfPrescriptions = codes.sumBy { it.urls.size }
        val totalNrOfCodes = codes.size

        ScanScreen.State(
            snackBar = ScanScreen.SnackBar(totalNrOfPrescriptions, totalNrOfCodes)
        )
    }

    fun scanOverlayState() = flow {
//        scanner.batch.mapNotNull {
//            processor.process(it)?.second
//        }.collect {
//            emit(ScanScreen.OverlayState(
//                area = it,
//                info = ScanScreen.Info.Focus,
//                state = ScanScreen.ScanState.Hold,
//            ))
//        }
        val batchFlow = scanner.batch.mapNotNull { batch ->
            if (batch == scanner.defaultBatch) {
                Pair(batch.averageScanTime, emptyScanWorkflow)
            } else {
                processor.process(batch)?.let { result ->
                    val (json, coords) = result
                    Pair(
                        batch.averageScanTime,
                        ScanWorkflow(
                            code = ScannedCode(json, OffsetDateTime.now()),
                            coordinates = coords
                        )
                    )
                }
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
            val state = it.copy(state = ScanScreen.ScanState.Hold)

            emit(state) // hold
            delay(1000L)
            emit(it.copy(state = ScanScreen.ScanState.Save)) // saved
            delay(3000L)
            emit(it.copy(state = ScanScreen.ScanState.Final)) // final
        }.map {
            if (it.state == ScanScreen.ScanState.Hold) {
                vibration.emit(ScanScreen.VibrationPattern.Focused)
            }

            if (it.state == ScanScreen.ScanState.Save) {
                val validCode = validateScannedCode(it.code)

                if (validCode == null) {
                    vibration.emit(ScanScreen.VibrationPattern.Error)
                    it.copy(
                        info = ScanScreen.Info.ErrorNotValid,
                        state = ScanScreen.ScanState.Error
                    )
                } else if (!addScannedCode(validCode)) {
                    vibration.emit(ScanScreen.VibrationPattern.Error)
                    it.copy(
                        info = ScanScreen.Info.ErrorDuplicated,
                        state = ScanScreen.ScanState.Error
                    )
                } else {
                    vibration.emit(ScanScreen.VibrationPattern.Saved)
                    it.copy(info = ScanScreen.Info.Scanned(validCode.urls.size))
                }
            } else {
                it
            }
        }.collect {
            emit(
                ScanScreen.OverlayState(
                    area = if (it != emptyScanWorkflow) it.coordinates else null,
                    state = it.state ?: ScanScreen.ScanState.Hold,
                    info = it.info ?: ScanScreen.Info.Focus,
                )
            )
        }
    }.flowOn(dispatchProvider.unconfined())

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

    fun saveToDatabase() {
        val now = OffsetDateTime.now()

        var i = 1
        val tasks = scannedCodes.value.flatMap { code ->
            code.extract().map { (_, taskId, accessCode) ->
                Task(
                    taskId = taskId,
                    nrInScanSession = i++,
                    scanSessionName = "",
                    accessCode = accessCode,
                    scanSessionEnd = now,
                    scannedOn = code.raw.scannedOn
                )
            }
        }

        viewModelScope.launch(dispatchProvider.io()) {
            tasks.takeIf { it.isNotEmpty() }
                ?.let { prescriptionUseCase.saveScannedTasks(it) }
        }

        scannedCodes.value = listOf()
    }
}

private fun ValidScannedCode.extract(): List<List<String>> =
    this.urls.mapNotNull {
        TwoDCodeValidator.taskPattern.matchEntire(it)?.groupValues
    }
