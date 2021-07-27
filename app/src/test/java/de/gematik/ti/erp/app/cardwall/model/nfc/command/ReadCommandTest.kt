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

package de.gematik.ti.erp.app.cardwall.model.nfc.command

import de.gematik.ti.erp.app.cardwall.model.nfc.identifier.ShortFileIdentifier
import org.junit.Assert
import org.junit.Test

class ReadCommandTest {
    private val testResource = TestResource()

    @Test
    fun shouldEqualReadCommand() {
        val expectedAPDU = testResource.getExpectApdu(ApduResultEnum.READCOMMAND_APDU, 1)
        val command = HealthCardCommand.read()

        Assert.assertArrayEquals(expectedAPDU, TestChannel().test(command))
    }

    @Test
    fun shouldEqualReadCommandOffsetNe() {
        val expectedAPDU = testResource.getExpectApdu(ApduResultEnum.READCOMMAND_APDU, 2)
        val offset = testResource.getParameter(ParameterEnum.PARAMETER_INT_OFFSET) as Int
        val ne = testResource.getParameter(ParameterEnum.PARAMETER_INT_NE) as Int
        val command = HealthCardCommand.read(offset, ne)

        Assert.assertArrayEquals(expectedAPDU, TestChannel().test(command))
    }

    @Test
    fun shouldEqualReadCommandOffset() {
        val expectedAPDU = testResource.getExpectApdu(ApduResultEnum.READCOMMAND_APDU, 3)
        val offset = testResource.getParameter(ParameterEnum.PARAMETER_INT_OFFSET) as Int
        val command = HealthCardCommand.read(offset)

        Assert.assertArrayEquals(expectedAPDU, TestChannel().test(command))
    }

    @Test
    fun shouldEqualReadCommandShortFileIdentifier() {
        val expectedAPDU = testResource.getExpectApdu(ApduResultEnum.READCOMMAND_APDU, 4)
        val sfi = testResource.getParameter(ParameterEnum.PARAMETER_SID) as ShortFileIdentifier
        val command = HealthCardCommand.read(sfi)

        Assert.assertArrayEquals(expectedAPDU, TestChannel().test(command))
    }

    @Test
    fun shouldEqualReadCommandShortFileIdentifierOffset() {
        val expectedAPDU = testResource.getExpectApdu(ApduResultEnum.READCOMMAND_APDU, 5)
        val sfi = testResource.getParameter(ParameterEnum.PARAMETER_SID) as ShortFileIdentifier
        val offset = testResource.getParameter(ParameterEnum.PARAMETER_INT_OFFSET) as Int
        val command = HealthCardCommand.read(sfi, offset)

        Assert.assertArrayEquals(expectedAPDU, TestChannel().test(command))
    }

    @Test
    fun shouldEqualReadCommandShortFileIdentifierOffsetNe() {
        val expectedAPDU = testResource.getExpectApdu(ApduResultEnum.READCOMMAND_APDU, 6)
        val sfi = testResource.getParameter(ParameterEnum.PARAMETER_SID) as ShortFileIdentifier
        val offset = testResource.getParameter(ParameterEnum.PARAMETER_INT_OFFSET) as Int
        val ne = testResource.getParameter(ParameterEnum.PARAMETER_INT_NE) as Int
        val command = HealthCardCommand.read(sfi, offset, ne)

        Assert.assertArrayEquals(expectedAPDU, TestChannel().test(command))
    }
}
