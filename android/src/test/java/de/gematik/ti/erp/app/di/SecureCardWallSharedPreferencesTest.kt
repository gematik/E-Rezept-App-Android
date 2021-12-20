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

package de.gematik.ti.erp.app.di

import android.content.SharedPreferences
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import io.mockk.mockk
import org.junit.Before

class SecureCardWallSharedPreferencesTest {

    private lateinit var appPrefs: SecureCardWallSharedPreferences
    private lateinit var appNormalPrefs: SharedPreferences
    private lateinit var appDemoPrefs: SharedPreferences
    private lateinit var demoUseCase: DemoUseCase

    @Before
    fun setup() {
        appNormalPrefs = mockk(relaxed = true)
        appDemoPrefs = mockk(relaxed = true)
        demoUseCase = mockk(relaxed = true)

        appPrefs = SecureCardWallSharedPreferences(appNormalPrefs, appDemoPrefs, demoUseCase)
    }
//
//    @Test
//    fun `expose normal preferences`() {
//        every { demoMode.isDemoModeActive } answers { false }
//
//        assertEquals(appNormalPrefs, appPrefs())
//    }
//
//    @Test
//    fun `expose demo preferences`() {
//        every { demoMode.isDemoModeActive } answers { true }
//
//        assertEquals(appDemoPrefs, appPrefs())
//    }
}
