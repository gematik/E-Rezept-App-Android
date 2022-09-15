/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.attestation

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.safetynet.SafetyNetApi
import com.google.android.gms.safetynet.SafetyNetClient
import dagger.hilt.android.qualifiers.ApplicationContext
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.attestation.AttestationException.AttestationExceptionType
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

const val PLAY_SERVICES_VERSION = 13000000

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
                .isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS
            ) {
                cont.resumeWithException(
                    Exception("Google Play Services not available.")
                )
            } else if (GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context, PLAY_SERVICES_VERSION) ==
                ConnectionResult.SUCCESS
            ) {
                safetyNetClient.attest(nonce, BuildKonfig.SAFETYNET_API_KEY)
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
