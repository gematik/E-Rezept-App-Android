package de.gematik.ti.erp.app.profiles.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.idp.repository.SingleSignOnToken

@Composable
fun connectionText(
    ssoToken: SingleSignOnToken?,
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
