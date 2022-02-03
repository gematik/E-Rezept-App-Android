/*
 * Copyright (c) 2022 gematik GmbH
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
import de.gematik.ti.erp.app.prescription.ui.model.ScanScreenData
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
    val info: ScanScreenData.Info? = null,
    val state: ScanScreenData.ScanState? = null,
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
    var vibration = MutableSharedFlow<ScanScreenData.VibrationPattern>()
        private set

    private val emptyScanWorkflow = ScanWorkflow(
        code = ScannedCode("", OffsetDateTime.now()),
        coordinates = FloatArray(0),
        state = ScanScreenData.ScanState.Final
    )

    private var existingTaskIds = scannedCodes.map {
        it.flatMap { validCode ->
            validCode.urls.mapNotNull { url ->
                TwoDCodeValidator.taskPattern.matchEntire(url)?.groupValues?.get(1)
            }
        } + prescriptionUseCase.getAllTasksWithTaskIdOnly()
    }

    fun screenState() = scannedCodes.map { codes ->
        val totalNrOfPrescriptions = codes.sumOf { it.urls.size }
        val totalNrOfCodes = codes.size

        ScanScreenData.State(
            snackBar = ScanScreenData.ActionBar(totalNrOfPrescriptions, totalNrOfCodes)
        )
    }

    fun scanOverlayState() = flow {
        val batchFlow = scanner.batch.mapNotNull { batch ->
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
            val state = it.copy(state = ScanScreenData.ScanState.Hold)

            emit(state) // hold
            delay(1000L)
            emit(it.copy(state = ScanScreenData.ScanState.Save)) // saved
            delay(3000L)
            emit(it.copy(state = ScanScreenData.ScanState.Final)) // final
        }.map {
            if (it.state == ScanScreenData.ScanState.Hold) {
                vibration.emit(ScanScreenData.VibrationPattern.Focused)
            }

            if (it.state == ScanScreenData.ScanState.Save) {
                val validCode = validateScannedCode(it.code)

                if (validCode == null) {
                    vibration.emit(ScanScreenData.VibrationPattern.Error)
                    it.copy(
                        info = ScanScreenData.Info.ErrorNotValid,
                        state = ScanScreenData.ScanState.Error
                    )
                } else if (!addScannedCode(validCode)) {
                    vibration.emit(ScanScreenData.VibrationPattern.Error)
                    it.copy(
                        info = ScanScreenData.Info.ErrorDuplicated,
                        state = ScanScreenData.ScanState.Error
                    )
                } else {
                    vibration.emit(ScanScreenData.VibrationPattern.Saved)
                    it.copy(info = ScanScreenData.Info.Scanned(validCode.urls.size))
                }
            } else {
                it
            }
        }.collect {
            emit(
                ScanScreenData.OverlayState(
                    area = if (it != emptyScanWorkflow) it.coordinates else null,
                    state = it.state ?: ScanScreenData.ScanState.Hold,
                    info = it.info ?: ScanScreenData.Info.Focus,
                )
            )
        }
    }.flowOn(dispatchProvider.default())

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
        viewModelScope.launch(dispatchProvider.io()) {
            prescriptionUseCase.mapScannedCodeToTask(scannedCodes.value)
            scannedCodes.value = listOf()
        }
    }
}
