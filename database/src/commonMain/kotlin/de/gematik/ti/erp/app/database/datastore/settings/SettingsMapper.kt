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

package de.gematik.ti.erp.app.database.datastore.settings

import de.gematik.ti.erp.app.settings.model.AppVersionErpModel
import de.gematik.ti.erp.app.settings.model.SettingsErpModel
import de.gematik.ti.erp.app.settings.model.ThemeMode

fun SettingsErpModel.toSettingsEntity(): SettingsEntity =
    SettingsEntity(
        latestAppVersion = latestAppVersion.toAppVersionEntity(),
        onboardingShownIn = onboardingShownIn?.toAppVersionEntity(),
        theme = theme.name,
        welcomeDrawerShown = welcomeDrawerShown,
        zoomEnabled = zoomEnabled,
        userHasAcceptedInsecureDevice = userHasAcceptedInsecureDevice,
        userHasAcceptedIntegrityNotOk = userHasAcceptedIntegrityNotOk,
        trackingAllowed = trackingAllowed,
        screenShotsAllowed = screenShotsAllowed
    )

fun SettingsEntity.toSettingsErpModel(): SettingsErpModel =
    SettingsErpModel(
        latestAppVersion = latestAppVersion.toAppVersionErpModel(),
        onboardingShownIn = onboardingShownIn?.toAppVersionErpModel(),
        theme = runCatching { ThemeMode.valueOf(theme) }.getOrDefault(ThemeMode.SYSTEM),
        welcomeDrawerShown = welcomeDrawerShown,
        zoomEnabled = zoomEnabled,
        userHasAcceptedInsecureDevice = userHasAcceptedInsecureDevice,
        userHasAcceptedIntegrityNotOk = userHasAcceptedIntegrityNotOk,
        trackingAllowed = trackingAllowed,
        screenShotsAllowed = screenShotsAllowed
    )

fun AppVersionErpModel.toAppVersionEntity(): AppVersionEntity =
    AppVersionEntity(
        name = name,
        code = code
    )

fun AppVersionEntity.toAppVersionErpModel(): AppVersionErpModel =
    AppVersionErpModel(
        name = name,
        code = code
    )
