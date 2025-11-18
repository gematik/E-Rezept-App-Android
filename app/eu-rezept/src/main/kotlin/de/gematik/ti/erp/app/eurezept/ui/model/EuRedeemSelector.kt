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
 * Enum containing identifiers for EU redemption navigation triggers and tracking flags.
 *
 * These selectors are used with [NavigationTriggerDataStore] to manage navigation state
 * and track user interactions in the EU redemption flow.
 */
enum class EuRedeemSelector {
    /**
     * Identifier for tracking whether the EU redemption instruction screen has been viewed.
     *
     * Used to determine if the user should be automatically navigated to the redemption code screen
     * or if they need to view the instructions first. Once set and consumed, prevents duplicate
     * navigation on subsequent app launches.
     */
    WAS_EU_REDEEM_INSTRUCTION_VIEWED;
}
