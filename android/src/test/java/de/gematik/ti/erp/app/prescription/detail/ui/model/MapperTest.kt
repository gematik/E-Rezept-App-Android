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

package de.gematik.ti.erp.app.prescription.detail.ui.model

import de.gematik.ti.erp.app.db.entities.Task
import de.gematik.ti.erp.app.prescription.repository.extractMedication
import de.gematik.ti.erp.app.prescription.repository.extractMedicationRequest
import de.gematik.ti.erp.app.prescription.usecase.createMatrixCode
import de.gematik.ti.erp.app.redeem.ui.BitMatrixCode
import de.gematik.ti.erp.app.utils.testScannedTasks
import de.gematik.ti.erp.app.utils.testSingleKBVBundle
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MapperTest {

    private lateinit var task: Task
    private lateinit var matrix: BitMatrixCode

    @Before
    fun setup() {
        task = testScannedTasks[0]
        matrix = BitMatrixCode(createMatrixCode("somePayload"))
    }

    @Test
    fun `test mapToUIPrescriptionDetail`() {
        val uiDetail = mapToUIPrescriptionDetailScanned(task, matrix, true)
        assertEquals(uiDetail.taskId, task.taskId)
        assertEquals(uiDetail.accessCode, task.accessCode)
        assertEquals(uiDetail.number, task.nrInScanSession)
        assertEquals(uiDetail.scannedOn, task.scannedOn)
    }

    @Test
    fun `test mapToUIPrescriptionOrder`() {
        val bundle = testSingleKBVBundle()
        val uiPrescriptionOrder = mapToUIPrescriptionOrder(
            task,
            requireNotNull(bundle.extractMedication()),
            requireNotNull(bundle.extractMedicationRequest())
        )
        assertEquals(task.taskId, uiPrescriptionOrder.taskId)
        assertEquals(task.accessCode, uiPrescriptionOrder.accessCode)
        assertEquals(false, uiPrescriptionOrder.substitutionsAllowed)
    }
}
