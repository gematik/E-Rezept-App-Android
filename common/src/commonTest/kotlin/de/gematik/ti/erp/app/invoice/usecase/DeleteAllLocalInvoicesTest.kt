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

package de.gematik.ti.erp.app.invoice.usecase

import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteAllLocalInvoicesTest {
    private val repository = mockk<InvoiceRepository>()
    private val testScheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(testScheduler)
    private lateinit var useCase: DeleteAllLocalInvoices

    @Before
    fun setup() {
        coEvery { repository.deleteLocalInvoiceById(any()) } returns Unit
        useCase = DeleteAllLocalInvoices(
            invoiceRepository = repository,
            dispatcher = dispatcher
        )
    }

    @Test
    fun testDeleteInvoice() {
        runTest(testScheduler) {
            useCase.invoke(listOf("123", "234"))
            coVerify(exactly = 1) {
                repository.deleteLocalInvoiceById("123")
            }
            coVerify(exactly = 1) {
                repository.deleteLocalInvoiceById("234")
            }
        }
    }
}
