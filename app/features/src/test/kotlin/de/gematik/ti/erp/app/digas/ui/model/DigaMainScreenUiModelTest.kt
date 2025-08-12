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

package de.gematik.ti.erp.app.digas.ui.model

import de.gematik.ti.erp.app.diga.model.DigaStatus
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val ready = DigaMainScreenUiModel(
    canBeRedeemedAgain = true,
    status = DigaStatus.Ready,
    lifeCycleTimestamps = DigaTimestamps()
)

private val inProgress = ready.copy(status = DigaStatus.InProgress(Clock.System.now()))
private val completedWithRejection = ready.copy(status = DigaStatus.CompletedWithRejection(Clock.System.now()))
private val completedSuccessfully = ready.copy(status = DigaStatus.CompletedSuccessfully)
private val downloadingApp = ready.copy(status = DigaStatus.DownloadDigaApp)
private val openAppWithRedeemCode = ready.copy(status = DigaStatus.OpenAppWithRedeemCode)
private val readyForSelfArchiveDiga = ready.copy(status = DigaStatus.ReadyForSelfArchiveDiga)
private val selfArchiveDiga = ready.copy(status = DigaStatus.SelfArchiveDiga)

class DigaMainScreenUiModelTest {

    @Test
    fun `ready can't be archived`() {
        assertFalse { ready.isArchived }
        assertFalse { ready.isArchivable() }
        assertFalse { ready.isArchiveRevertable }
    }

    @Test
    fun `inProgress can't be archived`() {
        assertFalse { inProgress.isArchived }
        assertFalse { inProgress.isArchivable() }
        assertFalse { inProgress.isArchiveRevertable }
    }

    @Test
    fun `CompletedWithRejection can be archived`() {
        assertFalse { completedWithRejection.isArchived }
        assertTrue { completedWithRejection.isArchivable() }
        assertFalse { completedWithRejection.isArchiveRevertable }
    }

    @Test
    fun `CompletedSuccessfully can be archived`() {
        assertFalse { completedSuccessfully.isArchived }
        assertTrue { completedSuccessfully.isArchivable() }
        assertFalse { completedSuccessfully.isArchiveRevertable }
    }

    @Test
    fun `downloadingApp can be archived`() {
        assertFalse { downloadingApp.isArchived }
        assertTrue { downloadingApp.isArchivable() }
        assertFalse { downloadingApp.isArchiveRevertable }
    }

    @Test
    fun `openAppWithRedeemCode can be archived`() {
        assertFalse { openAppWithRedeemCode.isArchived }
        assertTrue { openAppWithRedeemCode.isArchivable() }
        assertFalse { openAppWithRedeemCode.isArchiveRevertable }
    }

    @Test
    fun `readyForSelfArchiveDiga can be archived`() {
        assertFalse { readyForSelfArchiveDiga.isArchived }
        assertTrue { readyForSelfArchiveDiga.isArchivable() }
        assertFalse { readyForSelfArchiveDiga.isArchiveRevertable }
    }

    @Test
    fun `selfArchiveDiga can be archived`() {
        assertFalse { selfArchiveDiga.isArchived }
        assertTrue { selfArchiveDiga.isArchivable() }
        assertFalse { selfArchiveDiga.isArchiveRevertable }
    }
}
