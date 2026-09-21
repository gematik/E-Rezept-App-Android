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

package de.gematik.ti.erp.app.demomode.repository.diga

import de.gematik.ti.erp.app.diga.model.DigaData
import de.gematik.ti.erp.app.diga.repository.DigaInformationRepository

/**
 * Demo-mode implementation of [DigaInformationRepository].
 *
 * Returns a static [DigaData] for any PZN so the DiGA detail screen shows
 * realistic data without making any real BfArM network calls.
 */
class DemoDigaInformationRepository : DigaInformationRepository {

    override suspend fun fetchDigaByPzn(pzn: String): Result<DigaData> =
        Result.success(
            DigaData(
                pzn = pzn,
                contractMedicalServicesRequired = false,
                additionalDevices = emptyList(),
                maxCost = "0,00 €",
                description = "Diese DiGA unterstützt Sie bei der Behandlung und Verbesserung Ihrer Gesundheit " +
                    "auf digitale Weise. (Demo-Modus)",
                languageNames = listOf("Deutsch", "Englisch"),
                supportedPlatforms = listOf("Android", "iOS"),
                iconUrl = "",
                iconId = "",
                handbookUrl = "https://www.bfarm.de",
                helpUrl = "https://www.bfarm.de"
            )
        )
}
