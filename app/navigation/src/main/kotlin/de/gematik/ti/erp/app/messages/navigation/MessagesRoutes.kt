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

package de.gematik.ti.erp.app.messages.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.navigation.NavigationRouteNames
import de.gematik.ti.erp.app.navigation.NavigationRoutes
import de.gematik.ti.erp.app.navigation.Routes
import de.gematik.ti.erp.app.navigation.fromNavigationString
import de.gematik.ti.erp.app.navigation.toNavigationString

object MessagesRoutes : NavigationRoutes {
    override fun subGraphName() = "messages"
    const val MESSAGE_NAV_ORDER_ID = "MESSAGE_NAV_ORDER_ID"
    const val MESSAGE_NAV_ORDER_DETAIL = "MESSAGE_NAV_ORDER_DETAIL"
    const val MESSAGE_NAV_SELECTED_MESSAGE = "MESSAGE_NAV_SELECTED_MESSAGE"
    const val MESSAGE_NAV_IS_LOCAL_MESSAGE = "MESSAGE_NAV_IS_LOCAL_MESSAGE"

    object MessageListScreen : Routes(NavigationRouteNames.MessageListScreen.name)

    object MessageDetailScreen : Routes(
        NavigationRouteNames.MessageDetailScreen.name,
        navArgument(MESSAGE_NAV_ORDER_ID) { type = NavType.StringType },
        navArgument(MESSAGE_NAV_IS_LOCAL_MESSAGE) { type = NavType.BoolType }
    ) {
        fun path(orderId: String, isLocalMessage: Boolean = false) =
            MessageDetailScreen.path(MESSAGE_NAV_ORDER_ID to orderId, MESSAGE_NAV_IS_LOCAL_MESSAGE to isLocalMessage)
    }

    object MessageBottomSheetScreen : Routes(
        NavigationRouteNames.MessageBottomSheetScreen.name,
        navArgument(MESSAGE_NAV_ORDER_DETAIL) { type = NavType.StringType },
        navArgument(MESSAGE_NAV_SELECTED_MESSAGE) { type = NavType.StringType }
    ) {
        fun path(orderDetail: OrderUseCaseData.OrderDetail, selectedMessage: OrderUseCaseData.Message) =
            MessageBottomSheetScreen.path(
                MESSAGE_NAV_ORDER_DETAIL to orderDetail.toNavigationString(),
                MESSAGE_NAV_SELECTED_MESSAGE to selectedMessage.toNavigationString()
            )
    }
}

data class MessagesRoutesBackStackEntryArguments(
    private val navBackStackEntry: NavBackStackEntry
) {
    val orderId
        get() = requireNotNull(navBackStackEntry.arguments?.getString(MessagesRoutes.MESSAGE_NAV_ORDER_ID))

    val isLocalMessage
        get() = (navBackStackEntry.arguments?.getBoolean(MessagesRoutes.MESSAGE_NAV_IS_LOCAL_MESSAGE)) ?: false

    val orderDetail
        get(): OrderUseCaseData.OrderDetail? =
            navBackStackEntry.arguments?.let { bundle ->
                bundle.getString(MessagesRoutes.MESSAGE_NAV_ORDER_DETAIL)?.let {
                    fromNavigationString<OrderUseCaseData.OrderDetail>(it)
                }
            }

    val selectedMessage
        get(): OrderUseCaseData.Message? =
            navBackStackEntry.arguments?.let { bundle ->
                bundle.getString(MessagesRoutes.MESSAGE_NAV_SELECTED_MESSAGE)?.let {
                    fromNavigationString<OrderUseCaseData.Message>(it)
                }
            }
}
