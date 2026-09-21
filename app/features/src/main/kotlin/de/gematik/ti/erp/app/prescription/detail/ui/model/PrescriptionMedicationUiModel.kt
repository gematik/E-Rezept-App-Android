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

package de.gematik.ti.erp.app.prescription.detail.ui.model

import de.gematik.ti.erp.app.task.model.MedicationDispenseErpModel
import de.gematik.ti.erp.app.task.model.MedicationRequestErpModel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

/**
 * UI-level navigation model for the medication detail screen.
 * Carries either a medication request or a dispense from [TaskErpModel] to
 * [PrescriptionDetailMedicationScreen] without going through legacy SyncedTaskData types.
 */
@Serializable(with = PrescriptionMedicationUiModelSerializer::class)
@SerialName("PrescriptionMedicationUiModel")
sealed interface PrescriptionMedicationUiModel {

    @Serializable
    @SerialName("Request")
    data class Request(val medicationRequest: MedicationRequestErpModel?) : PrescriptionMedicationUiModel

    @Serializable
    @SerialName("Dispense")
    data class Dispense(val dispense: MedicationDispenseErpModel) : PrescriptionMedicationUiModel
}

object PrescriptionMedicationUiModelSerializer :
    JsonContentPolymorphicSerializer<PrescriptionMedicationUiModel>(PrescriptionMedicationUiModel::class) {
    override fun selectDeserializer(element: JsonElement): KSerializer<out PrescriptionMedicationUiModel> =
        when {
            "medicationRequest" in element.jsonObject -> PrescriptionMedicationUiModel.Request.serializer()
            else -> PrescriptionMedicationUiModel.Dispense.serializer()
        }
}
