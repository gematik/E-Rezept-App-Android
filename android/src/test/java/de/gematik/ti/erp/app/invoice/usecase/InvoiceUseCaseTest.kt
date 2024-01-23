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

@file:Suppress("ktlint:max-line-length", "ktlint:argument-list-wrapping")

package de.gematik.ti.erp.app.invoice.usecase

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.BeforeTest

import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class InvoiceUseCaseTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @MockK
    lateinit var useCase: InvoiceUseCase

    @MockK
    lateinit var repositpry: InvoiceRepository

    @BeforeTest
    fun setup() {
        MockKAnnotations.init(this)
        useCase = spyk(
            InvoiceUseCase(repositpry, coroutineRule.dispatchers)
        )
    }

    @Test
    fun `invoices - should return invoices sorted by timestamp and grouped by year`() =

        runTest {
            every { useCase.invoicesFlow("1234") } returns flowOf(listOf(pkvInvoice, pkvInvoice2))

            val invoices = useCase.invoices("1234").first()

            println("size : " + invoices.size)
            assertEquals(
                mapOf(
                    Pair(
                        first = later.toLocalDateTime(TimeZone.currentSystemDefault()).year,
                        second = listOf(pkvInvoice2)
                    ),
                    Pair(
                        first = now.toLocalDateTime(TimeZone.currentSystemDefault()).year,
                        second = listOf(pkvInvoice)
                    )
                ),
                invoices
            )
        }

    @Test
    fun `invoice create dmc payload`() =
        assertEquals("{\"urls\":[\"ChargeItem/01234?ac=98765\"]}", pkvInvoice.dmcPayload)
}
