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

package de.gematik.ti.erp.app.fhir.constant.prescription.euredeem

import de.gematik.ti.erp.app.utils.Reference

@Reference(
    info = "Link to GEM ERPEU PR PAR PATCH Task Input version 1.0.0",
    url = "https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_PAR_PATCH_Task_Input"
)
object FhirTaskEuPatchInputModelConstants {
    const val RESOURCE_TYPE = "Parameters"
    const val ID = "erp-eprescription-10-PATCH-Task-Request"
    const val PARAMETER_NAME = "eu-isRedeemableByPatientAuthorization"

    enum class FhirTaskEuPatchMeta(val identifier: String) {
        V_1_0("https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_PAR_PATCH_Task_Input|1.0"),

        @Reference(
            info = "Link to GEM ERPEU PR PAR PATCH Task Input version 1.1.2",
            url = "https://simplifier.net/erezept-workflow-eu/gem_erpeu_pr_par_patch_task_input"
        )
        V_1_1("https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_PAR_PATCH_Task_Input|1.1")
    }
}
