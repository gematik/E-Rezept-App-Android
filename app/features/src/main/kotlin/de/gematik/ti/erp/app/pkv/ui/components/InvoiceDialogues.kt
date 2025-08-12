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

@file:Suppress("UnusedPrivateMember")

package de.gematik.ti.erp.app.pkv.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold

@Composable
fun UserNotLoggedInDialog(
    dialogScaffold: DialogScaffold,
    onEvent: ComposableEvent<Unit>,
    onConfirmRequest: () -> Unit
) {
    onEvent.listen {
        dialogScaffold.show { dialog ->
            UserNotLoggedInDialog(
                onDismissRequest = {
                    dialog.dismiss()
                },
                onConfirmRequest = {
                    onConfirmRequest()
                    dialog.dismiss()
                }
            )
        }
    }
}

@Composable
private fun UserNotLoggedInDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit
) {
    ErezeptAlertDialog(
        title = stringResource(R.string.consent_error_connect),
        bodyText = stringResource(R.string.invoices_delete_not_logged_in_description),
        confirmText = stringResource(R.string.invoices_connect_btn),
        dismissText = stringResource(R.string.profile_invoices_cancel),
        onDismissRequest = {
            onDismissRequest()
        },
        onConfirmRequest = {
            onConfirmRequest()
        }
    )
}

@Composable
fun RevokeConsentDialog(
    dialogScaffold: DialogScaffold,
    onRevokeConsentEvent: ComposableEvent<Unit>,
    onRevokeConsent: () -> Unit
) {
    onRevokeConsentEvent.listen {
        dialogScaffold.show { dialog ->
            RevokeConsentDialog(
                onDismissRequest = {
                    dialog.dismiss()
                },
                onConfirmRequest = {
                    onRevokeConsent()
                    dialog.dismiss()
                }
            )
        }
    }
}

@Composable
private fun RevokeConsentDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit
) {
    ErezeptAlertDialog(
        title = stringResource(R.string.profile_revoke_consent_header),
        bodyText = stringResource(R.string.profile_revoke_consent_info),
        confirmText = stringResource(R.string.profile_revoke_consent),
        dismissText = stringResource(R.string.profile_invoices_cancel),
        onDismissRequest = {
            onDismissRequest()
        },
        onConfirmRequest = {
            onConfirmRequest()
        }
    )
}

@Composable
fun DeleteInvoiceDialog(
    dialogScaffold: DialogScaffold,
    onEvent: ComposableEvent<Unit>,
    onDeleteInvoice: () -> Unit
) {
    onEvent.listen {
        dialogScaffold.show {
            DeleteInvoiceDialog(
                onCancel = {
                    it.dismiss()
                },
                onDeleteInvoice = {
                    onDeleteInvoice()
                    it.dismiss()
                }
            )
        }
    }
}

@Composable
private fun DeleteInvoiceDialog(
    onCancel: () -> Unit,
    onDeleteInvoice: () -> Unit
) {
    ErezeptAlertDialog(
        title = stringResource(R.string.profile_delete_invoice_header),
        bodyText = stringResource(R.string.profile_delete_invoice_info),
        confirmText = stringResource(R.string.profile_delete_invoice),
        dismissText = stringResource(R.string.profile_invoices_cancel),
        confirmTextColor = AppTheme.colors.red600,
        dismissTextColor = AppTheme.colors.primary700,
        onDismissRequest = onCancel,
        onConfirmRequest = onDeleteInvoice
    )
}

@Composable
fun GrantConsentDialog(
    dialogScaffold: DialogScaffold,
    onGrantConsentEvent: ComposableEvent<Unit>,
    onShow: () -> Unit,
    onGrantConsent: () -> Unit
) {
    onGrantConsentEvent.listen {
        dialogScaffold.show { dialog ->
            onShow()
            GrantConsentDialog(
                onDismissRequest = { dialog.dismiss() },
                onConfirmRequest = {
                    onGrantConsent()
                    dialog.dismiss()
                }
            )
        }
    }
}

@Composable
private fun GrantConsentDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit
) {
    ErezeptAlertDialog(
        title = stringResource(R.string.get_consent_info_dialog_header),
        bodyText = stringResource(R.string.get_consent_info_dialog_info),
        confirmText = stringResource(R.string.get_consent_info_dialog_accept),
        dismissText = stringResource(R.string.get_consent_info_dialog_cancel),
        onDismissRequest = {
            onDismissRequest()
        },
        onConfirmRequest = {
            onConfirmRequest()
        }
    )
}

@LightDarkPreview
@Composable
private fun UserNotLoggedInDialogPreview() {
    PreviewAppTheme {
        UserNotLoggedInDialog({}, {})
    }
}

@LightDarkPreview
@Composable
private fun RevokeConsentDialogPreview() {
    PreviewAppTheme {
        RevokeConsentDialog({}, {})
    }
}

@LightDarkPreview
@Composable
private fun DeleteInvoiceDialogPreview() {
    PreviewAppTheme {
        DeleteInvoiceDialog({}, {})
    }
}

@LightDarkPreview
@Composable
private fun GrantConsentDialogPreview() {
    PreviewAppTheme {
        GrantConsentDialog({}, {})
    }
}
