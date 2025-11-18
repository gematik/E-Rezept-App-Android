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

package de.gematik.ti.erp.app.prescription.share.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.share.presentation.SharePrescriptionController.HandleResult
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SharePrescriptionControllerTest {
    private lateinit var context: Context
    private lateinit var useCase: PrescriptionUseCase
    private val profileId = "profile-1"

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        context = mockk(relaxed = true)
        useCase = mockk()

        every { context.getString(any()) } returns "Medication"
        every { useCase.getAllTasksWithTaskIdOnly() } returns flowOf(emptyList())
        coEvery { useCase.saveScannedTasks(any(), any(), any()) } just Runs
        // We don't actually start an Activity in unit tests—just capture the Intent.
        every { context.startActivity(any()) } just runs
    }

    private fun controllerWithProfile(pid: ProfileIdentifier? = profileId) =
        SharePrescriptionController(
            prescriptionUseCase = useCase,
            context = context,
            profileId = pid
        )

    @Test
    fun `share with name builds chooser with fragment url containing 3 fields`() {
        val controller = controllerWithProfile(profileId)

        val taskId = "160.000.113.613.873.45"
        val access = "873f108fbbb9b38edef39a8e75877bb021f3b92e748e2ae1975bb42c1a4be696"
        val name = "Kaletra 200 mg/50 mg Filmtabletten"

        val intentSlot: CapturingSlot<Intent> = slot()
        every { context.startActivity(capture(intentSlot)) } just runs

        controller.share(taskId, access, name)

        val chooser = intentSlot.captured
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)

        val inner = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        requireNotNull(inner)
        assertEquals(Intent.ACTION_SEND, inner.action)

        val sharedText = inner.getStringExtra(Intent.EXTRA_TEXT)!!
        assert(sharedText.startsWith("https://erezept.gematik.de/prescription"))

        val encodedFrag = sharedText.substringAfter('#')
        val decoded = Uri.decode(encodedFrag)
        assertEquals("[\"$taskId|$access|$name\"]", decoded)
    }

    @Test
    fun `share without name builds chooser with fragment url containing 2 fields`() {
        val controller = controllerWithProfile(profileId)

        val taskId = "160.000.113.613.873.43"
        val access = "873f108fbbb9b38edef39a8e75877bb021f3b92e748e2ae1975bb42c1a4be696"

        val intentSlot: CapturingSlot<Intent> = slot()
        every { context.startActivity(capture(intentSlot)) } just runs

        controller.share(taskId, access, null)

        val inner = intentSlot.captured.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        requireNotNull(inner)
        val sharedText = inner.getStringExtra(Intent.EXTRA_TEXT)!!
        val encodedFrag = sharedText.substringAfter('#')
        val decoded = Uri.decode(encodedFrag)
        assertEquals("[\"$taskId|$access\"]", decoded)
        verify(exactly = 1) { context.startActivity(any()) }
    }

    @Test
    fun `handle - not allowed url returns Failure`() = runTest {
        // Host isn't allowed -> isSharePrescriptionAllowed() should be false
        val url = "https://example.com/prescription/#%5B%22x%7Cy%22%5D"

        val result = controllerWithProfile().handle(url)
        assertEquals(listOf(HandleResult.Failure), result)
        coVerify(exactly = 0) { useCase.saveScannedTasks(any(), any(), any()) }
    }

    @Suppress("ktlint:max-line-length")
    @Test
    fun `handle - happy path with two items returns two TaskSaved and saves twice`() = runTest {
        val url =
            "https://erezept.gematik.de/prescription/#%5B%22160.000.113.613.873.45%7C873f108fbbb9b38edef39a8e75877bb021f3b92e748e2ae1975bb42c1a4be696%7CKaletra%22,%22160.000.113.613.873.43%7C873f108fbbb9b38edef39a8e75877bb021f3b92e748e2ae1975bb42c1a4be696%7CAcaimoum%22%5D"

        val results = controllerWithProfile().handle(url)

        assertEquals(
            listOf(
                HandleResult.TaskSaved,
                HandleResult.TaskSaved
            ),
            results
        )
        coVerify(exactly = 2) {
            useCase.saveScannedTasks(
                profileId = profileId,
                tasks = withArg { list ->
                    require(list.size == 1)
                    val t = list.first() as ScannedTaskData.ScannedTask
                    assertThat("", t.taskId.startsWith("160."))
                    assertThat("", t.accessCode.length >= 16)
                    // assertEquals("Kaletra 200 mg/50 mg Filmtabletten", t.name)
                },
                medicationString = any()
            )
        }
    }

    @Test
    fun `handle - existing task returns TaskAlreadyExists and does not save`() = runTest {
        val existingTaskId = "160.000.113.613.873.43"
        every { useCase.getAllTasksWithTaskIdOnly() } returns flowOf(listOf(existingTaskId))

        val url =
            "https://erezept.gematik.de/prescription/#%5B%22$existingTaskId%7C873f108fbbb9b38edef39a8e75877bb021f3b92e748e2ae1975bb42c1a4be696%7CName%22%5D"

        val results = controllerWithProfile().handle(url)
        assertEquals(listOf(HandleResult.TaskAlreadyExists), results)
        coVerify(exactly = 0) { useCase.saveScannedTasks(any(), any(), any()) }
    }

    @Test
    fun `handle - invalid accessCode pattern returns Failure`() = runTest {
        // Access code "short" should fail TwoDCodeValidator
        val url =
            "https://erezept.gematik.de/prescription/#%5B%22160.000.113.613.873.60%7Cshort%7CName%22%5D"

        val results = controllerWithProfile().handle(url)
        assertEquals(listOf(HandleResult.Failure), results)
        coVerify(exactly = 0) { useCase.saveScannedTasks(any(), any(), any()) }
    }

    @Test
    fun `handle - single item without name uses fallback medication string and saves`() = runTest {
        // Two fields only → name = null
        val url =
            "https://erezept.gematik.de/prescription/#160.000.113.613.873.60%7Cabcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234"

        val results = controllerWithProfile().handle(url)
        assertEquals(listOf(HandleResult.TaskSaved), results)

        val tasksSlot = slot<List<ScannedTaskData.ScannedTask>>()
        val medSlot = slot<String>()

        coVerify(exactly = 1) {
            useCase.saveScannedTasks(
                profileId = profileId,
                tasks = capture(tasksSlot),
                medicationString = capture(medSlot)
            )
        }

        val saved = tasksSlot.captured
        assertEquals(1, saved.size)
        val st = saved.first()
        assertEquals(profileId, st.profileId)
        assertEquals(0, st.index)
        assertEquals("", st.name)
        assertEquals("160.000.113.613.873.60", st.taskId)
        assertEquals(
            "abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234",
            st.accessCode
        )
        assertNull(st.redeemedOn)
        println("medSlot.captured ${medSlot.captured} is value")
        assertTrue(medSlot.captured.isNotBlank())
    }

    @Suppress("ktlint:max-line-length")
    @Test
    fun `handle - null profileId returns Failure`() = runTest {
        val url =
            "https://erezept.gematik.de/prescription/#%5B%22160.000.113.613.873.43%7C873f108fbbb9b38edef39a8e75877bb021f3b92e748e2ae1975bb42c1a4be696%7CName%22%5D"

        val results = controllerWithProfile(pid = null).handle(url)

        // requireNotNull(profileId) throws, caught by IllegalArgumentException branch → Failure
        assertEquals(listOf(HandleResult.Failure), results)
        coVerify(exactly = 0) { useCase.saveScannedTasks(any(), any(), any()) }
    }
}
