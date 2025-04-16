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

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold

@Composable
fun LocationPermissionDeniedDialog(
    event: ComposableEvent<Unit>,
    dialog: DialogScaffold,
    onClick: () -> Unit
) {
    event.listen {
        dialog.show {
            LocationPermissionDeniedDialog(
                onClick = {
                    onClick()
                    it.dismiss()
                }
            )
        }
    }
}

@Composable
fun LocationServicesNotAvailableDialog(
    event: ComposableEvent<Unit>,
    dialog: DialogScaffold,
    onClickDismiss: () -> Unit,
    onClickSettings: () -> Unit
) {
    event.listen {
        dialog.show {
            LocationServicesNotAvailableDialog(
                onClickDismiss = {
                    onClickDismiss()
                    it.dismiss()
                },
                onClickSettings = {
                    onClickSettings()
                    it.dismiss()
                }
            )
        }
    }
}

@Composable
private fun LocationPermissionDeniedDialog(
    onClick: () -> Unit
) {
    ErezeptAlertDialog(
        title = stringResource(R.string.search_pharmacies_location_na_header),
        body = stringResource(R.string.search_pharmacies_location_na_header_info),
        onDismissRequest = onClick
    )
}

@Composable
fun LocationServicesNotAvailableDialog(
    onClickDismiss: () -> Unit,
    onClickSettings: () -> Unit
) {
    ErezeptAlertDialog(
        title = stringResource(R.string.search_pharmacies_location_na_header),
        bodyText = stringResource(R.string.search_pharmacies_location_na_services),
        confirmText = stringResource(R.string.search_pharmacies_location_na_settings),
        dismissText = stringResource(R.string.cancel),
        onDismissRequest = onClickDismiss,
        onConfirmRequest = onClickSettings
    )
}

@LightDarkPreview
@Composable
fun LocationPermissionDeniedDialogPreview() {
    PreviewAppTheme {
        LocationPermissionDeniedDialog {}
    }
}

@LightDarkPreview
@Composable
fun LocationServicesNotAvailableDialogPreview() {
    PreviewAppTheme {
        LocationServicesNotAvailableDialog({}, {})
    }
}
