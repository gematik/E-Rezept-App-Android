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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.settings.model

object SettingsData {
    data class General(
        val latestAppVersion: AppVersion,
        val onboardingShownIn: AppVersion?,
        val welcomeDrawerShown: Boolean,
        val mainScreenTooltipsShown: Boolean,
        val zoomEnabled: Boolean,
        val userHasAcceptedInsecureDevice: Boolean,
        val userHasAcceptedIntegrityNotOk: Boolean,
        val mlKitAccepted: Boolean,
        val trackingAllowed: Boolean,
        val screenShotsAllowed: Boolean
    )

    data class AppVersion(
        val code: Int,
        val name: String
    )

    // is not used anywhere ???
    data class PharmacySearch(
        val name: String,
        val locationEnabled: Boolean,
        val deliveryService: Boolean = false,
        val onlineService: Boolean = false,
        val openNow: Boolean = false
    ) {
        fun isAnySet(): Boolean =
            deliveryService || onlineService || openNow
    }
}
