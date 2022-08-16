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

@file:UseSerializers(OCSPSerializer::class, X509Serializer::class)

package de.gematik.ti.erp.app.vau.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.ocsp.OCSPResp

/**
 * Reflects a json array with the following structure:
 *
 * {
 *   "add_roots" : [ "base64-kodiertes-Root-Cross-Zertifikat-1", ... ],
 *   "ca_certs" : [ "base64-kodiertes-Komponenten-CA-Zertifikat-1", ... ],
 *   "ee_certs" : [ "base64-kodiertes-EE-Zertifikat-1-aus-einer-Komponenten-CA", ... ]
 * }
 *
 * Refer to gemSpec_Krypt `Tab_KRYPT_ERP_Zertifikatsliste`
 */
@Serializable
data class UntrustedCertList(
    // additional cross roots
    @SerialName("add_roots")
    val addRoots: List<X509CertificateHolder>,

    // ca certs
    @SerialName("ca_certs")
    val caCerts: List<X509CertificateHolder>,

    // vau & idp certs
    @SerialName("ee_certs")
    val eeCerts: List<X509CertificateHolder>
)

/**
 * OCSP list:
 *
 * {
 *   "OCSP Responses": [ "base64 encoded ocsp response", ... ]
 * }
 *
 */
@Serializable
data class UntrustedOCSPList(
    @SerialName("OCSP Responses")
    val responses: List<OCSPResp>
)
