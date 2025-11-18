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

package de.gematik.ti.erp.app.eurezept.ui.model

/**
 * Represents the UI status of an EU prescription during selection or redemption actions.
 *
 * This sealed interface is used to indicate the current state of a prescription in the UI,
 * such as when it is loading, idle (ready), or has encountered an error.
 */
sealed interface EuPrescriptionStatus {
    /**
     * Indicates that the prescription is currently being processed (e.g., toggling selection or redeeming).
     */
    data object Loading : EuPrescriptionStatus

    /**
     * Indicates that the prescription is idle and ready for user interaction.
     */
    data object Idle : EuPrescriptionStatus

    /**
     * Indicates that an error occurred while processing the prescription.
     */
    data object Error : EuPrescriptionStatus
}
