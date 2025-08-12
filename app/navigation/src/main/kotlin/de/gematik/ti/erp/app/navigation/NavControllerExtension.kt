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

package de.gematik.ti.erp.app.navigation

import androidx.navigation.NavController

/**
 * [navigateAndClearStack] navigates to the given [route] by excluding screens
 * up to [excludeUpTodRoute] if it is not null.
 * If it is null then it excludes it up to the root
 * @param route The route to be navigated to.
 * @param excludeUpTodRoute Up to which route the screens need to be removed from the stack
 */
fun NavController.navigateAndClearStack(
    route: String,
    isLaunchSingleTop: Boolean = true,
    isInclusive: Boolean = true,
    excludeUpTodRoute: String? = null
) {
    navigate(route = route) {
        launchSingleTop = isLaunchSingleTop
        excludeUpTodRoute?.let {
            // pops up to the point that is given
            popUpTo(it) {
                inclusive = isInclusive
            }
        } ?: run {
            // pops up to to the start of the app
            popUpTo(0) {
                inclusive = isInclusive
            }
        }
    }
}
