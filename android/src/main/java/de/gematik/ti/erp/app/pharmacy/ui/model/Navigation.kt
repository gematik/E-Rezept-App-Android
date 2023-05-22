/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.pharmacy.ui.model

import de.gematik.ti.erp.app.Route
import de.gematik.ti.erp.app.prescription.detail.ui.model.PopUpName

object PharmacyNavigationScreens {
    object StartSearch : Route("pharmacySearch")
    object List : Route("pharmacySearch_detail")
    object Maps : Route("pharmacySearch_map")
    object OrderOverview : Route("redeem_viaTI") // TODO change when redeem_viaAVS is available
    object EditShippingContact : Route("redeem_editContactInformation")
    object PrescriptionSelection : Route("redeem_prescriptionChooseSubset")
}

object PharmacySearchPopUpNames {
    object PharmacySelected : PopUpName("pharmacySearch_selectedPharmacy")
    object FilterSelected : PopUpName("pharmacySearch_filter")
}
