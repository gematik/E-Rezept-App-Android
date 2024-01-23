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

import de.gematik.ti.erp.app.navigation.Routes

object UnlockEgkNavigation {
    object Intro : Routes("healthCardPassword_introduction")
    object CardAccessNumber : Routes("healthCardPassword_can")
    object PersonalUnblockingKey : Routes("healthCardPassword_puk")
    object OldSecret : Routes("healthCardPassword_oldPin")
    object NewSecret : Routes("healthCardPassword_pin")
    object UnlockEgk : Routes("healthCardPassword_readCard")
    object TroubleShooting : Routes("troubleShooting")
}
