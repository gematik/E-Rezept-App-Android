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

package de.gematik.ti.erp.app.authentication.presentation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import org.bouncycastle.util.encoders.Base64
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.ECGenParameterSpec

fun deviceSupportsAuthenticationMethod(status: Int) = when (status) {
    BiometricManager.BIOMETRIC_SUCCESS,
    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
        true

    else ->
        false
}

fun deviceHasAuthenticationMethodEnabled(status: Int) = status == BiometricManager.BIOMETRIC_SUCCESS

fun Context.deviceSecurityStatus(): Int {
    val biometricManager = BiometricManager.from(this)
    return biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
    )
}

fun Context.biometricStatus(): Int {
    val biometricManager = BiometricManager.from(this)
    return biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
    )
}

fun Context.deviceStrongBoxStatus() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        this.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
    } else {
        false
    }

fun Context.deviceHardwareBackedKeystoreStatus(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.packageManager.hasSystemFeature(PackageManager.FEATURE_HARDWARE_KEYSTORE) ||
            this.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
    } else {
        try {
            // create a temporary key pair to check if it is hardware backed
            val aliasOfSecureElementEntry = Base64.toBase64String("HardWareBackedAlias".toByteArray())

            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                "AndroidKeyStore"
            )
            val parameterSpec = KeyGenParameterSpec.Builder(
                aliasOfSecureElementEntry,
                KeyProperties.PURPOSE_SIGN
            ).apply {
                setDigests(KeyProperties.DIGEST_SHA256)
                setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            }.build()
            keyPairGenerator.initialize(parameterSpec)
            val keyPair = keyPairGenerator.generateKeyPair()
            val key = keyPair.private

            val factory = KeyFactory.getInstance(key.algorithm, "AndroidKeyStore")
            val keyInfo: KeyInfo = factory.getKeySpec(key, KeyInfo::class.java)
            val isHardWareBacked = keyInfo.isInsideSecureHardware

            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry(aliasOfSecureElementEntry)

            isHardWareBacked
        } catch (e: Exception) {
            false
        }
    }
}

fun enrollBiometricsIntent(): Intent {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val intent = Intent(Settings.ACTION_BIOMETRIC_ENROLL)
        intent.putExtra(
            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
    } else {
        Intent(Settings.ACTION_SETTINGS)
    }
}

fun enrollDeviceSecurityIntent(): Intent {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Intent(Settings.ACTION_SECURITY_SETTINGS)
    } else {
        Intent(Settings.ACTION_SETTINGS)
    }
}
