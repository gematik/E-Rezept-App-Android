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

package de.gematik.ti.erp.app.nfc.command

import de.gematik.ti.erp.app.card.model.command.HealthCardCommand
import de.gematik.ti.erp.app.card.model.command.generalAuthenticate
import org.junit.Assert
import org.junit.experimental.theories.DataPoint
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.runner.RunWith

@RunWith(Theories::class)
class GeneralAuthenticateCommandTest {
    private val testResource = TestResource()

    @Theory
    fun shouldEqualGeneralAuthenticateCommand(commandChaining: Boolean) {
        val expectedAPDU = testResource.getExpectApdu(
            ApduResultEnum.GENERALAUTHENTICATECOMMAND_APDU,
            1,
            commandChaining
        )
        val command = HealthCardCommand.generalAuthenticate(commandChaining)

        Assert.assertArrayEquals(expectedAPDU, TestChannel().test(command))
    }

    @Theory
    fun shouldEqualGeneralAuthenticateCommand1(commandChaining: Boolean) {
        val expectedAPDU = testResource.getExpectApdu(
            ApduResultEnum.GENERALAUTHENTICATECOMMAND_APDU,
            2,
            commandChaining
        )
        val command = HealthCardCommand.generalAuthenticate(commandChaining, byteArrayOf(), 1)

        Assert.assertArrayEquals(expectedAPDU, TestChannel().test(command))
    }

    @Theory
    fun shouldEqualGeneralAuthenticateCommand3(commandChaining: Boolean) {
        val expectedAPDU = testResource.getExpectApdu(
            ApduResultEnum.GENERALAUTHENTICATECOMMAND_APDU,
            3,
            commandChaining
        )
        val command = HealthCardCommand.generalAuthenticate(commandChaining, byteArrayOf(), 3)

        Assert.assertArrayEquals(expectedAPDU, TestChannel().test(command))
    }

    companion object {
        @DataPoint
        @JvmStatic
        fun boolArray() = booleanArrayOf(true, false)
    }
}
