/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.pkv.presentation

import de.gematik.ti.erp.app.consent.model.ConsentState
import de.gematik.ti.erp.app.consent.model.ConsentState.Companion.isConsentGranted
import de.gematik.ti.erp.app.consent.model.ConsentState.Companion.isNotGranted

// call this on screens that need consent in a LaunchEffect or/and on pull to refresh
object ConsentValidator {

    fun validateAndExecute(
        isSsoTokenValid: Boolean,
        consentState: ConsentState,
        getChargeConsent: () -> Unit,
        onConsentGranted: () -> Unit,
        grantConsent: (() -> Unit)? = null
    ) {
        if (!isSsoTokenValid) return
        when {
            consentState is ConsentState.ValidState.UnknownConsent || consentState is ConsentState.ValidState.NotGranted -> getChargeConsent()
            consentState.isConsentGranted() -> onConsentGranted()
            consentState.isNotGranted() -> grantConsent?.invoke()
        }
    }
}
