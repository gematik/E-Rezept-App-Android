/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.cardwall.model.nfc.card

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.BERTags
import org.bouncycastle.asn1.DLApplicationSpecific
import java.io.IOException

/**
 * Represent the Version2 information of HealthCard
 */
class HealthCardVersion2(
    /**
     * Information of C0 with version of filling instruction for version2
     */
    val fillingInstructionsVersion: ByteArray, // C0
    /**
     * Information of C1 with version of card object system
     */
    val objectSystemVersion: ByteArray, // C1
    /**
     * Information of C2 with version of product identification object system
     */
    val productIdentificationObjectSystemVersion: ByteArray, // C2
    /**
     * Information of C4 with version of filling instruction for EF.GDO
     */
    val fillingInstructionsEfGdoVersion: ByteArray, // C4
    /**
     * Information of C5 with version of filling instruction for EF.ATR
     */
    val fillingInstructionsEfAtrVersion: ByteArray, // C5
    /**
     * Information of C6 with version of filling instruction for EF.KeyInfo
     * Only filled for gSMC-K and gSMC-KT
     */
    val fillingInstructionsEfKeyInfoVersion: ByteArray, // C6  //only  gSMC-K and gSMC-KT
    /**
     * Information of C3 with version of filling instruction for Environment Settings
     * Only filled for gSMC-K
     */
    val fillingInstructionsEfEnvironmentSettingsVersion: ByteArray, // C3  //only  gSMC-K
    /**
     * Information of C7 with version of filling instruction for EF.GDO
     */
    val fillingInstructionsEfLoggingVersion: ByteArray // C7
) {
    companion object {
        private fun processData(data: ByteArray): Map<Int, ByteArray> =
            ASN1InputStream(data).use { decoder ->
                mutableMapOf<Int, ByteArray>().apply {
                    val app = decoder.readObject() as DLApplicationSpecific
                    if (app.isConstructed) {
                        val seq: ASN1Sequence = app.getObject(BERTags.SEQUENCE) as ASN1Sequence
                        seq.objects.iterator().forEach { obj ->
                            DLApplicationSpecific.getInstance(obj as ASN1Primitive).let {
                                this[it.applicationTag] = it.contents
                            }
                        }
                    }
                }
            }

        /**
         * Create and fill a new instance of Version2 Object with available data from card response data
         *
         * @param data
         * response data from card
         *
         * @return new instance of Version2
         *
         * @throws IOException
         */
        fun of(data: ByteArray) =
            processData(data).let {
                HealthCardVersion2(
                    fillingInstructionsVersion = it[0] ?: byteArrayOf(),
                    objectSystemVersion = it[1] ?: byteArrayOf(),
                    productIdentificationObjectSystemVersion = it[2] ?: byteArrayOf(),
                    fillingInstructionsEfEnvironmentSettingsVersion = it[3] ?: byteArrayOf(),
                    fillingInstructionsEfGdoVersion = it[4] ?: byteArrayOf(),
                    fillingInstructionsEfAtrVersion = it[5] ?: byteArrayOf(),
                    fillingInstructionsEfKeyInfoVersion = it[6] ?: byteArrayOf(),
                    fillingInstructionsEfLoggingVersion = it[7] ?: byteArrayOf()
                )
            }
    }
}

const val EGK21_MIN_VERSION = (4 shl 16) or (4 shl 8) or 0

fun HealthCardVersion2.isEGK21(): Boolean {
    val v = this.objectSystemVersion
    val version = (v[0].toInt() shl 16) or (v[1].toInt() shl 8) or v[1].toInt()

    return version >= EGK21_MIN_VERSION
}
