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

package de.gematik.ti.erp.app.prescription.detail.ui.model

import de.gematik.ti.erp.app.Route

object PrescriptionDetailsNavigationScreens {
    object Overview : Route("overview")
    object MedicationOverview : Route("medicationOverview")
    object Medication : Route("medication")
    object Patient : Route("patient")
    object Prescriber : Route("prescriber")
    object Organization : Route("organization")
    object Accident : Route("accident")
    object TechnicalInformation : Route("technicalInformation")
    object Ingredient : Route("ingredient")
}
