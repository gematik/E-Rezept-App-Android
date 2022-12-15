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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyOrderState
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import org.json.JSONArray
import org.json.JSONObject

@Stable
class LocalRedeemState(
    private val prescriptions: State<List<PharmacyUseCaseData.PrescriptionOrder>>
) {
    @Immutable
    data class DMCode(
        val payload: String,
        val nrOfCodes: Int,
        val name: String?,
        val containsScanned: Boolean
    )

    var isSingleCodes by mutableStateOf(false)
        private set

    fun onSwitchToSingleCodes() {
        isSingleCodes = true
    }

    fun onSwitchToGroupedCodes() {
        isSingleCodes = false
    }

    val codes
        @Composable
        get() = derivedStateOf {
            val prescriptions = prescriptions.value

            @Suppress("MagicNumber")
            val maxTasks = if (isSingleCodes) {
                1
            } else {
                if (prescriptions.size < 5) {
                    2
                } else {
                    3
                }
            }

            prescriptions
                .map { prescription ->
                    prescription to "Task/${prescription.taskId}/\$accept?ac=${prescription.accessCode}"
                }
                .windowed(maxTasks, maxTasks, partialWindows = true)
                .map { codes ->
                    val prescriptions = codes.map { it.first }
                    val urls = codes.map { it.second }
                    val json = createPayload(urls).toString().replace("\\", "")
                    DMCode(
                        payload = json,
                        nrOfCodes = urls.size,
                        name = prescriptions.filter { it.title != null }.joinToString { it.title!! },
                        containsScanned = prescriptions.any { it.title == null }
                    )
                }
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

@Composable
fun rememberLocalRedeemState(
    orderState: PharmacyOrderState
): LocalRedeemState {
    val prescriptions = orderState.prescriptions
    return remember {
        LocalRedeemState(
            prescriptions
        )
    }
}
