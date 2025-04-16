/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.redeem.usecase

import de.gematik.ti.erp.app.BCProvider
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.HttpErrorState
import de.gematik.ti.erp.app.api.httpErrorState
import de.gematik.ti.erp.app.fhir.communication.model.DirectCommunicationMessage
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.pharmacy.buildRecipientInfo
import de.gematik.ti.erp.app.pharmacy.filterByRSAPublicKey
import de.gematik.ti.erp.app.pharmacy.mapper.toPharmacyContact
import de.gematik.ti.erp.app.pharmacy.mapper.toRedeemOption
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.redeem.model.RedeemedPrescriptionState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.DERSet
import org.bouncycastle.asn1.cms.Attribute
import org.bouncycastle.asn1.cms.AttributeTable
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
import org.bouncycastle.util.encoders.Base64
import java.security.spec.MGF1ParameterSpec
import java.util.UUID
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

private val json = Json {
    encodeDefaults = true
    prettyPrint = false
}
private const val OidRecipientMail = "1.2.276.0.76.4.173" // komle-recipient-emails

/**
 * 1. Inform the UI on the process start
 * 2. Load the pharmacy certificates using the pharmacy id. This is required to encrypt the message
 * 3. Create the communication message in the FHIR Json format
 * 4. Encrypt the message using the pharmacy certificates and obtain the encrypted byte array
 * 5. Send the encrypted message to the pharmacy with the prescription information in an async manner
 * 6. Collect the results on the responses for all the prescriptions
 * 7. Save the communication to the database and the pharmacy as often used
 * 8. Inform the UI on the process end
 * 9. Combine the results together in [RedeemedPrescriptionState.OrderCompleted] and return the flow
 */
class RedeemPrescriptionsOnDirectUseCase(
    private val communicationRepository: CommunicationRepository,
    private val pharmacyRepository: PharmacyRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(
        orderId: UUID,
        redeemOption: PharmacyScreenData.OrderOption,
        prescriptionOrderInfos: List<PharmacyUseCaseData.PrescriptionInOrder>,
        contact: PharmacyUseCaseData.ShippingContact,
        pharmacy: PharmacyUseCaseData.Pharmacy,
        onRedeemProcessStart: () -> Unit = {},
        onRedeemProcessEnd: () -> Unit = {}
    ): Flow<RedeemedPrescriptionState.OrderCompleted> =
        flow {
            withContext(dispatcher) {
                onRedeemProcessStart()

                val certificates = loadCertificates(pharmacy.id).getOrThrow()

                prescriptionOrderInfos
                    .map { prescriptionOrderInfo ->
                        async {
                            prescriptionOrderInfo to pharmacyRepository.redeemPrescriptionDirectly(
                                url = redeemOption.toPharmacyContact(pharmacy),
                                message = convertJsonMessageToByteArray(
                                    message = createCommunication(
                                        orderId = orderId,
                                        prescription = prescriptionOrderInfo,
                                        contact = contact,
                                        redeemOption = redeemOption
                                    ),
                                    recipientCertificates = certificates
                                ),
                                pharmacyTelematikId = pharmacy.telematikId,
                                transactionId = orderId.toString()
                            )
                        }
                    }
                    .awaitAll()
                    .toMap()
                    .mapValues { (prescriptionOrderInfo, redeemResult) ->
                        redeemResult.fold(
                            onSuccess = {
                                onRedeemProcessEnd()
                                Napier.i { "Prescription (direct) ${prescriptionOrderInfo.title} redeemed successfully" }
                                try {
                                    // save the communication to the database
                                    communicationRepository.saveLocalCommunication(
                                        taskId = prescriptionOrderInfo.taskId,
                                        pharmacyId = pharmacy.id,
                                        transactionId = orderId.toString()
                                    )

                                    // save the pharmacy as often used when the prescription was redeemed successfully
                                    launch { pharmacyRepository.markPharmacyAsOftenUsed(pharmacy) }

                                    RedeemedPrescriptionState.Success
                                } catch (e: Throwable) {
                                    RedeemedPrescriptionState.Error(
                                        errorState = HttpErrorState.ErrorWithCause("Error on local save ${e.message}")
                                    )
                                }
                            },
                            onFailure = { error ->
                                Napier.e { "Error on prescription (direct) ${prescriptionOrderInfo.title} redemption ${error.stackTraceToString()}" }
                                onRedeemProcessEnd()
                                when (error) {
                                    is ApiCallException -> RedeemedPrescriptionState.Error(
                                        errorState = error.response.httpErrorState()
                                    )

                                    else -> RedeemedPrescriptionState.Error(
                                        errorState = HttpErrorState.ErrorWithCause("Error on local save ${error.message}")
                                    )
                                }
                            }
                        )
                    }
            }.also { emit(it) }
        }
            .map { RedeemedPrescriptionState.OrderCompleted(orderId = orderId.toString(), results = it) }
            .cancellable()

    private suspend fun loadCertificates(pharmacyId: String): Result<List<X509CertificateHolder>> =
        pharmacyRepository.searchBinaryCerts(locationId = pharmacyId).mapCatching { list ->
            list.map { base64Cert ->
                X509CertificateHolder(Base64.decode(base64Cert))
            }
        }

    private fun createCommunication(
        orderId: UUID,
        prescription: PharmacyUseCaseData.PrescriptionInOrder,
        contact: PharmacyUseCaseData.ShippingContact,
        redeemOption: PharmacyScreenData.OrderOption
    ): String {
        val communication = DirectCommunicationMessage(
            version = 2,
            supplyOptionsType = redeemOption.toRedeemOption().type,
            name = contact.name,
            address = listOf(contact.line1, contact.line2, contact.postalCode, contact.city),
            phone = contact.telephoneNumber,
            hint = contact.deliveryInformation,
            text = "",
            mail = contact.mail,
            transactionID = orderId.toString(),
            taskID = prescription.taskId,
            accessCode = prescription.accessCode
        )

        val jsonString = json.encodeToString(communication)
        return jsonString
    }

    @Requirement(
        "A_22778-01#3",
        "A_22779-01#3",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Build and encrypt direct redeem message with pharmacy`s certificate"
    )
    fun convertJsonMessageToByteArray(
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
}
