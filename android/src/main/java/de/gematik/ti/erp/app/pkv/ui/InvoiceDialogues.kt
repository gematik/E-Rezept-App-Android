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

package de.gematik.ti.erp.app.pkv.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog

@Composable
fun RevokeConsentDialog(onCancel: () -> Unit, onRevokeConsent: () -> Unit) {
    CommonAlertDialog(
        header = stringResource(R.string.profile_revoke_consent_header),
        info = stringResource(R.string.profile_revoke_consent_info),
        cancelText = stringResource(R.string.profile_invoices_cancel),
        actionText = stringResource(R.string.profile_revoke_consent),
        cancelTextColor = AppTheme.colors.primary600,
        actionTextColor = AppTheme.colors.red600,
        onCancel = onCancel,
        onClickAction = onRevokeConsent
    )
}

@Composable
fun DeleteInvoiceDialog(onCancel: () -> Unit, onDeleteInvoice: () -> Unit) {
    CommonAlertDialog(
        header = stringResource(R.string.profile_delete_invoice_header),
        info = stringResource(R.string.profile_delete_invoice_info),
        cancelText = stringResource(R.string.profile_invoices_cancel),
        actionText = stringResource(R.string.profile_delete_invoice),
        cancelTextColor = AppTheme.colors.primary600,
        actionTextColor = AppTheme.colors.red600,
        onCancel = onCancel,
        onClickAction = onDeleteInvoice
    )
}

@Composable
fun GrantConsentDialog(onCancel: () -> Unit, onGrantConsent: () -> Unit) {
    CommonAlertDialog(
        header = stringResource(R.string.profile_grant_consent_header),
        info = stringResource(R.string.profile_grant_consent_info),
        cancelText = stringResource(R.string.profile_invoices_cancel),
        actionText = stringResource(R.string.profile_grant_consent),
        onCancel = onCancel,
        onClickAction = onGrantConsent
    )
}
