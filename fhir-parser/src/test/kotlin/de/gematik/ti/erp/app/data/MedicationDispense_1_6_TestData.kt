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

package de.gematik.ti.erp.app.data

val bundle_dispense_1_6_simple by lazy { getResourceAsString("/fhir/dispense_parser/1_6_workflow/medication_dispense_1_6_simple.json") }
val bundle_dispense_1_6_digaDeeplink by lazy { getResourceAsString("/fhir/dispense_parser/1_6_workflow/medication_dispense_1_6_diga_deeplink.json") }
val bundle_dispense_1_6_digaName by lazy { getResourceAsString("/fhir/dispense_parser/1_6_workflow/medication_dispense_1_6_diga_name.json") }
val bundle_dispense_1_6_digaNoRedeemCode by lazy { getResourceAsString("/fhir/dispense_parser/1_6_workflow/medication_dispense_1_6_diga_no_redeem_code.json") }
val bundle_dispense_1_6_DosageDaysOfWeek by lazy { getResourceAsString("/fhir/dispense_parser/1_6_workflow/medication_dispense_1_6_dosage_days_of_week.json") }
val bundle_dispense_1_6_DosageInterval by lazy { getResourceAsString("/fhir/dispense_parser/1_6_workflow/medication_dispense_1_6_dosage_interval.json") }
val bundle_dispense_1_6_DosageTime by lazy { getResourceAsString("/fhir/dispense_parser/1_6_workflow/medication_dispense_1_6_dosage_time.json") }
val bundle_dispense_1_6_DosageTimeOfDay by lazy { getResourceAsString("/fhir/dispense_parser/1_6_workflow/medication_dispense_1_6_dosage_time_of_day.json") }
val bundle_dispense_1_6_DosageWeekDay by lazy { getResourceAsString("/fhir/dispense_parser/1_6_workflow/medication_dispense_1_6_dosage_weekday.json") }
