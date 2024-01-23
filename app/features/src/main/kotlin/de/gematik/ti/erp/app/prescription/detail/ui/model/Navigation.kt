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

package de.gematik.ti.erp.app.prescription.detail.ui.model

import androidx.compose.runtime.Immutable
import de.gematik.ti.erp.app.Route

object PrescriptionDetailsNavigationScreens {
    object Overview : Route("prescriptionDetail")
    object MedicationOverview : Route("prescriptionDetail_medicationOverview")
    object Medication : Route("prescriptionDetail_medication")
    object Patient : Route("prescriptionDetail_patient")
    object Prescriber : Route("prescriptionDetail_practitioner")
    object Organization : Route("prescriptionDetail_organization")
    object Accident : Route("prescriptionDetail_accidentInfo")
    object TechnicalInformation : Route("prescriptionDetail_technicalInfo")
    object Ingredient : Route("prescriptionDetail_medication_ingredients")
}

object PrescriptionDetailsPopUpNames {
    object Validity : PopUpName("prescriptionDetail_prescriptionValidityInfo")
    object SubstitutionAllowed : PopUpName("prescriptionDetail_substitutionInfo")
    object DirectAssignment : PopUpName("prescriptionDetail_directAssignmentInfo")
    object EmergencyFee : PopUpName("prescriptionDetail_emergencyServiceFeeInfo")
    object AdditionalFee : PopUpName("prescriptionDetail_coPaymentInfo")
    object Scanned : PopUpName("prescriptionDetail_scannedPrescriptionInfo")
    object Failure : PopUpName("prescriptionDetail_errorInfo")
}

@Immutable
open class PopUpName(val name: String)
