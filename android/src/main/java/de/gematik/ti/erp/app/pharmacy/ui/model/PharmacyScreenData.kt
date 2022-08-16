/*
 * Copyright (c) 2022 gematik GmbH
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

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.fhir.model.PharmacyContacts
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PrescriptionOrder
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.ShippingContact
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData

object PharmacyScreenData {
    @Immutable
    data class DetailScreenState(
        val selectedPharmacy: PharmacyUseCaseData.Pharmacy
    )

    @Immutable
    data class OrderScreenState(
        val activeProfile: ProfilesUseCaseData.Profile,
        val contact: ShippingContact,
        val prescriptions: List<Pair<PrescriptionOrder, Boolean>>,
        val selectedPharmacy: PharmacyUseCaseData.Pharmacy,
        val orderOption: OrderOption
    ) {
        @Stable
        fun anySelected() = prescriptions.any { it.second }
    }

    @Immutable
    enum class OrderOption {
        ReserveInPharmacy,
        CourierDelivery,
        MailDelivery
    }

    val defaultOrderState = OrderScreenState(
        activeProfile = ProfilesUseCaseData.Profile(
            id = "0",
            name = "",
            insuranceInformation = ProfilesUseCaseData.ProfileInsuranceInformation(),
            active = false,
            color = ProfilesData.ProfileColorNames.SPRING_GRAY,
            lastAuthenticated = null,
            ssoTokenScope = null,
            avatarFigure = ProfilesData.AvatarFigure.Initials
        ),
        contact = ShippingContact(
            name = "",
            line1 = "",
            line2 = "",
            postalCodeAndCity = "",
            telephoneNumber = "",
            mail = "",
            deliveryInformation = ""
        ),
        prescriptions = listOf(),
        selectedPharmacy = PharmacyUseCaseData.Pharmacy(
            name = "",
            address = null,
            location = null,
            distance = null,
            contacts = PharmacyContacts(phone = "", mail = "", url = ""),
            provides = listOf(),
            openingHours = null,
            telematikId = "",
            ready = false
        ),
        orderOption = OrderOption.ReserveInPharmacy
    )
}
