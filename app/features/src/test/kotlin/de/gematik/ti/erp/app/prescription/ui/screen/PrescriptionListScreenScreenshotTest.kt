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

package de.gematik.ti.erp.app.prescription.ui.screen

import de.gematik.ti.erp.app.prescription.ui.preview.PrescriptionScreenPreviewData
import de.gematik.ti.erp.app.prescription.ui.preview.PrescriptionScreenPreviewParameterProvider
import de.gematik.ti.erp.app.screenshot.BaseScreenshotTest
import de.gematik.ti.erp.app.screenshot.ScreenshotConfig
import org.junit.Test

class PrescriptionListScreenScreenshotTest(config: ScreenshotConfig) : BaseScreenshotTest(config) {

    private val previewProvider = PrescriptionScreenPreviewParameterProvider()
    private val previewDataList: List<PrescriptionScreenPreviewData>
        get() = previewProvider.values.toList()

    @Test
    fun screenshotWithPrescriptionsUserError() {
        val previewData = previewDataList.find { it.name == "some-prescriptions-user-error" } ?: previewDataList.first()
        paparazzi.snapshot {
            PrescriptionsScreenScaffoldPreview(previewData)
        }
    }

    @Test
    fun screenshotEmptyPrescriptionsUserLoggedIn() {
        val previewData = previewDataList.find { it.name == "empty-prescriptions-user-logged-in" } ?: previewDataList[1]
        paparazzi.snapshot {
            PrescriptionsScreenScaffoldPreview(previewData)
        }
    }

    @Test
    fun screenshotEmptyPrescriptionsUserLoggedOut() {
        val previewData = previewDataList.find { it.name == "empty-prescriptions-user-logged-out" } ?: previewDataList.getOrNull(2)
        if (previewData != null) {
            paparazzi.snapshot {
                PrescriptionsScreenScaffoldPreview(previewData)
            }
        }
    }

    @Test
    fun screenshotWithPrescriptionsUserLoggedIn() {
        val previewData = previewDataList.find { it.name == "with-prescriptions-user-logged-in" } ?: previewDataList.getOrNull(3)
        if (previewData != null) {
            paparazzi.snapshot {
                PrescriptionsScreenScaffoldPreview(previewData)
            }
        }
    }

    @Test
    fun screenshotWithPrescriptionsUserInvalid() {
        val previewData = previewDataList.find { it.name == "with-prescriptions-user-invalid" } ?: previewDataList.lastOrNull()
        if (previewData != null) {
            paparazzi.snapshot {
                PrescriptionsScreenScaffoldPreview(previewData)
            }
        }
    }

    @Test
    fun screenshotWithReadyPrescriptionsUserLoggedIn() {
        val previewData = previewDataList.find { it.name == "with-ready-prescriptions-user-logged-in" }
        if (previewData != null) {
            paparazzi.snapshot {
                PrescriptionsScreenScaffoldPreview(previewData)
            }
        }
    }
}
