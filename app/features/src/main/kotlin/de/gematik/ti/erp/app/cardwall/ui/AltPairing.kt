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

package de.gematik.ti.erp.app.cardwall.ui

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.secureRandomInstance
import io.github.aakira.napier.Napier
import kotlinx.coroutines.suspendCancellableCoroutine
import org.bouncycastle.util.encoders.Base64
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.PublicKey
import java.security.spec.ECGenParameterSpec
import kotlin.coroutines.resume

private const val KeyStoreAliasKeySize = 32 // in bytes
private const val KeyTimeout = 15 * 60 // in seconds

@Stable
class AltPairingProvider(
    private val activity: FragmentActivity,
    private val promptInfo: BiometricPrompt.PromptInfo
) {
    private val executor = ContextCompat.getMainExecutor(activity)

    sealed interface AuthResult {
        object Error : AuthResult
        object Authenticated : AuthResult

        @Stable
        class Initialized(val aliasOfSecureElementEntry: ByteArray, val publicKey: PublicKey) : AuthResult
    }

    @Requirement(
        "A_21576#1",
        "A_21578#1",
        "A_21579#1",
        "A_21580#1",
        "A_21580#1",
        "A_21581",
        "A_21585",
        "A_21586",
        "A_21587",
        "A_21588",
        "A_21589",
        "A_21590",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Initialize biometric authentication for strongbox backed devices."
    )
    @Requirement(
        "O.Biom_1#1",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Initialize biometric authentication for strongbox backed devices."
    )
    @RequiresApi(Build.VERSION_CODES.P)
    suspend fun initializeAndPrompt(): AuthResult = suspendCancellableCoroutine { continuation ->
        val aliasOfSecureElementEntry = ByteArray(KeyStoreAliasKeySize).apply {
            secureRandomInstance().nextBytes(this)
        }

        @Requirement(
            "O.Biom_7",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "The app uses the Android keystore to evaluate the biometric authentication"
        )
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore"
        )

        val parameterSpec = KeyGenParameterSpec.Builder(
            Base64.toBase64String(aliasOfSecureElementEntry),
            KeyProperties.PURPOSE_SIGN
        ).apply {
            @Requirement(
                "O.Biom_6",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "Biometric secured private keys are invalid whenever the biometrics setup changes. " +
                    "Invalidates biometry after changes"
            )
            setInvalidatedByBiometricEnrollment(true)
            setUserAuthenticationRequired(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // While the documentation of Android suggests to set this to zero, this is safe to use.
                // If the key is used and later, e.g. a fingerprint is added, the keystore implementation of
                // Android will throw a `KeyPermanentlyInvalidatedException`. Later on if the user restarts
                // the phone, the key is permanently invalidated and the actual `UserNotAuthenticatedException`
                // is thrown.
                @Requirement(
                    "O.Biom_2#3",
                    "O.Biom_3#3",
                    sourceSpecification = "BSI-eRp-ePA",
                    rationale = "Require Biometric STRONG."
                )
                setUserAuthenticationParameters(KeyTimeout, KeyProperties.AUTH_BIOMETRIC_STRONG)
            }
            setIsStrongBoxBacked(true)
            setDigests(KeyProperties.DIGEST_SHA256)

            setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
        }.build()

        keyPairGenerator.initialize(parameterSpec)
        val keyPair = keyPairGenerator.generateKeyPair()
        val publicKey = keyPair.public // required to init

        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    continuation.resume(
                        AuthResult.Initialized(
                            aliasOfSecureElementEntry = aliasOfSecureElementEntry,
                            publicKey = publicKey
                        )
                    )
                }

                override fun onAuthenticationError(
                    errCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errCode, errString)

                    Napier.e("Failed to authenticate: $errString")

                    cleanup(aliasOfSecureElementEntry)
                    continuation.resume(AuthResult.Error)
                }
            }
        )

        prompt.authenticate(promptInfo)

        continuation.invokeOnCancellation {
            prompt.cancelAuthentication()
            cleanup(aliasOfSecureElementEntry)
        }
    }

    fun cleanup(aliasOfSecureElementEntry: ByteArray) {
        try {
            KeyStore.getInstance("AndroidKeyStore")
                .apply { load(null) }
                .deleteEntry(Base64.toBase64String(aliasOfSecureElementEntry))
        } catch (e: KeyStoreException) {
            Napier.e("Couldn't remove key from keystore on failure; expected to happen.", e)
        }
    }
}

@Composable
fun rememberAltPairing(): AltPairingProvider {
    val activity = LocalContext.current as FragmentActivity
    val title = stringResource(R.string.alternate_auth_header)
    val description = stringResource(R.string.alternate_auth_info)
    val negativeButton = stringResource(R.string.cancel)
    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setNegativeButtonText(negativeButton)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            )
            .build()
    }
    return remember {
        AltPairingProvider(activity, promptInfo)
    }
}
