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

package de.gematik.ti.erp.app.core.mvvm

import androidx.lifecycle.ViewModel
import de.gematik.ti.erp.app.core.BaseViewModel
import de.gematik.ti.erp.app.core.ViewModelSet
import de.gematik.ti.erp.app.core.component1
import de.gematik.ti.erp.app.core.component2
import de.gematik.ti.erp.app.core.component3
import io.mockk.mockk
import org.junit.Assert.assertSame
import org.junit.Test

private class VM1 : BaseViewModel()
private class VM2 : BaseViewModel()
private class VM3 : BaseViewModel()

class ViewModelHolderTest {
    private val mockVM1 = mockk<VM1>()
    private val mockVM2 = mockk<VM2>()
    private val mockVM3 = mockk<VM3>()

    @Test
    fun `test componentX function`() {
        val holder: ViewModelSet = setOf(mockVM1, mockVM2, mockVM3)

        val (vm1: VM1) = holder
        assertSame(mockVM1, vm1)

        val (vm2: VM2) = holder
        assertSame(mockVM2, vm2)

        val (vm3: VM3) = holder
        assertSame(mockVM3, vm3)
    }

    @Test
    fun `test componentX function with multiple parameters`() {
        val holder: ViewModelSet = setOf(mockVM1, mockVM2, mockVM3)

        val (vm1: VM1, vm2: VM2, vm3: VM3) = holder
        assertSame(mockVM1, vm1)
        assertSame(mockVM2, vm2)
        assertSame(mockVM3, vm3)
    }

    @Test
    fun `test componentX function with generic type - should be deterministic`() {
        val holder: ViewModelSet = setOf(mockVM1, mockVM2, mockVM3)

        val (vm1: ViewModel, vm2: ViewModel, vm3: ViewModel) = holder
        assertSame(vm1, vm2)
        assertSame(vm2, vm3)
    }

    @Test
    fun `test componentX function with mixed ordering`() {
        val holder: ViewModelSet = setOf(mockVM1, mockVM2, mockVM3)

        val (vm3: VM3, vm1: VM1, vm2: VM2) = holder
        assertSame(mockVM1, vm1)
        assertSame(mockVM2, vm2)
        assertSame(mockVM3, vm3)
    }
}
