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

package de.gematik.ti.erp.app.prescription.ui.model

import androidx.compose.runtime.Immutable
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData

object PrescriptionScreenData {
    @Immutable
    data class State(
        val prescriptions: List<PrescriptionUseCaseData.Prescription>,
        val redeemedPrescriptions: List<PrescriptionUseCaseData.Prescription>
    )

    val EmptyState = State(emptyList(), emptyList())
}
