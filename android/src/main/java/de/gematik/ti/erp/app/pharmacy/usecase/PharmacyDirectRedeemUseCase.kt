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

package de.gematik.ti.erp.app.pharmacy.usecase

import de.gematik.ti.erp.app.pharmacy.buildDirectPharmacyMessage
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import org.bouncycastle.cert.X509CertificateHolder
import java.util.UUID

class PharmacyDirectRedeemUseCase(
    private val repository: PharmacyRepository
) {

    suspend fun redeemPrescription(
        url: String,
        message: String,
        telematikId: String,
        recipientCertificates: List<X509CertificateHolder>,
        transactionId: String = UUID.randomUUID().toString()
    ): Result<Unit> =
        runCatching {
            val asn1Message = buildDirectPharmacyMessage(
                message = message,
                recipientCertificates = recipientCertificates
            )

            repository.redeemPrescription(
                url = url,
                message = asn1Message,
                pharmacyTelematikId = telematikId,
                transactionId = transactionId
            ).getOrThrow()
        }
}
