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

package de.gematik.ti.erp.app.prescription.detail.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import de.gematik.ti.erp.app.navigation.NavigationRouteNames
import de.gematik.ti.erp.app.navigation.NavigationRoutes
import de.gematik.ti.erp.app.navigation.Routes

object PrescriptionDetailRoutes : NavigationRoutes {
    override fun subGraphName() = "prescriptionDetail"

    const val PRESCRIPTION_DETAIL_NAV_TASK_ID = "taskId"
    const val PRESCRIPTION_DETAIL_NAV_SELECTED_MEDICATION = "selectedMedication"
    const val PRESCRIPTION_DETAIL_NAV_SELECTED_INGREDIENT = "selectedIngredient"
    const val BOTTOM_SHEET_TITLE_ID = "BOTTOM_SHEET_TITLE_ID"
    const val BOTTOM_SHEET_INFO_ID = "BOTTOM_SHEET_INFO_ID"

    object PrescriptionDetailScreen :
        Routes(
            NavigationRouteNames.PrescriptionDetailScreen.name,
            navArgument(PRESCRIPTION_DETAIL_NAV_TASK_ID) { type = NavType.StringType }
        ) {
        fun path(taskId: String) = path(PRESCRIPTION_DETAIL_NAV_TASK_ID to taskId)
    }

    object PrescriptionDetailMedicationOverviewScreen : Routes(
        NavigationRouteNames.PrescriptionDetailMedicationOverviewScreen.name,
        navArgument(PRESCRIPTION_DETAIL_NAV_TASK_ID) { type = NavType.StringType }
    ) {
        fun path(taskId: String) =
            path(
                PRESCRIPTION_DETAIL_NAV_TASK_ID to taskId
            )
    }

    object PrescriptionDetailMedicationScreen : Routes(
        NavigationRouteNames.PrescriptionDetailMedicationScreen.name,
        navArgument(PRESCRIPTION_DETAIL_NAV_TASK_ID) { type = NavType.StringType },
        navArgument(PRESCRIPTION_DETAIL_NAV_SELECTED_MEDICATION) { type = NavType.StringType }
    ) {
        fun path(
            taskId: String,
            selectedMedication: String
        ) = path(
            PRESCRIPTION_DETAIL_NAV_TASK_ID to taskId,
            PRESCRIPTION_DETAIL_NAV_SELECTED_MEDICATION to selectedMedication
        )
    }

    object PrescriptionDetailPatientScreen : Routes(
        NavigationRouteNames.PrescriptionDetailPatientScreen.name,
        navArgument(PRESCRIPTION_DETAIL_NAV_TASK_ID) { type = NavType.StringType }
    ) {
        fun path(taskId: String) = path(PRESCRIPTION_DETAIL_NAV_TASK_ID to taskId)
    }

    object PrescriptionDetailPrescriberScreen : Routes(
        NavigationRouteNames.PrescriptionDetailPractitionerScreen.name,
        navArgument(PRESCRIPTION_DETAIL_NAV_TASK_ID) { type = NavType.StringType }
    ) {
        fun path(taskId: String) = path(PRESCRIPTION_DETAIL_NAV_TASK_ID to taskId)
    }

    object PrescriptionDetailOrganizationScreen : Routes(
        NavigationRouteNames.PrescriptionDetailOrganizationScreen.name,
        navArgument(PRESCRIPTION_DETAIL_NAV_TASK_ID) { type = NavType.StringType }
    ) {
        fun path(taskId: String) = path(PRESCRIPTION_DETAIL_NAV_TASK_ID to taskId)
    }

    object PrescriptionDetailAccidentInfoScreen : Routes(
        NavigationRouteNames.PrescriptionDetailAccidentInfoScreen.name,
        navArgument(PRESCRIPTION_DETAIL_NAV_TASK_ID) { type = NavType.StringType }
    ) {
        fun path(taskId: String) = path(PRESCRIPTION_DETAIL_NAV_TASK_ID to taskId)
    }

    object PrescriptionDetailTechnicalInformationScreen : Routes(
        NavigationRouteNames.PrescriptionDetailTechnicalInfoScreen.name,
        navArgument(PRESCRIPTION_DETAIL_NAV_TASK_ID) { type = NavType.StringType }
    ) {
        fun path(taskId: String) = path(PRESCRIPTION_DETAIL_NAV_TASK_ID to taskId)
    }

    object PrescriptionDetailIngredientsScreen : Routes(
        NavigationRouteNames.PrescriptionDetailMedicationIngredientsScreen.name,
        navArgument(PRESCRIPTION_DETAIL_NAV_SELECTED_INGREDIENT) { type = NavType.StringType }
    ) {
        fun path(selectedIngredient: String) = path(PRESCRIPTION_DETAIL_NAV_SELECTED_INGREDIENT to selectedIngredient)
    }

    object SelPayerPrescriptionBottomSheetScreen : Routes(
        NavigationRouteNames.PrescriptionDetailSelfPayerPrescriptionBottomSheetScreen.name,
        navArgument(BOTTOM_SHEET_TITLE_ID) { type = NavType.IntType },
        navArgument(BOTTOM_SHEET_INFO_ID) { type = NavType.IntType }
    ) {
        fun path(
            titleId: Int,
            infoId: Int
        ) = path(BOTTOM_SHEET_TITLE_ID to titleId, BOTTOM_SHEET_INFO_ID to infoId)
    }

    object AdditionalFeeNotExemptBottomSheetScreen : Routes(
        NavigationRouteNames.PrescriptionDetailAdditionalFeeNotExemptBottomSheetScreen.name,
        navArgument(BOTTOM_SHEET_TITLE_ID) { type = NavType.IntType },
        navArgument(BOTTOM_SHEET_INFO_ID) { type = NavType.IntType }
    ) {
        fun path(
            titleId: Int,
            infoId: Int
        ) = path(BOTTOM_SHEET_TITLE_ID to titleId, BOTTOM_SHEET_INFO_ID to infoId)
    }

    object AdditionalFeeExemptBottomSheetScreen : Routes(
        NavigationRouteNames.PrescriptionDetailAdditionalFeeExemptBottomSheetScreen.name,
        navArgument(BOTTOM_SHEET_TITLE_ID) { type = NavType.IntType },
        navArgument(BOTTOM_SHEET_INFO_ID) { type = NavType.IntType }
    ) {
        fun path(
            titleId: Int,
            infoId: Int
        ) = path(BOTTOM_SHEET_TITLE_ID to titleId, BOTTOM_SHEET_INFO_ID to infoId)
    }

    object FailureBottomSheetScreen : Routes(
        NavigationRouteNames.PrescriptionDetailFailureBottomSheetScreen.name,
        navArgument(BOTTOM_SHEET_TITLE_ID) { type = NavType.IntType },
        navArgument(BOTTOM_SHEET_INFO_ID) { type = NavType.IntType }
    ) {
        fun path(
            titleId: Int,
            infoId: Int
        ) = path(BOTTOM_SHEET_TITLE_ID to titleId, BOTTOM_SHEET_INFO_ID to infoId)
    }

    object ScannedBottomSheetScreen : Routes(
        NavigationRouteNames.PrescriptionDetailScannedBottomSheetScreen.name,
        navArgument(BOTTOM_SHEET_TITLE_ID) { type = NavType.IntType },
        navArgument(BOTTOM_SHEET_INFO_ID) { type = NavType.IntType }
    ) {
        fun path(
            titleId: Int,
            infoId: Int
        ) = path(BOTTOM_SHEET_TITLE_ID to titleId, BOTTOM_SHEET_INFO_ID to infoId)
    }

    object DirectAssignmentBottomSheetScreen : Routes(
        NavigationRouteNames.PrescriptionDetailDirectAssignmentBottomSheetScreen.name,
        navArgument(BOTTOM_SHEET_TITLE_ID) { type = NavType.IntType },
        navArgument(BOTTOM_SHEET_INFO_ID) { type = NavType.IntType }
    ) {
        fun path(
            titleId: Int,
            infoId: Int
        ) = path(BOTTOM_SHEET_TITLE_ID to titleId, BOTTOM_SHEET_INFO_ID to infoId)
    }

    object SubstitutionAllowedBottomSheetScreen : Routes(
        NavigationRouteNames.PrescriptionDetailSubstitutionAllowedBottomSheetScreen.name,
        navArgument(BOTTOM_SHEET_TITLE_ID) { type = NavType.IntType },
        navArgument(BOTTOM_SHEET_INFO_ID) { type = NavType.IntType }
    ) {
        fun path(
            titleId: Int,
            infoId: Int
        ) = path(BOTTOM_SHEET_TITLE_ID to titleId, BOTTOM_SHEET_INFO_ID to infoId)
    }

    object SubstitutionNotAllowedBottomSheetScreen : Routes(
        NavigationRouteNames.PrescriptionDetailSubstitutionNotAllowedBottomSheetScreen.name,
        navArgument(BOTTOM_SHEET_TITLE_ID) { type = NavType.IntType },
        navArgument(BOTTOM_SHEET_INFO_ID) { type = NavType.IntType }
    ) {
        fun path(
            titleId: Int,
            infoId: Int
        ) = path(BOTTOM_SHEET_TITLE_ID to titleId, BOTTOM_SHEET_INFO_ID to infoId)
    }

    object EmergencyFeeExemptBottomSheetScreen : Routes(
        NavigationRouteNames.PrescriptionDetailEmergencyFeeExemptBottomSheetScreen.name,
        navArgument(BOTTOM_SHEET_TITLE_ID) { type = NavType.IntType },
        navArgument(BOTTOM_SHEET_INFO_ID) { type = NavType.IntType }
    ) {
        fun path(
            titleId: Int,
            infoId: Int
        ) = path(BOTTOM_SHEET_TITLE_ID to titleId, BOTTOM_SHEET_INFO_ID to infoId)
    }

    object EmergencyFeeNotExemptBottomSheetScreen : Routes(
        NavigationRouteNames.PrescriptionDetailEmergencyFeeNotExemptBottomSheetScreen.name,
        navArgument(BOTTOM_SHEET_TITLE_ID) { type = NavType.IntType },
        navArgument(BOTTOM_SHEET_INFO_ID) { type = NavType.IntType }
    ) {
        fun path(
            titleId: Int,
            infoId: Int
        ) = path(BOTTOM_SHEET_TITLE_ID to titleId, BOTTOM_SHEET_INFO_ID to infoId)
    }

    object HowLongValidBottomSheetScreen : Routes(
        NavigationRouteNames.PrescriptionDetailHowLongValidBottomSheetScreen.name,
        navArgument(PRESCRIPTION_DETAIL_NAV_TASK_ID) { type = NavType.StringType }
    ) {
        fun path(taskId: String) = path(PRESCRIPTION_DETAIL_NAV_TASK_ID to taskId)
    }
}
