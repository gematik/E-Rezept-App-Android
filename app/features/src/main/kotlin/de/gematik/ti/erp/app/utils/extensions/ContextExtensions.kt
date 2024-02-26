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

package de.gematik.ti.erp.app.utils.extensions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.nfc.NfcManager
import android.os.Build
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import de.gematik.ti.erp.app.features.R

fun Context.isGooglePlayServiceAvailable(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS

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
