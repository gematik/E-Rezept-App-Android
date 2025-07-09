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

package de.gematik.ti.erp.app.orderhealthcard.usecase

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LoadHealthInsuranceListUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var resources: Resources
    private lateinit var afd: AssetFileDescriptor

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Setup mocked context and resource access
        context = mockk()
        resources = mockk()
        afd = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test loading the data from file`() = runTest {
        val tempFile = File.createTempFile("test-health-companies", ".json")
        tempFile.writeText(contacts)
        val fileInputStream = FileInputStream(tempFile)
        val afd = mockk<AssetFileDescriptor>()
        every { afd.createInputStream() } returns fileInputStream

        every { resources.openRawResourceFd(any()) } returns afd
        every { context.resources } returns resources

        val useCase = LoadHealthInsuranceListUseCase(context, testDispatcher)
        val result = useCase().first()

        assertEquals(2, result.size)
        assertEquals("Kasse 1", result[0].name)
        assertEquals("Kasse 2", result[1].name)

        fileInputStream.close()
        tempFile.delete()
    }

    @Test
    fun `test loadHealthInsuranceContactsFromCSV() with expected data`() {
        loadHealthInsuranceContactsFromJSON(contacts.byteInputStream()).let {
            assertEquals("Kasse 1", it[0].name)
            assertEquals("TestMail@test.de", it[0].healthCardAndPinMail)
            assertEquals("+123123", it[0].healthCardAndPinPhone)
            assertEquals("https://www.TestURL.de/", it[0].healthCardAndPinUrl)
            assertEquals("https://www.TestPinURL.de/", it[0].pinUrl)
            assertEquals("testHeader", it[0].subjectCardAndPinMail)
            assertEquals("testBody", it[0].bodyCardAndPinMail)
            assertEquals("testHeader", it[0].subjectPinMail)
            assertEquals("testBody", it[0].bodyPinMail)

            assertEquals("Kasse 2", it[1].name)
            assertEquals(null, it[1].healthCardAndPinMail)
            assertEquals(null, it[1].healthCardAndPinPhone)
            assertEquals(null, it[1].healthCardAndPinUrl)
            assertEquals(null, it[1].pinUrl)
            assertEquals(null, it[1].subjectCardAndPinMail)
            assertEquals(null, it[1].bodyCardAndPinMail)
            assertEquals(null, it[1].subjectPinMail)
            assertEquals(null, it[1].bodyPinMail)
        }
    }

    companion object {
        private val contacts =
            """
                [
                   {
                      "name":"Kasse 1",
                      "healthCardAndPinPhone":"+123123",
                      "healthCardAndPinMail":"TestMail@test.de",
                      "healthCardAndPinUrl":"https://www.TestURL.de/",
                      "pinUrl":"https://www.TestPinURL.de/",
                      "subjectCardAndPinMail":"testHeader",
                      "bodyCardAndPinMail":"testBody",
                      "subjectPinMail":"testHeader",
                      "bodyPinMail":"testBody"
                   },
                   {
                      "name":"Kasse 2",
                      "healthCardAndPinPhone":null,
                      "healthCardAndPinMail":null,
                      "healthCardAndPinUrl":null,
                      "pinUrl":null,
                      "subjectCardAndPinMail":null,
                      "bodyCardAndPinMail":null,
                      "subjectPinMail":null,
                      "bodyPinMail":null
                   }
                ]
            """.trimIndent()

        private val mockInputStream = ByteArrayInputStream(contacts.toByteArray())
    }
}
