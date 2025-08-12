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

package de.gematik.ti.erp.app.authentication.observer

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import de.gematik.ti.erp.app.authentication.presentation.ChooseAuthenticationController
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier

@Composable
fun ChooseAuthenticationNavigationEventsListener(
    controller: ChooseAuthenticationController,
    navController: NavController
) {
    with(controller) {
        chooseAuthenticationNavigationEvents.showCardWallIntroScreenEvent.listen { id ->
            navController.navigate(CardWallRoutes.CardWallIntroScreen.path(id))
        }
        chooseAuthenticationNavigationEvents.showCardWallWithFilledCanEvent.listen { cardWallData ->
            navController.navigate(
                CardWallRoutes.CardWallPinScreen.path(
                    profileIdentifier = cardWallData.profileId,
                    can = cardWallData.can
                )
            )
        }
        chooseAuthenticationNavigationEvents.showCardWallIntroScreenWithGidEvent.listen { gidData ->
            navController.navigate(
                CardWallRoutes.CardWallIntroScreen.pathWithGid(
                    gidNavigationData = gidData
                )
            )
        }
        chooseAuthenticationNavigationEvents.showCardWallGidListScreenEvent.listen { profileId: ProfileIdentifier ->
            navController.navigate(
                CardWallRoutes.CardWallGidListScreen.path(
                    profileId
                )
            )
        }
        chooseAuthenticationNavigationEvents.showCardWallSelectInsuranceScreenEvent.listen { profileId: ProfileIdentifier ->
            navController.navigate(
                CardWallRoutes.CardWallSelectInsuranceTypeBottomSheetScreen.path(
                    profileId
                )
            )
        }
        chooseAuthenticationNavigationEvents.showCardWallGidListScreenWithGidEvent.listen { gidData ->
            navController.navigate(
                CardWallRoutes.CardWallGidListScreen.pathWithGid(
                    gidNavigationData = gidData
                )
            )
        }
    }
}
