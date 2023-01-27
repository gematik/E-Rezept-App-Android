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

package de.gematik.ti.erp.app.nfc.command

import de.gematik.ti.erp.app.card.model.command.HealthCardCommand
import de.gematik.ti.erp.app.card.model.command.manageSecEnvWithoutCurves
import org.junit.Assert
import org.junit.experimental.theories.DataPoint
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.runner.RunWith

@RunWith(Theories::class)
class ManageSecurityEnvironmentCommandTest {
    private val testResource = TestResource()

    @Theory
    fun shouldEqualManageSecurityEnvironmentCommandMseUseCaseKeyBooleanOid(dfSpecific: Boolean) {
        val expectedAPDU = testResource.getExpectApdu(
            ApduResultEnum.MANAGESECURITYENVIRONMENTCOMMAND_APDU,
            3,
            dfSpecific
        )
        val key = TestResource.KEY_PRK_EGK_AUT_CVC_E256
        val oid = testResource.getParameter(ParameterEnum.PARAMETER_BYTEARRAY_OID) as ByteArray
        val command = HealthCardCommand.manageSecEnvWithoutCurves(key, dfSpecific, oid)

        Assert.assertArrayEquals(TestChannel().test(command), expectedAPDU)
    }

    companion object {
        @DataPoint
        @JvmStatic
        fun boolArray() = booleanArrayOf(true, false)
    }
}
