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

package de.gematik.ti.erp.app.idp.extension

import org.bouncycastle.asn1.x500.X500Name
import java.security.cert.X509Certificate
import javax.security.auth.x500.X500Principal

/**
 * Extracts the Common Name (CN) from an X.500 Principal.
 *
 * ## What is a Common Name?
 * The CN is like the "official name" in a certificate. For example, in a certificate
 * for "google.com", the CN would be "google.com". It identifies who or what the
 * certificate belongs to.
 *
 * @return The Common Name, or empty string if not found
 */
fun X500Principal.commonName(): String =
    name
        .split(",")
        .map { it.trim() }
        .firstOrNull { it.startsWith("CN=") }
        ?.removePrefix("CN=")
        ?.substringBefore('\n')
        ?.trim()
        .orEmpty()

/**
 * Extracts the issuer's Common Name from an X509 certificate.
 *
 * ## What is an Issuer?
 * The issuer is the Certificate Authority (CA) that created and signed the certificate.
 * Think of it as the organization that vouches for the certificate's authenticity.
 *
 * @return The issuer's Common Name
 * @throws IllegalArgumentException if CN cannot be determined
 */
fun X509Certificate.issuerCommonName(): String {
    val cn = issuerX500Principal.commonName()
    require(cn.isNotEmpty()) {
        "Issuer CN could not be determined from idp certificate"
    }
    return cn
}

fun X500Name.issuerCommonName(): String {
    val relativeDistinguishedNames = this.getRDNs(org.bouncycastle.asn1.x500.style.BCStyle.CN)
    val commonName = relativeDistinguishedNames.firstOrNull()?.first?.value?.toString()?.trim().orEmpty()
    require(commonName.isNotEmpty()) {
        "Issuer CN could not be determined from idp certificate"
    }
    return commonName
}
