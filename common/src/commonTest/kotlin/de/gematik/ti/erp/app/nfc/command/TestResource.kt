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

@file:Suppress("ktlint:max-line-length", "ktlint:argument-list-wrapping")

package de.gematik.ti.erp.app.nfc.command

import de.gematik.ti.erp.app.card.model.card.CardKey
import de.gematik.ti.erp.app.card.model.identifier.ApplicationIdentifier
import de.gematik.ti.erp.app.card.model.identifier.FileIdentifier
import de.gematik.ti.erp.app.card.model.identifier.ShortFileIdentifier
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.util.Locale

@Suppress("ktlint:enum-entry-name-case")
enum class ParameterEnum {
    PARAMETER_INT_OFFSET, PARAMETER_PIN, PARAMETER_INT_NE, PARAMETER_INT_RECORDNUMBER, PARAMETER_INT_FCPLENGTH, PARAMETER_INT_GETCHALLENGE_LENGTH, PARAMETER_INT_GETRANDOM, PARAMETER_INT_CHANNELNUMBER, PARAMETER_SID, PARAMETER_FILEIDENTIFIER, PARAMETER_APPLICATIONIDENTIFIER, PARAMETER_INT_IDDOMAIN, PARAMETER_GEMCVC, PARAMETER_ECPUBLICKEY, PARAMETER_FINGERPRINT, // PARAMETER_BYTEARRAY_MSE,
    PARAMETER_BYTEARRAY_DEFAULT, PARAMETER_RSAPUBLICKEY, PARAMETER_BYTEARRAY_INTERNLAUTH, PARAMETER_BYTEARRAY_REFERENCE, PARAMETER_BYTEARRAY_EXTERNALAUTH, PARAMETER_BYTEARRAY_CMDDATA, PARAMETER_BYTEARRAY_OID, PARAMETER_STRING_PACEINFOP256r1, PARAMETER_STRING_PACEINFOP384r1, PARAMETER_STRING_PACEINFOP512r1, PARAMETER_BYTEARRAY_CAN, PARAMETER_BYTEARRAY_NONZEZ, PARAMETER_BYTEARRAY_PK1, PARAMETER_BYTEARRAY_PK1PICC, PARAMETER_BYTEARRAY_PK2, PARAMETER_BYTEARRAY_PK2VP, PARAMETER_BYTEARRAY_PK2PICC, PARAMETER_BYTEARRAY_MACPCD, PARAMETER_BYTEARRAY_MACPICC, PARAMETER_STRING_ECCURVE_PK1
}

enum class ApduResultEnum {
    ACTIVATECOMMAND_APDU, ACTIVATERECORDCOMMAND_APDU, WRITECOMMAND_APDU, VERITYCOMMAND_APDU, TERMINATEDFCOMMAND_APDU, TERMINATECOMMAND_APDU, TERMINATECARDUSAGECOMMAND_APDU, SETLOGICALEOFCOMMAND_APDU, SEARCHRECORDCOMMAND_APDU, READRECORDCOMMAND_APDU, READCOMMAND_APDU, PSOVERIFYDIGITALSIGNATURECOMMAND_APDU, PSOVERIFYCERTIFICATECOMMAND_APDU, PSOTRANSCIPHER_APDU, PSOENCIPHER_APDU, PSODECIPHER_APDU, PSOCOMPUTEDIGITALSIGNATURECOMMAND_APDU, PSOCOMPUTECRYPTOGRAPHICCHECKSUM_APDU, PSOVERIFYCRYPTPGRAPHICCHECKSUMCOMMAND_APDU, MANAGESECURITYENVIRONMENTCOMMAND_APDU, MANAGECHANNELCOMMAND_APDU, LOADAPPLICATIONCOMMAND_APDU, LISTPUBLICKEYCOMMAND_APDU, INTERNALAUTHENTICATECOMMAND_APDU, GETRANDOMCOMMAND_APDU, GETPINSTATUSCOMMAND_APDU, GETCHALLENGECOMMAND_APDU, GENERATEASYMMETRICKEYPAIRCOMMAND_APDU, GENERALAUTHENTICATECOMMAND_APDU, FINGERPRINTCOMMAND_APDU, EXTERNALMUTUALAUTHENTICATECOMMAND_APDU, ERASERECORDCOMMAND_APDU, ERASECOMMAND_APDU, ENABLEVERIFICATIONREQUIREMENTCOMMAND_APDU, DISABLEVERIFICATIONREQUIREMENTCOMMAND_APDU, DELETERECORDCOMMAND_APDU, DELETECOMMAND_APDU, DEACTIVATERECORDCOMMAND_APDU, DEACTIVATECOMMAND_APDU, CHANGEREFERENCEDATACOMMAND_APDU, APPENDRECORDCOMMAND_APDU, SELECTCOMMAND_APDU, UPDATERECORDCOMMAND_APDU, UPDATECOMMAND_APDU
}

private const val RESOURCE_PREFIX = "src/commonTest/resources/nfc"

class TestResource {
    private val expectedApdusYml: Map<String, Map<String, String>>
    private val testParameterYml: Map<String, String>

    init {
        val yaml = Yaml()

        expectedApdusYml = yaml.load(File("$RESOURCE_PREFIX/expectApdu.yml").inputStream())
        testParameterYml = yaml.load(
            File("$RESOURCE_PREFIX/testParameters.yml").inputStream()
        )
    }

    /**
     * testId is required (count of test > 1)
     */
    fun getExpectApdu(
        apduResultEnum: ApduResultEnum,
        testId: Int,
        vararg booleanValue: Boolean
    ): ByteArray {
        val map = expectedApdusYml[apduResultEnum.name + "-" + testId]!!
        var booleanStrValues = ""
        for (bool in booleanValue) {
            booleanStrValues = "$booleanStrValues-$bool"
        }
        return convertByteArray(requireNotNull(map["apdu$booleanStrValues"]))
    }

    /**
     * testId is not required (count of test = 1)
     */
    fun getExpectApduWithoutTestID(
        apduResultEnum: ApduResultEnum,
        vararg booleanValue: Boolean
    ): String? {
        val map = expectedApdusYml[apduResultEnum.name]!!
        var booleanStrValues = ""
        for (bool in booleanValue) {
            booleanStrValues = "$booleanStrValues-$bool"
        }
        return map["apdu$booleanStrValues"]
    }

    fun getParameter(parameterEnum: ParameterEnum): Any? {
        val ymlValue = testParameterYml[parameterEnum.name]!!
        if (parameterEnum.name.startsWith("PARAMETER_BYTEARRAY")) {
            return convertByteArray(ymlValue)
        }
        if (parameterEnum.name.startsWith("PARAMETER_INT")) {
            return ymlValue.toInt()
        }
        return when (parameterEnum) {
            ParameterEnum.PARAMETER_FILEIDENTIFIER -> FileIdentifier(ymlValue)
            ParameterEnum.PARAMETER_APPLICATIONIDENTIFIER -> ApplicationIdentifier(ymlValue)
            ParameterEnum.PARAMETER_FINGERPRINT -> {
                val byteArray = ByteArray(128)
                var i = 0
                while (i < 128) {
                    byteArray[i] = i.toByte()
                    i++
                }

                byteArray
            }
            ParameterEnum.PARAMETER_SID -> ShortFileIdentifier(ymlValue)
            else -> ymlValue
        }
    }

    companion object {
        private const val ID_PIN_CH = 1
        private const val ID_PRK_EGK_AUT_CVC_E256 = 9
        // val PASSWD: Password = Password(ID_PIN_CH)
        // val PIN: Format2Pin = Format2Pin(intArrayOf(1, 2, 3, 4, 5, 6))

        val KEY_PRK_EGK_AUT_CVC_E256 = CardKey(ID_PRK_EGK_AUT_CVC_E256)

        /**
         * Convert a Hex String in a Byte Array. Spaces will be removed.
         *
         * @param hexString
         * @return
         */
        fun convertByteArray(hexString: String): ByteArray {
            val hex = formatHexString(hexString, false)
            val result = ByteArray(hex!!.length / 2)
            val enc = hex.toCharArray()
            var i = 0
            while (i < enc.size) {
                val curr = StringBuilder(2)
                curr.append(enc[i]).append(enc[i + 1])
                result[i / 2] = curr.toString().toInt(16).toByte()
                i += 2
            }
            return result
        }

        /**
         * Format a hexadecimal String. <br></br>
         * String 'd3f246' will be formatted to 'D3 F2 46'<br></br>
         * String '13aB5' will be formatted to '01 3A B5'
         *
         * @param hexString
         * @param insertSpaces
         * @return String
         */
        fun formatHexString(hexString: String?, insertSpaces: Boolean): String? {
            if (hexString == null) {
                return null
            }
            var hex = hexString.replace(" ", "")
            hex = hex.uppercase(Locale.getDefault())

            // Correct a odd length if any
            if (hex.length % 2 != 0) {
                hex = "0$hex"
            }
            if (insertSpaces) {
                val sb = StringBuffer()
                for (i in 0 until hex.length) {
                    sb.append(hex[i])
                    if (i % 2 == 1 && i != hex.length - 1) {
                        sb.append(" ")
                    }
                }
                hex = sb.toString()
            }
            return hex
        }
    }
}
