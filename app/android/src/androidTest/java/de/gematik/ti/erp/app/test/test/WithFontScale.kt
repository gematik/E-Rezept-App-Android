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

package de.gematik.ti.erp.app.test.test

import de.gematik.ti.erp.app.test.test.core.execShellCmd
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
open class WithFontScale(protected val fontScale: String) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: fontScale={0}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf("1.0"),
                arrayOf("1.3")
            )
        }

        @JvmStatic
        @BeforeClass
        fun disableGestureNavbar() {
            execShellCmd("cmd overlay disable com.android.internal.systemui.navbar.gestural")
        }

        @JvmStatic
        @AfterClass
        fun enableGestureNavbar() {
            execShellCmd("cmd overlay enable com.android.internal.systemui.navbar.gestural")
        }
    }

    @Before
    fun changeScales() {
        execShellCmd("settings put system font_scale $fontScale")
    }

    @After
    fun resetScales() {
        execShellCmd("settings put system font_scale 1.0")
    }
}
