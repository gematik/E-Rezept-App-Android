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

package de.gematik.ti.erp.app.prescription.detail.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import de.gematik.ti.erp.app.analytics.PopUpName
import de.gematik.ti.erp.app.navigation.NavigationRouteNames
import de.gematik.ti.erp.app.navigation.NavigationRoutes
import de.gematik.ti.erp.app.navigation.Routes

object PrescriptionDetailRoutes : NavigationRoutes {
    override fun subGraphName() = "prescriptionDetail"
    const val TaskId = "taskId"
    const val SelectedMedication = "selectedMedication"
    const val SelectedIngredient = "selectedIngredient"
    object PrescriptionDetailScreen :
        Routes(
            NavigationRouteNames.PrescriptionDetailScreen.name,
            navArgument(TaskId) { type = NavType.StringType }
        ) {
        fun path(taskId: String) = path(TaskId to taskId)
    }
    object PrescriptionDetailMedicationOverviewScreen : Routes(
        NavigationRouteNames.PrescriptionDetailMedicationOverviewScreen.name,
        navArgument(TaskId) { type = NavType.StringType }
    ) {
        fun path(
            taskId: String
        ) = path(
            TaskId to taskId
        )
    }
    object PrescriptionDetailMedicationScreen : Routes(
        NavigationRouteNames.PrescriptionDetailMedicationScreen.name,
        navArgument(TaskId) { type = NavType.StringType },
        navArgument(SelectedMedication) { type = NavType.StringType }
    ) {
        fun path(
            taskId: String,
            selectedMedication: String
        ) = path(
            TaskId to taskId,
            SelectedMedication to selectedMedication
        )
    }
    object PrescriptionDetailPatientScreen : Routes(
        NavigationRouteNames.PrescriptionDetailPatientScreen.name,
        navArgument(TaskId) { type = NavType.StringType }
    ) {
        fun path(taskId: String) = path(TaskId to taskId)
    }
    object PrescriptionDetailPrescriberScreen : Routes(
        NavigationRouteNames.PrescriptionDetailPractitionerScreen.name,
        navArgument(TaskId) { type = NavType.StringType }
    ) {
        fun path(taskId: String) = path(TaskId to taskId)
    }
    object PrescriptionDetailOrganizationScreen : Routes(
        NavigationRouteNames.PrescriptionDetailOrganizationScreen.name,
        navArgument(TaskId) { type = NavType.StringType }
    ) {
        fun path(taskId: String) = path(TaskId to taskId)
    }
    object PrescriptionDetailAccidentInfoScreen : Routes(
        NavigationRouteNames.PrescriptionDetailAccidentInfoScreen.name,
        navArgument(TaskId) { type = NavType.StringType }
    ) {
        fun path(taskId: String) = path(TaskId to taskId)
    }
    object PrescriptionDetailTechnicalInformationScreen : Routes(
        NavigationRouteNames.PrescriptionDetailTechnicalInfoScreen.name,
        navArgument(TaskId) { type = NavType.StringType }
    ) {
        fun path(taskId: String) = path(TaskId to taskId)
    }
    object PrescriptionDetailIngredientsScreen : Routes(
        NavigationRouteNames.PrescriptionDetailMedicationIngredientsScreen.name,
        navArgument(SelectedIngredient) { type = NavType.StringType }
    ) {
        fun path(selectedIngredient: String) = path(SelectedIngredient to selectedIngredient)
    }
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
