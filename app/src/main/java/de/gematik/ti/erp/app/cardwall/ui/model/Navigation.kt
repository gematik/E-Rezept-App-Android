/*
 * Copyright (c) 2021 gematik GmbH
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
    object Intro : Route("CardWallIntro")
    object CardAccessNumber : Route("CardWallCardAccessNumber")
    object PersonalIdentificationNumber : Route("CardWallPersonalIdentificationNumber")
    object AuthenticationSelection : Route("CardWallAuthenticationSelection")
    object Authentication : Route("CardWallAuthentication")
    object Happy : Route("CardWallHappy")
    object Switch : Route("CardWallSwitch")
    object InsuranceApp : Route("InsuranceApp")
    object OrderHealthCard : Route("OrderHealthCard")
    object NoRoute : Route("")
}

enum class CardWallSwitchNavigation {
    INTRO, NO_ROUTE, INSURANCE_APP
}

fun mapCardWallNavigation(nav: CardWallSwitchNavigation) = when (nav) {
    CardWallSwitchNavigation.INTRO -> CardWallNavigation.Intro
    CardWallSwitchNavigation.NO_ROUTE -> CardWallNavigation.NoRoute
    CardWallSwitchNavigation.INSURANCE_APP -> CardWallNavigation.InsuranceApp
}
