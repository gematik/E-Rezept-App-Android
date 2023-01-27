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

package de.gematik.ti.erp.app.profiles.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.idp.model.IdpData

@Composable
fun connectionText(
    ssoToken: IdpData.SingleSignOnToken?,
    lastAuthenticatedDate: String?
) = when {
    ssoToken != null && ssoToken.isValid() -> {
        stringResource(R.string.settings_profile_connected)
    }
    lastAuthenticatedDate != null -> {
        stringResource(id = R.string.settings_profile_last_authenticated_on, lastAuthenticatedDate)
    }
    else -> { stringResource(R.string.settings_profile_not_connected) }
}
