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

package de.gematik.ti.erp.app.redeem.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.datamatrix.DataMatrixWriter
import de.gematik.ti.erp.app.DispatchProvider
import androidx.lifecycle.ViewModel
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@Stable
data class BitMatrixCode(val matrix: BitMatrix)

object RedeemScreen {
    @Stable
    data class SingleCode(
        val payload: String,
        val nrOfCodes: Int,
        val isScanned: Boolean
    )

    @Immutable
    data class State(
        val showSingleCodes: Boolean,
        val codes: List<SingleCode>
    )
}

class RedeemViewModel(
    private val prescriptionUseCase: PrescriptionUseCase,
    private val profilesUseCase: ProfilesUseCase,
    private val dispatchers: DispatchProvider
) : ViewModel() {
    private val showSingleCodes = MutableStateFlow(false)

    val defaultState = RedeemScreen.State(
        showSingleCodes = showSingleCodes.value,
        codes = listOf()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun screenState(taskIds: List<String>): Flow<RedeemScreen.State> =
        showSingleCodes.flatMapLatest { showSingle ->
            val maxTasks = if (showSingle) {
                1
            } else {
                if (taskIds.size < 5) {
                    2
                } else {
                    3
                }
            }

            generateRedeemCodes(taskIds, maxTasks).map {
                RedeemScreen.State(
                    showSingleCodes = showSingle,
                    codes = it
                )
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun generateRedeemCodes(
        taskIds: List<String>,
        maxTasksPerCode: Int
    ): Flow<List<RedeemScreen.SingleCode>> =
        profilesUseCase.activeProfile.flatMapLatest { activeProfile ->
            combine(
                prescriptionUseCase.syncedTasks(activeProfile.id),
                prescriptionUseCase.scannedTasks(activeProfile.id)
            ) { syncedTasks, scannedTasks ->
                val synced = syncedTasks.mapNotNull {
                    if (it.redeemState().isRedeemable() && it.taskId in taskIds) {
                        Triple(it.taskId, it.accessCode!!, it.medicationRequestMedicationName())
                    } else {
                        null
                    }
                }
                val scanned = scannedTasks.mapNotNull {
                    if (it.isRedeemable() && it.taskId in taskIds) {
                        Triple(it.taskId, it.accessCode, null)
                    } else {
                        null
                    }
                }

                (synced + scanned)
                    .map { (id, acc, name) ->
                        Pair(
                            name,
                            "Task/$id/\$accept?ac=$acc"
                        )
                    }
                    .windowed(maxTasksPerCode, maxTasksPerCode, partialWindows = true)
                    .map { tasksList ->
                        val urls = tasksList.map { it.second }
                        val json = createPayload(urls).toString().replace("\\", "")
                        RedeemScreen.SingleCode(
                            payload = json,
                            nrOfCodes = urls.size,
                            isScanned = tasksList.first().first == null // TODO add name handling
                        )
                    }
            }
        }

    fun redeemPrescriptions(taskIds: List<String>) {
        viewModelScope.launch(dispatchers.IO) {
            taskIds.forEach { taskId ->
                prescriptionUseCase.redeemScannedTask(taskId, true)
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
}

fun createBitMatrix(data: String): BitMatrix =
    // width & height is unused in the underlying implementation
    DataMatrixWriter().encode(data, BarcodeFormat.DATA_MATRIX, 1, 1)
