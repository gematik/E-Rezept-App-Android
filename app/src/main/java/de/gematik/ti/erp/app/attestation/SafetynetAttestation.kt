package de.gematik.ti.erp.app.attestation

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.safetynet.SafetyNetApi
import com.google.android.gms.safetynet.SafetyNetClient
import dagger.hilt.android.qualifiers.ApplicationContext
import de.gematik.ti.erp.app.BuildConfig
import de.gematik.ti.erp.app.attestation.AttestationException.AttestationExceptionType
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class SafetynetAttestation @Inject constructor(
    @ApplicationContext val context: Context,
    private val safetyNetClient: SafetyNetClient
) : Attestation {

    override suspend fun attest(request: Attestation.Request): Attestation.Result {
        val safetynetResult = callSafetynet(request.nonce)
        return unpackResult(safetynetResult)
    }

    private suspend fun callSafetynet(nonce: ByteArray): SafetyNetApi.AttestationResponse =
        suspendCancellableCoroutine { cont ->
            if (GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context, 13000000) ==
                ConnectionResult.SUCCESS
            ) {
                safetyNetClient.attest(nonce, BuildConfig.SAFETYNET_API_KEY)
                    .addOnSuccessListener {
                        cont.resume(it)
                    }
                    .addOnFailureListener { e ->
                        cont.resumeWithException(e)
                    }
            } else {
                cont.resumeWithException(
                    AttestationException(
                        AttestationExceptionType.PLAY_SERVICES_VERSION_MISMATCH,
                        "Google Play Services too old."
                    )
                )
            }
        }

    private fun unpackResult(
        response: SafetyNetApi.AttestationResponse
    ): SafetynetResult {
        val jws = response.jwsResult ?: throw AttestationException(
            AttestationExceptionType.ATTESTATION_FAILED,
            "JWS was null"
        )
        return SafetynetResult(jws)
    }
}
