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

package de.gematik.ti.erp.app.mocks.settings

import de.gematik.ti.erp.app.settings.datasource.SettingsDataSource
import de.gematik.ti.erp.app.settings.model.SettingsData
import kotlinx.coroutines.flow.MutableStateFlow

class OnboardingNotDoneMockSettingsDataSource : SettingsDataSource {
    override val appVersion = SETTINGS_APP_VERSION_DATA

    override val authentication: MutableStateFlow<SettingsData.Authentication> =
        MutableStateFlow(SETTINGS_UNSPECIFIED)

    override val pharmacySearch: MutableStateFlow<SettingsData.PharmacySearch> =
        MutableStateFlow(SETTINGS_PHARMACY_SEARCH_RESULT_DATA)

    override val generalData: MutableStateFlow<SettingsData.General> =
        MutableStateFlow(SETTINGS_GENERAL_DATA)
}
