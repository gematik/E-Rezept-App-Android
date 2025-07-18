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

package de.gematik.ti.erp.app.demomode.datasource.data

import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.hours

object DemoPharmacyInfo {

    internal val PHARMACY_NAMES = listOf(
        "Apotheke am Zoo",
        "City Apotheke",
        "Europa Apotheke",
        "Gesundheitsapotheke",
        "Sonnen Apotheke",
        "Vital Apotheke",
        "Rosen Apotheke",
        "Adler Apotheke",
        "Gutenberg Apotheke",
        "Bären Apotheke"
    )

    internal val demoFavouritePharmacy = OverviewPharmacyData.OverviewPharmacy(
        telematikId = DemoConstants.PHARMACY_TELEMATIK_ID, // actual id, would need change when the pharmacy changes it
        isFavorite = true,
        usageCount = 2,
        lastUsed = Clock.System.now().minus(1.hours),
        pharmacyName = "+1 Apotheke",
        address = "Brunnenstraße 64\n" + "13355 Berlin"
    )
}
