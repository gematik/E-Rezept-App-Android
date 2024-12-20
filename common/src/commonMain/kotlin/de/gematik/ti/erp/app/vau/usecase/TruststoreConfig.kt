/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.vau.usecase

import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.Requirement
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.util.encoders.Base64
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@Requirement(
    "A_20161-01#6",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "Create trust store config."
)
@Requirement(
    "A_21218#6",
    sourceSpecification = "gemSpec_Krypt",
    rationale = "Gematik Root CA 3 as trust anchor has to be setup in the program code."
)
class TruststoreConfig(getTrustAnchor: () -> String) {
    val maxOCSPResponseAge: Duration by lazy {
        BuildKonfig.VAU_OCSP_RESPONSE_MAX_AGE.hours
    }

    val trustAnchor by lazy {
        X509CertificateHolder(Base64.decode(getTrustAnchor()))
    }
}
