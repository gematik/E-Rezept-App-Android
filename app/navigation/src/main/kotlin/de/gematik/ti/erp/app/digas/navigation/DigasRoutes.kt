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

package de.gematik.ti.erp.app.digas.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import de.gematik.ti.erp.app.navigation.NavigationRouteNames
import de.gematik.ti.erp.app.navigation.NavigationRoutes
import de.gematik.ti.erp.app.navigation.Routes

object DigasRoutes : NavigationRoutes {
    override fun subGraphName(): String = "digas"
    const val DIGAS_NAV_TASK_ID = "taskId"
    const val DIGAS_IS_READY = "DigaIsReady"
    const val DIGAS_NAV_TITLE = "title"
    const val DIGAS_NAV_DESCRIPTION = "description"
    const val DIGAS_NAV_LINK = "digaLink"
    const val DIGAS_NAV_PDF_LINK = "digaPdfLink"

    object DigasMainScreen : Routes(
        NavigationRouteNames.DigasMainScreen.name,
        navArgument(DIGAS_NAV_TASK_ID) { type = NavType.StringType },
        navArgument(DIGAS_IS_READY) {
            type = NavType.BoolType
            defaultValue = false
        }
    ) {
        fun path(taskId: String, isReady: Boolean) = path(DIGAS_NAV_TASK_ID to taskId, DIGAS_IS_READY to isReady)
    }

    object DigasValidityBottomSheetScreen : Routes(
        NavigationRouteNames.DigasValidityBottomSheetScreen.name,
        navArgument(DIGAS_NAV_TASK_ID) { type = NavType.StringType }
    ) {
        fun path(taskId: String) = path(DIGAS_NAV_TASK_ID to taskId)
    }

    object DigaSupportBottomSheetScreen : Routes(
        NavigationRouteNames.DigasSupportBottomSheetScreen.name,
        navArgument(DIGAS_NAV_LINK) { type = NavType.StringType },
        navArgument(DIGAS_NAV_PDF_LINK) { type = NavType.StringType }
    ) {
        fun path(link: String, pdfLink: String?) = path(DIGAS_NAV_LINK to link, DIGAS_NAV_PDF_LINK to pdfLink)
    }

    object DigaContributionInfoBottomSheetScreen : Routes(
        NavigationRouteNames.DigaContributionInfoSheetScreen.name
    )

    object DigasDescriptionScreen : Routes(
        NavigationRouteNames.DigasDescriptionScreen.name,
        navArgument(DIGAS_NAV_TITLE) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        },
        navArgument(name = DIGAS_NAV_DESCRIPTION) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }
    ) {
        fun path(title: String?, description: String?) = path(DIGAS_NAV_TITLE to title, DIGAS_NAV_DESCRIPTION to description)
    }

    object DigaFeedbackPromptScreen : Routes(NavigationRouteNames.DigaFeedbackPromptScreen.name)

    object InsuranceSearchListScreen : Routes(NavigationRouteNames.InsuranceSearchListScreen.name)
}
