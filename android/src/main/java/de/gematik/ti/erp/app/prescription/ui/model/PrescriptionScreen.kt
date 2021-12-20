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

package de.gematik.ti.erp.app.prescription.ui.model

import de.gematik.ti.erp.app.common.usecase.model.Hint
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData

object PrescriptionScreen {
    data class State(
        val showDemoBanner: Boolean,
        val hints: List<Hint>,
        val prescriptions: List<PrescriptionUseCaseData.Recipe>,
        val redeemedPrescriptions: List<PrescriptionUseCaseData.Recipe>,
        val nowInEpochDays: Long
    )
}
