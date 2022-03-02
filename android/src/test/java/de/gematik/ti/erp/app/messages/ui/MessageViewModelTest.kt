/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.messages.ui

import de.gematik.ti.erp.app.db.entities.Communication
import de.gematik.ti.erp.app.messages.listOfCommunicationsRead
import de.gematik.ti.erp.app.messages.usecase.MessageUseCase
import de.gematik.ti.erp.app.utils.CoroutineTestRule
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class MessageViewModelTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var viewModel: MessageViewModel
    private lateinit var useCase: MessageUseCase

    @Before
    fun setUp() {
        useCase = mockk()
        viewModel = MessageViewModel(useCase, coroutineRule.testDispatchProvider)
    }

    @Test
    fun `test loading communications - not empty list`() =
        runTest {
            every { useCase.loadCommunicationsLocally(any()) } returns flow { listOfCommunicationsRead() }
            viewModel.fetchCommunications().collect {
                assertTrue(it.isNotEmpty())
            }
        }

    @Test
    fun `test loading communications - empty list`() =
        runTest {
            every { useCase.loadCommunicationsLocally(any()) } returns flow { listOf<Communication>() }
            viewModel.fetchCommunications().collect {
                assertTrue(it.isEmpty())
            }
        }
}
