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
    object Troubleshooting : Route("troubleShooting")
    object ExternalAuthenticator : Route("cardWall_extAuth")
    object Intro : Route("cardWall_introduction")
    object MissingCapabilities : Route("cardWall_notCapable")
    object CardAccessNumber : Route("cardWall_CAN")
    object PersonalIdentificationNumber : Route("cardWall_PIN")
    object AuthenticationSelection : Route("cardWall_saveLogin")
    object AlternativeOption : Route("cardWall_saveLoginSecurityInfo")
    object Authentication : Route("cardWall_readCard")
    object OrderHealthCard : Route("contactInsuranceCompany")
    object UnlockEgk : Route("healthCardPassword_introduction")
}
