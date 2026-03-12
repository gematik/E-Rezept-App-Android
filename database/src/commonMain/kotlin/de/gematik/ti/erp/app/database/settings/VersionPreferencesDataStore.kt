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

package de.gematik.ti.erp.app.database.settings

import kotlinx.coroutines.flow.StateFlow

/**
 * Consent version options for ERP Charge (PKV).
 * Only used in debug builds for testing different consent versions.
 */
enum class ConsentVersion(val version: String, val displayName: String) {
    V1_0("1.0", "(Production) Version 1.0"),
    V1_1("1.1", "Version 1.1")
}

/**
 * Communication version options for dispense requests.
 * Only used in debug builds for testing different communication versions.
 */
enum class CommunicationVersion(val version: String, val displayName: String) {
    V_1_2("1.2", "Version 1.2"),
    V_1_3("1.3", "Version 1.3"),
    V_1_4("1.4", "Version 1.4"),
    V_1_5("1.5", "(Production) Version 1.5"),
    V_1_6("1.6", "Version 1.6")
}

enum class EuVersion(val version: String, val displayName: String) {
    V_1_0("1.0", "Version 1.0"),
    V_1_1("1.1", "Version 1.1");
}

/**
 * Interface for consent version preferences (DEBUG ONLY).
 *
 * Abstracts consent version selection for testing different versions.
 * This setting is only available in debug builds and is ignored in production.
 */
interface ConsentVersionDataStore {
    /**
     * Observes the current consent version preference.
     *
     * Emits [ConsentVersion.V1_1] by default (production default).
     *
     * @return StateFlow of [ConsentVersion] that emits whenever the preference changes
     */
    val consentVersion: StateFlow<ConsentVersion>

    /**
     * Saves the consent version preference (DEBUG ONLY).
     *
     * NOTE: This setting is ignored in production builds.
     *
     * @param consentVersion The consent version to save (V1_0 or V1_1)
     */
    fun saveConsentVersion(consentVersion: ConsentVersion)
}

/**
 * Interface for communication version preferences (DEBUG ONLY).
 *
 * Abstracts communication version selection for testing different versions.
 * This setting is only available in debug builds and is ignored in production.
 */
interface CommunicationVersionDataStore {
    /**
     * Observes the current communication version preference.
     *
     * Emits [CommunicationVersion.V_1_5] by default (production default).
     *
     * @return StateFlow of [CommunicationVersion] that emits whenever the preference changes
     */
    val communicationVersion: StateFlow<CommunicationVersion>

    /**
     * Saves the communication version preference (DEBUG ONLY).
     *
     * NOTE: This setting is ignored in production builds.
     *
     * @param communicationVersion The communication version to save
     */
    fun saveCommunicationVersion(communicationVersion: CommunicationVersion)
}

interface EuVersionDataStore {
    val euVersion: StateFlow<EuVersion>
    fun saveEuVersion(euVersion: EuVersion)
}

/**
 * Communication DiGA version options for DiGA dispense requests.
 * Only used in debug builds for testing different DiGA communication versions.
 * Production default is [V_1_4].
 */
enum class CommunicationDigaVersion(val version: String, val displayName: String) {
    V_1_4("1.4", "(Production) Version 1.4"),
    V_1_5("1.5", "Version 1.5"),
    V_1_6("1.6", "Version 1.6")
}

/**
 * Interface for communication DiGA version preferences (DEBUG ONLY).
 *
 * Abstracts DiGA communication version selection for testing different versions.
 * This setting is only available in debug builds and is ignored in production.
 */
interface CommunicationDigaVersionDataStore {
    /**
     * Observes the current DiGA communication version preference.
     *
     * Emits [CommunicationDigaVersion.V_1_4] by default (production default).
     *
     * @return StateFlow of [CommunicationDigaVersion] that emits whenever the preference changes
     */
    val communicationDigaVersion: StateFlow<CommunicationDigaVersion>

    /**
     * Saves the DiGA communication version preference (DEBUG ONLY).
     *
     * NOTE: This setting is ignored in production builds.
     *
     * @param communicationDigaVersion The DiGA communication version to save
     */
    fun saveCommunicationDigaVersion(communicationDigaVersion: CommunicationDigaVersion)
}
