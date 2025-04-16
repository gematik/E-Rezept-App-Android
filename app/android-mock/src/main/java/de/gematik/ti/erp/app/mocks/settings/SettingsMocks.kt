/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.mocks.settings

import de.gematik.ti.erp.app.settings.model.SettingsData

internal val SETTINGS_APP_VERSION_DATA = SettingsData.AppVersion(
    code = 1,
    name = "1.0.0"
)

internal val SETTINGS_PHARMACY_SEARCH_RESULT_DATA = SettingsData.PharmacySearch(
    name = "TEST-ONLY",
    locationEnabled = true,
    deliveryService = false,
    onlineService = false,
    openNow = false
)

internal val SETTINGS_GENERAL_DATA = SettingsData.General(
    latestAppVersion = SETTINGS_APP_VERSION_DATA,
    onboardingShownIn = null, // app will show onboarding if this is null
    welcomeDrawerShown = true,
    zoomEnabled = false,
    userHasAcceptedInsecureDevice = true,
    mainScreenTooltipsShown = true,
    mlKitAccepted = false,
    screenShotsAllowed = true,
    trackingAllowed = false,
    userHasAcceptedIntegrityNotOk = true
)

internal val SETTINGS_PASSWORD = SettingsData.Authentication(
    password = SettingsData.Authentication.Password(password = "password"),
    deviceSecurity = false,
    failedAuthenticationAttempts = 0
)

internal val SETTINGS_UNSPECIFIED = SettingsData.Authentication(
    password = null,
    deviceSecurity = false,
    failedAuthenticationAttempts = 0
)
