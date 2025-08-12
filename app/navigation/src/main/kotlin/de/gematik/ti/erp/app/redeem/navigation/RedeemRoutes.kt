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

package de.gematik.ti.erp.app.redeem.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import de.gematik.ti.erp.app.navigation.NavigationRouteNames
import de.gematik.ti.erp.app.navigation.NavigationRoutes
import de.gematik.ti.erp.app.navigation.Routes
import de.gematik.ti.erp.app.navigation.fromNavigationString
import de.gematik.ti.erp.app.navigation.toNavigationString
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty

object RedeemRoutes : NavigationRoutes {
    override fun subGraphName(): String = "redeem"

    const val REDEEM_NAV_SELECTED_PHARMACY = "Pharmacy"
    const val REDEEM_NAV_ORDER_OPTION = "OrderOption"
    const val REDEEM_NAV_MODAL_BEHAVIOUR = "ModalBehaviour"
    const val REDEEM_NAV_TASK_ID = "taskId"

    object HowToRedeemScreen : Routes(NavigationRouteNames.HowToRedeemScreen.name)
    object RedeemLocal : Routes(
        NavigationRouteNames.RedeemLocal.name,
        navArgument(REDEEM_NAV_TASK_ID) { type = NavType.StringType }
    ) {
        fun path(taskId: String): String = path(REDEEM_NAV_TASK_ID to taskId)
    }

    object RedeemOnlinePreferences : Routes(NavigationRouteNames.RedeemOnline.name)

    object RedeemPrescriptionSelection : Routes(
        path = NavigationRouteNames.RedeemPrescriptionSelection.name,
        navArgument(REDEEM_NAV_MODAL_BEHAVIOUR) { type = NavType.BoolType }
    ) {
        fun path(isModal: Boolean): String = path(REDEEM_NAV_MODAL_BEHAVIOUR to isModal)
    }

    object RedeemOrderOverviewScreen : Routes(
        path = NavigationRouteNames.RedeemOrderOverviewScreen.name,
        navArgument(REDEEM_NAV_SELECTED_PHARMACY) { type = NavType.StringType },
        navArgument(REDEEM_NAV_ORDER_OPTION) { type = NavType.StringType },
        navArgument(REDEEM_NAV_TASK_ID) { type = NavType.StringType }

    ) {
        fun path(
            pharmacy: PharmacyUseCaseData.Pharmacy?,
            orderOption: PharmacyScreenData.OrderOption?,
            taskId: String?
        ): String = path(
            REDEEM_NAV_SELECTED_PHARMACY to (pharmacy?.let { it.toNavigationString() } ?: ""),
            REDEEM_NAV_ORDER_OPTION to (orderOption?.name ?: ""),
            REDEEM_NAV_TASK_ID to (taskId ?: "")
        )
    }

    object RedeemEditShippingContactScreen : Routes(
        path = NavigationRouteNames.RedeemEditShippingContactScreen.name,
        navArgument(REDEEM_NAV_ORDER_OPTION) { type = NavType.StringType }
    ) {
        fun path(orderOption: PharmacyScreenData.OrderOption): String = path(REDEEM_NAV_ORDER_OPTION to orderOption.name)
    }
}

class RedeemRouteBackStackEntryArguments(
    private val navBackStackEntry: NavBackStackEntry
) {
    fun getPharmacy(): PharmacyUseCaseData.Pharmacy? =
        navBackStackEntry.arguments?.let { bundle ->
            bundle.getString(RedeemRoutes.REDEEM_NAV_SELECTED_PHARMACY)?.let {
                when {
                    it.isNotNullOrEmpty() -> fromNavigationString<PharmacyUseCaseData.Pharmacy>(it)
                    else -> null
                }
            }
        }

    fun getOrderOption() =
        navBackStackEntry.arguments?.let { bundle ->
            bundle.getString(RedeemRoutes.REDEEM_NAV_ORDER_OPTION)?.let {
                when {
                    it.isNotNullOrEmpty() -> PharmacyScreenData.OrderOption.valueOf(it)
                    else -> null
                }
            }
        }
}
