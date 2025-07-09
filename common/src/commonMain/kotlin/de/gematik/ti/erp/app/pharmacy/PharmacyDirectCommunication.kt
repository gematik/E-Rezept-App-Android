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

package de.gematik.ti.erp.app.pharmacy

import de.gematik.ti.erp.app.BCProvider
import de.gematik.ti.erp.app.Requirement
import io.github.aakira.napier.Napier
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1PrintableString
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERSet
import org.bouncycastle.asn1.cms.Attribute
import org.bouncycastle.asn1.cms.AttributeTable
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber
import org.bouncycastle.asn1.cms.RecipientIdentifier
import org.bouncycastle.asn1.isismtt.ISISMTTObjectIdentifiers
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cms.CMSAlgorithm
import org.bouncycastle.cms.CMSAuthEnvelopedDataGenerator
import org.bouncycastle.cms.CMSProcessableByteArray
import org.bouncycastle.cms.SimpleAttributeTableGenerator
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator
import org.bouncycastle.operator.OutputAEADEncryptor
import org.bouncycastle.operator.jcajce.JceAsymmetricKeyWrapper
import java.security.spec.MGF1ParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

const val OidRecipientMail = "1.2.276.0.76.4.173" // komle-recipient-emails
@Requirement(
    "A_22778-01#3",
    "A_22779-01#3",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Build and encrypt direct redeem message with pharmacy`s certificate"
)
fun buildDirectPharmacyMessage(
    message: String,
    recipientCertificates: List<X509CertificateHolder>
): ByteArray {
    require(recipientCertificates.isNotEmpty()) { "No recipients specified!" }

    val msg = CMSProcessableByteArray(message.toByteArray())

    val edGen = CMSAuthEnvelopedDataGenerator()

    val info = buildRecipientInfo(recipientCertificates)

    edGen.setUnauthenticatedAttributeGenerator(
        SimpleAttributeTableGenerator(
            AttributeTable(
                Attribute(
                    ASN1ObjectIdentifier(OidRecipientMail),
                    DERSet(info)
                )
            )
        )
    )

    val jcaConverter = JcaX509CertificateConverter().apply {
        setProvider(BCProvider)
    }
    @Requirement(
        "GS-A_4389#3",
        "GS-A_4390#1",
        sourceSpecification = "gemSpec_Krypt",
        rationale = "Build and encrypt direct redeem message with pharmacy`s certificate"
    )
    recipientCertificates
        .filterByRSAPublicKey()
        .forEach { recipientCert ->
            val jcaCert = jcaConverter.getCertificate(recipientCert)

            edGen.addRecipientInfoGenerator(
                JceKeyTransRecipientInfoGenerator(
                    jcaCert,
                    JceAsymmetricKeyWrapper(
                        OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT),
                        jcaCert.publicKey
                    )
                ).setProvider(BCProvider)
            )
        }

    val contentEncryptor = JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_GCM)
        .setProvider(BCProvider)
        .build()

    val ed = edGen.generate(msg, contentEncryptor as OutputAEADEncryptor)

    return ed.toASN1Structure().encoded
}

fun buildRecipientInfo(recipientCertificates: List<X509CertificateHolder>) =
    ASN1EncodableVector().apply {
        recipientCertificates.forEach { recipientCert ->
            add(
                DERSequence(
                    ASN1EncodableVector().apply {
                        val telematikId = requireNotNull(recipientCert.extractTelematikId()) {
                            "Telematik ID not found!"
                        }

                        add(DERIA5String(telematikId, true))
                        add(RecipientIdentifier(IssuerAndSerialNumber(recipientCert.toASN1Structure())))
                    }
                )
            )
        }
    }

fun X509CertificateHolder.extractTelematikId(): String? =
    try {
        this
            .getExtension(ISISMTTObjectIdentifiers.id_isismtt_at_admission)
            .parsedValue.let { it as ASN1Sequence } // AdmissionSyntax
            .find { it is ASN1Sequence }.let { it as ASN1Sequence } // contentsOfAdmissions
            .getObjectAt(0).let { it as ASN1Sequence } // first one
            .find { it is ASN1Sequence }.let { it as ASN1Sequence } // professionInfos
            .getObjectAt(0).let { it as ASN1Sequence } // first one
            .find { it is ASN1PrintableString } // registrationNumber
            .let { it as ASN1PrintableString }.string
    } catch (ignored: Exception) {
        Napier.w("Telematik ID could not be extracted", ignored)
        null
    }

fun List<X509CertificateHolder>.filterByRSAPublicKey() =
    this.filter { recipientCert ->
        recipientCert.subjectPublicKeyInfo.algorithm.algorithm == PKCSObjectIdentifiers.rsaEncryption
    }
