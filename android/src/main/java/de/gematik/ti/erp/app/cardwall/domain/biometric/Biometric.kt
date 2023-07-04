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

package de.gematik.ti.erp.app.cardwall.domain.biometric

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.biometric.BiometricManager
import de.gematik.ti.erp.app.Requirement

fun isDeviceSupportsBiometric(biometricMode: Int) = when (biometricMode) {
    BiometricManager.BIOMETRIC_SUCCESS,
    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
        true
    else ->
        false
}

@Requirement(
    "A_21583",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = "Check for support of BIOMETRIC_STRONG."
)
fun deviceStrongBiometricStatus(context: Context): Int {
    val biometricManager = BiometricManager.from(context)
    return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
}

@Requirement(
    "A_21578#2",
    "A_21579#2",
    "A_21580#2",
    "A_21580#2",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = "Check for availability of strongbox."
)
fun hasDeviceStrongBox(context: Context) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
    } else {
        false
    }
