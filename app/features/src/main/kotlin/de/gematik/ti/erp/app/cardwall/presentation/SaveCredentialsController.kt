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

package de.gematik.ti.erp.app.cardwall.presentation

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.authentication.model.BiometricMethod
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.secureRandomInstance
import de.gematik.ti.erp.app.userauthentication.observer.BiometricPromptBuilder
import io.github.aakira.napier.Napier
import kotlinx.coroutines.suspendCancellableCoroutine
import org.bouncycastle.util.encoders.Base64
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PublicKey
import java.security.spec.ECGenParameterSpec
import kotlin.coroutines.resume

private const val KeyStoreAliasKeySize = 32 // in bytes
private const val KeyTimeout = 15 * 60 // in seconds

@Stable
class SaveCredentialsController(
    private val biometricPromptBuilder: BiometricPromptBuilder,
    private val promptInfo: BiometricPrompt.PromptInfo
) {
    sealed interface AuthResult {
        data object Error : AuthResult
        data object Authenticated : AuthResult

        @Stable
        class Initialized(val aliasOfSecureElementEntry: ByteArray, val publicKey: PublicKey) : AuthResult
    }

    val aliasToByteArray: (ByteArray) -> String = { Base64.toBase64String(it) }

    @Requirement(
        "A_21585#1",
        "A_21590#1",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Initialize biometric authentication for strongbox backed devices."
    )
    @RequiresApi(Build.VERSION_CODES.P)
    suspend fun initializeAndPrompt(useStrongBox: Boolean): AuthResult = suspendCancellableCoroutine { continuation ->
        val aliasOfSecureElementEntry = ByteArray(KeyStoreAliasKeySize).apply {
            secureRandomInstance().nextBytes(this)
        }

        // ensures a clean keystore state
        deleteKey(aliasToByteArray(aliasOfSecureElementEntry))

        try {
            val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")

            @Requirement(
                "O.Auth_5#1",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "Invalidate when change is registered.",
                codeLines = 10
            )
            val parameterSpec = KeyGenParameterSpec.Builder(
                Base64.toBase64String(aliasOfSecureElementEntry),
                KeyProperties.PURPOSE_SIGN
            ).apply {
                setInvalidatedByBiometricEnrollment(true)
                setUserAuthenticationRequired(true)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // While the documentation of Android suggests to set this to zero, this is safe to use.
                    // If the key is used and later, e.g. a fingerprint is added, the keystore implementation of
                    // Android will throw a `KeyPermanentlyInvalidatedException`. Later on if the user restarts
                    // the phone, the key is permanently invalidated and the actual `UserNotAuthenticatedException`
                    // is thrown.
                    setUserAuthenticationParameters(
                        KeyTimeout,
                        KeyProperties.AUTH_DEVICE_CREDENTIAL or KeyProperties.AUTH_BIOMETRIC_STRONG
                    )
                } else {
                    // needed for Huawei and Android devices < R
                    setUserAuthenticationValidityDurationSeconds(KeyTimeout)
                }

                if (useStrongBox) {
                    try {
                        setIsStrongBoxBacked(true)
                    } catch (e: Exception) {
                        Napier.e("StrongBox is not available")
                        setIsStrongBoxBacked(false)
                    }
                }

                setDigests(KeyProperties.DIGEST_SHA256)
                setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            }.build()

            keyPairGenerator.initialize(parameterSpec)
            val keyPair = keyPairGenerator.generateKeyPair()
            val publicKey = keyPair.public // required to init

            Napier.d { "KeyPair generated successfully!" }

            val prompt = biometricPromptBuilder.buildBiometricPrompt(
                onSuccess = {
                    continuation.resume(
                        AuthResult.Initialized(
                            aliasOfSecureElementEntry = aliasOfSecureElementEntry,
                            publicKey = publicKey
                        )
                    )
                },
                onError = { _: String, _: Int ->
                    deleteKey(aliasToByteArray(aliasOfSecureElementEntry))
                    continuation.resume(AuthResult.Error)
                }
            )
            prompt.authenticate(promptInfo)
            continuation.invokeOnCancellation {
                prompt.cancelAuthentication()
                deleteKey(aliasToByteArray(aliasOfSecureElementEntry))
            }
        } catch (e: Exception) {
            Napier.e("Key generation failed", e)
            deleteKey(aliasToByteArray(aliasOfSecureElementEntry))
            continuation.resume(AuthResult.Error)
        }
    }

    fun deleteKey(alias: String) {
        try {
            KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
                if (containsAlias(alias)) {
                    deleteEntry(alias)
                }
            }
        } catch (e: Exception) {
            Napier.e("Error deleting old key", e)
        }
    }
}

@Composable
fun rememberSaveCredentialsScreenController(): SaveCredentialsController {
    val context = LocalContext.current
    val activity = context as BaseActivity
    val biometricPromptBuilder = remember { BiometricPromptBuilder(activity) }

    val title = stringResource(R.string.auth_prompt_headline)
    val description = stringResource(R.string.alternate_auth_info)
    val negativeButton = stringResource(R.string.auth_prompt_cancel)

    // Track the current method type (Strong, Weak, Device)
    val biometricMethod = remember { mutableStateOf(BiometricMethod.None) }

    LaunchedEffect(Unit) {
        activity.biometricStateChangedFlow.collect {
            Napier.i(tag = "Biometric") { "Biometric state changed, refreshing authenticator: $it" }
            biometricMethod.value = it
        }
    }

    // PromptInfo is rebuilt on method change
    val promptInfo = remember(biometricMethod.value) {
        biometricPromptBuilder.buildPromptInfoDynamically(
            title = title,
            description = description,
            negativeButton = negativeButton,
            method = biometricMethod.value // pass enum
        )
    }

    return remember {
        SaveCredentialsController(biometricPromptBuilder, promptInfo)
    }
}
