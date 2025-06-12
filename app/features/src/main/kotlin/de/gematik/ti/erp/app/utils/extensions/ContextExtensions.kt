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

package de.gematik.ti.erp.app.utils.extensions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.nfc.NfcManager
import android.os.Build
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.pharmacy.ui.model.MapContent
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Coordinates
import de.gematik.ti.erp.app.utils.compose.canHandleIntent
import de.gematik.ti.erp.app.utils.compose.provideEmailIntent
import io.github.aakira.napier.Napier

fun Context.isGooglePlayServiceAvailable(): Boolean =
    try {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS
    } catch (e: Throwable) {
        // silently fail
        Napier.e { "isGooglePlayServiceAvailable failed: ${e.message}" }
        false
    }

fun Context.hasNFCTerminal(): Boolean =
    this.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)

fun Context.isNfcEnabled(): Boolean = if (hasNFCTerminal()) {
    val nfcManager = getSystemService(Context.NFC_SERVICE) as? NfcManager
    nfcManager?.defaultAdapter?.isEnabled ?: false
} else {
    false
}

fun Context.openAppPlayStoreLink() {
    startActivity(
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse(getString(R.string.app_playstore_link))
        )
    )
}

fun Context.gotoCoordinates(coordinates: Coordinates) {
    val (component, mapIntent) = mapsSelectionLauncher(coordinates)
    when {
        component != null -> startActivity(mapIntent)
        else -> startActivity(openGoogleMaps(coordinates).mapIntent)
    }
}

fun Context.openEmailClient(emailAddress: String) {
    val intent = provideEmailIntent(emailAddress)
    if (canHandleIntent(intent, packageManager)) {
        startActivity(intent)
    }
}

private fun Context.openGoogleMaps(coordinates: Coordinates): MapContent {
    val uri = Uri.parse(
        "https://www.google.com/maps/dir/?api=1&destination=${coordinates.latitude},${coordinates.longitude}"
    )
    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
    mapIntent.setPackage("com.google.android.apps.maps")
    return MapContent(
        component = mapIntent.resolveActivity(packageManager),
        mapIntent = mapIntent
    )
}

private fun Context.mapsSelectionLauncher(coordinates: Coordinates): MapContent {
    val uri = Uri.parse(
        "https://www.google.com/maps/dir/?api=1&destination=${coordinates.latitude},${coordinates.longitude}"
    )
    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
    return MapContent(
        component = mapIntent.resolveActivity(packageManager),
        mapIntent = mapIntent
    )
}
