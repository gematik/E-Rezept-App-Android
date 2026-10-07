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
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app

import org.junit.Assert.assertTrue
import kotlin.test.Test

class UncaughtExceptionTest {

    @Test
    fun `formats nested metadata collections without callback-based joins`() {
        val exception = UncaughtException(
            throwable = IllegalStateException("primary", IllegalArgumentException("root cause")),
            packageName = "de.gematik.ti.erp.app",
            isDemoMode = false,
            metadata = linkedMapOf(
                "device" to linkedMapOf(
                    "model" to "Pixel",
                    7 to listOf("one", null, 3)
                ),
                "enabled" to true
            )
        )

        val formattedException = exception.toString()

        assertTrue(formattedException.contains("Cause Chain       : IllegalArgumentException: root cause"))
        assertTrue(formattedException.contains("  device: \n    model: Pixel\n    7: one, null, 3"))
        assertTrue(formattedException.contains("  enabled: true"))
    }
}
