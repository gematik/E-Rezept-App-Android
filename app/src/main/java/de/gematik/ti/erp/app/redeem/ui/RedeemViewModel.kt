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

package de.gematik.ti.erp.app.redeem.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.google.common.math.IntMath
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.datamatrix.DataMatrixWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.core.BaseViewModel
import de.gematik.ti.erp.app.db.entities.LowDetailEventSimple
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import java.time.OffsetDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

object RedeemScreen {
    @Stable
    data class SingleCode(
        val matrix: BitMatrix,
        val nrOfCodes: Int,
        val isScanned: Boolean
    )

    @Immutable
    data class State(
        val maxTaskPerCode: Int,
        val showSingleCodes: Boolean,
        val codes: List<SingleCode>
    )
}

@HiltViewModel
class RedeemViewModel @Inject constructor(
    private val prescriptionUseCase: PrescriptionUseCase,
    private val dispatchProvider: DispatchProvider
) : BaseViewModel() {

    private val showSingleCodes = MutableStateFlow(false)
    private val maxTasksPerCode = MutableStateFlow(3)

    val defaultState = RedeemScreen.State(
        maxTasksPerCode.value,
        showSingleCodes.value,
        listOf()
    )

    fun screenState(taskIds: List<String>): Flow<RedeemScreen.State> {
        var codes = listOf<RedeemScreen.SingleCode>()
        return showSingleCodes.map { showSingle ->
            val maxTasks = if (!showSingle) {
                if ((IntMath.mod(taskIds.size, 2) == 0)) {
                    2
                } else {
                    3
                }
            } else {
                1
            }
            if (maxTasks == 3 && taskIds.size > 5) {
                generateRedeemCodes(taskIds.subList(0, 2), maxTasks).collect {
                    codes = it
                }
                generateRedeemCodes(
                    taskIds.subList(3, taskIds.size - 1),
                    maxTasks,
                ).collect {
                    codes = codes + it
                }
            } else {
                generateRedeemCodes(taskIds, maxTasks).collect {
                    codes = it
                }
            }
            RedeemScreen.State(maxTaskPerCode = maxTasks, showSingleCodes = showSingle, codes)
        }
    }

    private fun generateRedeemCodes(
        taskIds: List<String>,
        maxTasksPerCode: Int,
    ): Flow<List<RedeemScreen.SingleCode>> {

        return prescriptionUseCase.tasks().take(1).map {

            val tasks = it
                .asSequence()
                .filter { task -> task.taskId in taskIds }
                .distinctBy { task -> task.taskId }

            tasks
                .toList()
                .map { task ->
                    Pair(task.scannedOn != null, "Task/${task.taskId}/\$accept?ac=${task.accessCode}")
                }
                .windowed(maxTasksPerCode, maxTasksPerCode, partialWindows = true)
                .map { tasksList ->
                    val urls = tasksList.map {
                        it.second
                    }
                    val json = createPayload(urls).toString().replace("\\", "")
                    RedeemScreen.SingleCode(
                        createBitMatrix(json),
                        urls.size,
                        tasksList.first().first
                    )
                }
                .toList()
        }
    }

    fun redeemPrescriptions(taskIds: List<String>, protocolText: String) {
        viewModelScope.launch(dispatchProvider.io()) {
            prescriptionUseCase.redeem(taskIds, true, true)
            val now = OffsetDateTime.now()
            taskIds.forEach { taskId ->
                val lowDetailEvent = LowDetailEventSimple(protocolText, now, taskId)
                prescriptionUseCase.saveLowDetailEvent(lowDetailEvent)
            }
        }
    }

    fun onShowSingleCodes(showSingleCodes: Boolean) {
        this.showSingleCodes.value = showSingleCodes
    }

    private fun createPayload(data: List<String>): JSONObject {
        val rootObject = JSONObject()
        val urls = JSONArray()
        for (d in data) {
            urls.put(d)
        }
        rootObject.put("urls", urls)
        return rootObject
    }

    private fun createBitMatrix(data: String): BitMatrix =
        // width & height is unused in the underlying implementation
        DataMatrixWriter().encode(data, BarcodeFormat.DATA_MATRIX, 1, 1)
}
