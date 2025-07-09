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

package de.gematik.ti.erp.app.pharmacy.usecase

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.pharmacy.buildDirectPharmacyMessage
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import org.bouncycastle.cert.X509CertificateHolder

// TODO: Remove when the redemption in debug buttons is using the correct ones
class PharmacyDirectRedeemUseCase(
    private val repository: PharmacyRepository
) {
    @Requirement(
        "A_22778-01#2",
        "A_22779-01#2",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Start Redeem without TI (useCase)."
    )
    suspend fun redeemPrescriptionDirectly(
        url: String,
        message: String,
        telematikId: String,
        recipientCertificates: List<X509CertificateHolder>,
        transactionId: String
    ): Result<Unit> =
        runCatching {
            val asn1Message = buildDirectPharmacyMessage(
                message = message,
                recipientCertificates = recipientCertificates
            )

            repository.redeemPrescriptionDirectly(
                url = url,
                message = asn1Message,
                pharmacyTelematikId = telematikId,
                transactionId = transactionId
            ).getOrThrow()
        }
}
