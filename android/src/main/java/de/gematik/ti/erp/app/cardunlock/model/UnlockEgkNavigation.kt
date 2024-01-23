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

package de.gematik.ti.erp.app.cardunlock.model

import de.gematik.ti.erp.app.Route

object UnlockEgkNavigation {
    object Intro : Route("healthCardPassword_introduction")
    object CardAccessNumber : Route("healthCardPassword_can")
    object PersonalUnblockingKey : Route("healthCardPassword_puk")
    object OldSecret : Route("healthCardPassword_oldPin")
    object NewSecret : Route("healthCardPassword_pin")
    object UnlockEgk : Route("healthCardPassword_readCard")
    object TroubleShooting : Route("troubleShooting")
}
