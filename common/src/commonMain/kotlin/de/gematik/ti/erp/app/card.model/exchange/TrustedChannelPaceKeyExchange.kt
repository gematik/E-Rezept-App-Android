/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.cardwall.model.nfc.exchange

import de.gematik.ti.erp.app.card.model.CardUtilities.byteArrayToECPoint
import de.gematik.ti.erp.app.card.model.CardUtilities.extractKeyObjectEncoded
import de.gematik.ti.erp.app.card.model.card.CardKey
import de.gematik.ti.erp.app.card.model.card.HealthCardVersion2
import de.gematik.ti.erp.app.card.model.card.ICardChannel
import de.gematik.ti.erp.app.card.model.card.PaceKey
import de.gematik.ti.erp.app.card.model.card.isEGK21
import de.gematik.ti.erp.app.card.model.cardobjects.Ef
import de.gematik.ti.erp.app.card.model.command.HealthCardCommand
import de.gematik.ti.erp.app.card.model.command.executeSuccessfulOn
import de.gematik.ti.erp.app.card.model.command.generalAuthenticate
import de.gematik.ti.erp.app.card.model.command.manageSecEnvWithoutCurves
import de.gematik.ti.erp.app.card.model.command.read
import de.gematik.ti.erp.app.card.model.command.select
import de.gematik.ti.erp.app.card.model.exchange.KeyDerivationFunction
import de.gematik.ti.erp.app.card.model.exchange.KeyDerivationFunction.getAES128Key
import de.gematik.ti.erp.app.card.model.exchange.PaceInfo
import de.gematik.ti.erp.app.card.model.identifier.FileIdentifier
import de.gematik.ti.erp.app.card.model.identifier.ShortFileIdentifier
import de.gematik.ti.erp.app.secureRandomInstance
import de.gematik.ti.erp.app.utils.Bytes
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.BERTags
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.macs.CMac
import org.bouncycastle.crypto.params.KeyParameter
import java.math.BigInteger

private const val SECRET_KEY_REFERENCE = 2 // Reference of secret key for PACE (CAN)
private const val AES_BLOCK_SIZE = 16
private const val BYTE_LENGTH = 8
private const val MAX = 64
private const val TAG_6 = 6
private const val TAG_49 = 0x49

/**
 * Opens a secure PACE Channel for secure messaging
 *
 * picc = card
 * pcd = smartphone
 */
suspend fun ICardChannel.establishTrustedChannel(cardAccessNumber: String): PaceKey {
    val randomGenerator = secureRandomInstance()

    suspend fun step0ReadSupportedPaceParameters(step1: suspend (paceInfo: PaceInfo) -> PaceKey): PaceKey {
        HealthCardCommand.select(selectParentElseRoot = false, readFirst = true).executeSuccessfulOn(
            this
        )

        HealthCardCommand.read(ShortFileIdentifier(Ef.Version2.SFID), 0).executeSuccessfulOn(this).let {
            check(HealthCardVersion2.of(it.apdu.data).isEGK21()) { "Invalid eGK Version." }
        }

        HealthCardCommand.select(FileIdentifier(Ef.CardAccess.FID), false)
            .executeSuccessfulOn(this)

        val paceInfo = PaceInfo(HealthCardCommand.read().executeOn(this).apdu.data)

        HealthCardCommand.manageSecEnvWithoutCurves(
            CardKey(SECRET_KEY_REFERENCE),
            false,
            paceInfo.paceInfoProtocolBytes
        ).executeSuccessfulOn(this)

        return step1(paceInfo)
    }

    suspend fun step1EphemeralPublicKeyFirst(
        paceInfo: PaceInfo,
        step2: suspend (
            paceInfo: PaceInfo,
            nonceSInt: BigInteger,
            pcdSkX1: BigInteger,
            pcdPk1: ByteArray
        ) -> PaceKey
    ): PaceKey {
        val nonceZBytes = HealthCardCommand.generalAuthenticate(true).executeSuccessfulOn(this).apdu.data
        val nonceZBytesEncoded = extractKeyObjectEncoded(nonceZBytes)
        val canBytes = cardAccessNumber.toByteArray()
        val aes128Key = getAES128Key(canBytes, KeyDerivationFunction.Mode.PASSWORD)
        val encKey = KeyParameter(aes128Key)

        val nonceS = ByteArray(AES_BLOCK_SIZE)
        AESEngine().apply {
            init(false, encKey)
            processBlock(nonceZBytesEncoded, 0, nonceS, 0)
        }
        val nonceSInt = BigInteger(1, nonceS)

        val pk1Pcd = ByteArray(paceInfo.ecCurve.fieldSize / BYTE_LENGTH)
        randomGenerator.nextBytes(pk1Pcd)

        val pcdSkX1 = BigInteger(1, pk1Pcd)
        val pcdPkSkX1 = paceInfo.ecPointG.multiply(pcdSkX1)

        return step2(paceInfo, nonceSInt, pcdSkX1, pcdPkSkX1.getEncoded(false))
    }

    suspend fun step2EphemeralPublicKeySecond(
        paceInfo: PaceInfo,
        nonceSInt: BigInteger,
        pcdSkX1: BigInteger,
        pcdPk1: ByteArray,
        step3: suspend (
            paceInfo: PaceInfo,
            pcdSkX2: BigInteger,
            pcdPkS2: ByteArray
        ) -> PaceKey
    ): PaceKey {
        val piccPk1Bytes =
            HealthCardCommand.generalAuthenticate(true, pcdPk1, 1).executeSuccessfulOn(this).apdu.data

        val piccPk1BytesEncoded = extractKeyObjectEncoded(piccPk1Bytes)
        val y1 = byteArrayToECPoint(piccPk1BytesEncoded, paceInfo.ecCurve)
        val x2 = ByteArray(paceInfo.ecCurve.fieldSize / BYTE_LENGTH)
        randomGenerator.nextBytes(x2)

        val sharedSecretP = y1.multiply(pcdSkX1)
        val pointGS = paceInfo.ecPointG.multiply(nonceSInt).add(sharedSecretP)

        val pcdSkX2 = BigInteger(1, x2)
        val pcdPkS2 = pointGS.multiply(pcdSkX2)

        return step3(paceInfo, pcdSkX2, pcdPkS2.getEncoded(false))
    }

    suspend fun step3MutualAuthentication(
        paceInfo: PaceInfo,
        pcdSkX2: BigInteger,
        pcdPkS2: ByteArray,
        step4: suspend (
            piccMacDerived: ByteArray,
            pcdMac: ByteArray
        ) -> Boolean
    ): PaceKey {
        val piccPk2Bytes =
            HealthCardCommand.generalAuthenticate(true, pcdPkS2, 3).executeSuccessfulOn(this).apdu.data

        val piccPk2 = extractKeyObjectEncoded(piccPk2Bytes)

        val piccPk2ECPoint = byteArrayToECPoint(piccPk2, paceInfo.ecCurve)
        val sharedSecretK = piccPk2ECPoint.multiply(pcdSkX2)

        val sharedSecretKBytes: ByteArray =
            Bytes.bigIntToByteArray(sharedSecretK.normalize().xCoord.toBigInteger())

        val paceKey = PaceKey(
            getAES128Key(sharedSecretKBytes, KeyDerivationFunction.Mode.ENC),
            getAES128Key(sharedSecretKBytes, KeyDerivationFunction.Mode.MAC)
        )

        val pcdMac = deriveMac(paceKey.mac, piccPk2, paceInfo.protocolID)
        val piccMacDerived = deriveMac(paceKey.mac, pcdPkS2, paceInfo.protocolID)

        require(step4(piccMacDerived, pcdMac))

        return paceKey
    }

    fun step4VerifyPcdAndPiccMac(
        piccMacDerived: ByteArray,
        pcdMac: ByteArray
    ): Boolean {
        val piccMacBytes =
            HealthCardCommand.generalAuthenticate(false, pcdMac, 5)
                .executeSuccessfulOn(this).apdu.data

        val piccMac = extractKeyObjectEncoded(piccMacBytes)

        return piccMac.contentEquals(piccMacDerived)
    }

    /**
     * Negotiate the PaceKey and return the object
     */
    return step0ReadSupportedPaceParameters { paceInfo ->
        step1EphemeralPublicKeyFirst(paceInfo) { _, nonceSInt, pcdSkX1, pcdPk1 ->
            step2EphemeralPublicKeySecond(paceInfo, nonceSInt, pcdSkX1, pcdPk1) { _, pcdSkX2, pcdPkS2 ->
                step3MutualAuthentication(paceInfo, pcdSkX2, pcdPkS2) { piccMacDerived, pcdMac ->
                    step4VerifyPcdAndPiccMac(piccMacDerived, pcdMac)
                }
            }
        }
    }
}

private fun createAsn1AuthToken(ecPoint: ByteArray, protocolID: String): ByteArray {
    val asn1EncodableVector = ASN1EncodableVector()
    asn1EncodableVector.add(ASN1ObjectIdentifier(protocolID))
    asn1EncodableVector.add(
        DERTaggedObject(
            false,
            TAG_6,
            DEROctetString(ecPoint)
        )
    )
    return DERTaggedObject(false, BERTags.APPLICATION, TAG_49, DERSequence(asn1EncodableVector)).encoded
}

private fun deriveMac(mac: ByteArray, publicKey: ByteArray, protocolID: String): ByteArray =
    CMac(AESEngine(), MAX).apply {
        init(KeyParameter(mac))

        val authToken = createAsn1AuthToken(publicKey, protocolID)
        update(authToken, 0, authToken.size)
    }.let {
        ByteArray(it.macSize).apply {
            it.doFinal(this, 0)
        }
    }
