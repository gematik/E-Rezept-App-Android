/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.nfc.command

import de.gematik.ti.erp.app.card.model.command.HealthCardCommand
import de.gematik.ti.erp.app.card.model.command.select
import de.gematik.ti.erp.app.card.model.identifier.ApplicationIdentifier
import de.gematik.ti.erp.app.card.model.identifier.FileIdentifier
import org.junit.Assert
import kotlin.test.Test
import org.junit.experimental.theories.DataPoint
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.runner.RunWith

@RunWith(Theories::class)
class SelectCommandTest {
    private val testResource = TestResource()

    @Theory
    fun shouldEqualSelectCommandSelectParentElseRoot(
        selectParentElseRoot: Boolean,
        readFirst: Boolean
    ) {
        val expectedAPDU = testResource.getExpectApdu(
            ApduResultEnum.SELECTCOMMAND_APDU,
            1,
            selectParentElseRoot,
            readFirst
        )
        val command = HealthCardCommand.select(selectParentElseRoot, readFirst)

        Assert.assertArrayEquals(expectedAPDU, TestChannel().test(command))
    }

    @Test
    fun shouldEqualSelectCommandApplicationIdentifier() {
        val expectedAPDU = testResource.getExpectApdu(ApduResultEnum.SELECTCOMMAND_APDU, 2)
        val aid =
            testResource.getParameter(ParameterEnum.PARAMETER_APPLICATIONIDENTIFIER) as ApplicationIdentifier
        val command = HealthCardCommand.select(aid)

        Assert.assertArrayEquals(expectedAPDU, TestChannel().test(command))
    }

    @Theory
    fun shouldEqualSelectCommandApplicationIdentifierSelectNextElseFirstOccurrenceRequestFcpFcpLength(
        selectNextElseFirstOccurrence: Boolean,
        requestFcp: Boolean
    ) {
        val expectedAPDU = testResource.getExpectApdu(
            ApduResultEnum.SELECTCOMMAND_APDU,
            3,
            selectNextElseFirstOccurrence,
            requestFcp
        )
        val aid =
            testResource.getParameter(ParameterEnum.PARAMETER_APPLICATIONIDENTIFIER) as ApplicationIdentifier
        val fcpLength = testResource.getParameter(ParameterEnum.PARAMETER_INT_FCPLENGTH) as Int
        val command = HealthCardCommand.select(aid, selectNextElseFirstOccurrence, requestFcp, fcpLength)

        Assert.assertArrayEquals(expectedAPDU, TestChannel().test(command))
    }

    @Theory
    fun shouldEqualSelectCommandFileIdentifierSelectDfElseEf(selectDfElseEf: Boolean) {
        val expectedAPDU =
            testResource.getExpectApdu(ApduResultEnum.SELECTCOMMAND_APDU, 4, selectDfElseEf)
        val fid =
            testResource.getParameter(ParameterEnum.PARAMETER_FILEIDENTIFIER) as FileIdentifier
        val command = HealthCardCommand.select(fid, selectDfElseEf)

        Assert.assertArrayEquals(expectedAPDU, TestChannel().test(command))
    }

    @Theory
    fun shouldEqualSelectCommandFileIdentifierSelectDfElseEfRequestFcpFcpLength(
        selectDfElseEf: Boolean,
        requestFcp: Boolean
    ) {
        val expectedAPDU = testResource.getExpectApdu(
            ApduResultEnum.SELECTCOMMAND_APDU,
            5,
            selectDfElseEf,
            requestFcp
        )
        val fid = testResource.getParameter(ParameterEnum.PARAMETER_FILEIDENTIFIER) as FileIdentifier
        val fcpLength = testResource.getParameter(ParameterEnum.PARAMETER_INT_FCPLENGTH) as Int
        val command = HealthCardCommand.select(fid, selectDfElseEf, requestFcp, fcpLength)

        Assert.assertArrayEquals(expectedAPDU, TestChannel().test(command))
    }

    companion object {
        @DataPoint
        @JvmStatic
        fun boolArray() = booleanArrayOf(true, false)
    }
}
