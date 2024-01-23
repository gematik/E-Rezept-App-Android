/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.analytics.mapper

import de.gematik.ti.erp.app.navigation.NavigationRouteNames

class ContentSquareMapper {
    @Suppress("ComplexMethod")
    fun map(routeNames: NavigationRouteNames): String? =
        when (routeNames) {
            NavigationRouteNames.DeviceCheckLoadingScreen -> "appsecurity:loading"
            NavigationRouteNames.InsecureDeviceScreen -> "main:deviceSecurity"
            NavigationRouteNames.IntegrityWarningScreen -> "main:integrityWarning"

            NavigationRouteNames.PrescriptionDetailScreen -> "prescriptionDetail"
            NavigationRouteNames.PrescriptionDetailMedicationScreen -> "prescriptionDetail:medication"
            NavigationRouteNames.PrescriptionDetailPatientScreen -> "prescriptionDetail:patient"
            NavigationRouteNames.PrescriptionDetailPractitionerScreen -> "prescriptionDetail:practitioner"
            NavigationRouteNames.PrescriptionDetailOrganizationScreen -> "prescriptionDetail:organization"
            NavigationRouteNames.PrescriptionDetailMedicationOverviewScreen -> "prescriptionDetail:medicationOverview"
            NavigationRouteNames.PrescriptionDetailMedicationIngredientsScreen ->
                "prescriptionDetail:medication_ingredients"
            NavigationRouteNames.PrescriptionDetailAccidentInfoScreen -> "prescriptionDetail:accidentInfo"
            NavigationRouteNames.PrescriptionDetailTechnicalInfoScreen -> "prescriptionDetail:technicalInfo"

            NavigationRouteNames.ProfileScreen -> "profile"
            NavigationRouteNames.ProfileEditPictureScreen -> "profile:editPicture"
            NavigationRouteNames.ProfileImageCropperScreen -> "profile:editPicture:imageCropper"
            NavigationRouteNames.ProfileTokenScreen -> "profile:token"
            NavigationRouteNames.ProfileAuditEventsScreen -> "profile:auditEvents"
            NavigationRouteNames.ProfilePairedDevicesScreen -> "profile:registeredDevices"

            NavigationRouteNames.InvoiceListScreen -> "chargeItem:list"
            NavigationRouteNames.InvoiceDetailsScreen -> "chargeItem:details"
            NavigationRouteNames.InvoiceExpandedDetailsScreen -> "chargeItem:details:expanded"
            NavigationRouteNames.InvoiceLocalCorrectionScreen -> "chargeItem:localCorrection"
            NavigationRouteNames.InvoiceShareScreen -> "chargeItem:share"

            NavigationRouteNames.OnboardingWelcomeScreen -> ""
            NavigationRouteNames.AllowAnalyticsScreen -> ""
            NavigationRouteNames.BiometricScreen -> ""
            NavigationRouteNames.TermsOfUseScreen -> ""
            NavigationRouteNames.DataProtectionScreen -> ""
            NavigationRouteNames.OnboardingSelectAppLoginScreen -> ""
            NavigationRouteNames.OnboardingDataProtectionAndTermsOfUseOverviewScreen -> ""
            NavigationRouteNames.OnboardingAnalyticsPreviewScreen -> ""

            NavigationRouteNames.MlKit -> "mlKit"
            NavigationRouteNames.MlKitInformationScreen -> "mlKit:information"

            NavigationRouteNames.PrescriptionScanScreen -> "main:scanner"

            NavigationRouteNames.SampleOverviewScreen -> null
            NavigationRouteNames.BottomSheetSampleScreen -> null
            NavigationRouteNames.BottomSheetSampleLargeScreen -> null
            NavigationRouteNames.BottomSheetSampleSmallScreen -> null
            NavigationRouteNames.DemoTrackerScreen -> null
        }
}
