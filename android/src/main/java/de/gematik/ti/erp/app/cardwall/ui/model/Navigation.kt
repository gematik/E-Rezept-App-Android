/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.cardwall.ui.model

import de.gematik.ti.erp.app.Route

object CardWallNavigation {
    object Troubleshooting : Route("TroubleShooting")
    object ExternalAuthenticator : Route("card_wall_external_authenticator_overview")
    object Intro : Route("card_wall_intro")
    object MissingCapabilities : Route("card_wall_missing_capabilities")
    object CardAccessNumber : Route("card_wall_card_access_number")

    object PersonalIdentificationNumber : Route("card_wall_personal_identification_number")
    object AuthenticationSelection : Route("card_wall_authentication_selection")
    object AlternativeOption : Route("card_wall_alternative_option")

    object Authentication : Route("card_wall_authentication")
    object OrderHealthCard : Route("card_wall_order_health_card")
    object UnlockEgk : Route("card_wall_unlock_egk")
}
