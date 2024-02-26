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

            NavigationRouteNames.OnboardingWelcomeScreen -> "onboarding:welcome"
            NavigationRouteNames.AllowAnalyticsScreen -> "onboarding:allowAnalytics"
            NavigationRouteNames.BiometricScreen -> "onboarding:biometric"
            NavigationRouteNames.TermsOfUseScreen -> "onboarding:termsOfUse"
            NavigationRouteNames.DataProtectionScreen -> "onboarding:dataProtection"
            NavigationRouteNames.OnboardingSelectAppLoginScreen -> "onboarding:selectAppLogin"
            NavigationRouteNames.OnboardingDataProtectionAndTermsOfUseOverviewScreen ->
                "onboarding:termsOfUseAndDataProtection"

            NavigationRouteNames.OnboardingAnalyticsPreviewScreen -> "onboarding:analyticsPreview"

            NavigationRouteNames.MlKit -> "mlKit"
            NavigationRouteNames.MlKitInformationScreen -> "mlKit:information"

            NavigationRouteNames.CardUnlockIntroScreen -> "healthCardPassword:introduction"
            NavigationRouteNames.CardUnlockCanScreen -> "healthCardPassword:can"
            NavigationRouteNames.CardUnlockPukScreen -> "healthCardPassword:puk"
            NavigationRouteNames.CardUnlockOldSecretScreen -> "healthCardPassword:oldPin"
            NavigationRouteNames.CardUnlockNewSecretScreen -> "healthCardPassword:pin"
            NavigationRouteNames.CardUnlockEgkScreen -> "healthCardPassword:readCard"

            NavigationRouteNames.CardWallIntroScreen -> "cardWall:welcome"
            NavigationRouteNames.CardWallCanScreen -> "cardWall:CAN"
            NavigationRouteNames.CardWallPinScreen -> "cardWall:PIN"
            NavigationRouteNames.CardWallSaveCredentialsScreen -> "cardWall:saveCredentials:initial"
            NavigationRouteNames.CardWallSaveCredentialsInfoScreen -> "cardWall:saveCredentials:information"
            NavigationRouteNames.CardWallReadCardScreen -> "cardWall:connect"
            NavigationRouteNames.CardWallExternalAuthenticationScreen -> "cardWall:extAuth"

            NavigationRouteNames.TroubleShootingIntroScreen -> "troubleShooting"
            NavigationRouteNames.TroubleShootingDeviceOnTopScreen -> "troubleShooting:readCardHelp1"
            NavigationRouteNames.TroubleShootingFindNfcPositionScreen -> "troubleShooting:readCardHelp2"
            NavigationRouteNames.TroubleShootingNoSuccessScreen -> "troubleShooting:readCardHelp3"

            NavigationRouteNames.SettingsScreen -> "settings"
            NavigationRouteNames.SettingsAccessibilityScreen -> "settings:accessibility"
            NavigationRouteNames.SettingsProductImprovementScreen -> "settings:productImprovements"
            NavigationRouteNames.SettingsAllowAnalyticsScreen -> "settings:productImprovements:complyTracking"
            NavigationRouteNames.SettingsDeviceSecurityScreen -> "settings:authenticationMethods"
            NavigationRouteNames.SettingsSetAppPasswordScreen -> "settings:authenticationMethods:setAppPassword"
            NavigationRouteNames.SettingsDataProtectionScreen -> "settings:dataProtection"
            NavigationRouteNames.SettingsTermsOfUseScreen -> "settings:termsOfUse"
            NavigationRouteNames.SettingsLegalNoticeScreen -> "settings:legalNotice"
            NavigationRouteNames.SettingsOpenSourceLicencesScreen -> "settings:openSourceLicence"
            NavigationRouteNames.SettingsAdditionalLicencesScreen -> "settings:additionalLicence"

            NavigationRouteNames.PrescriptionScanScreen -> "main:scanner"

            NavigationRouteNames.PharmacyStartScreen -> "pharmacySearch"
            NavigationRouteNames.PharmacyFilterSheetScreen -> "pharmacySearch:filter"
            NavigationRouteNames.PharmacySearchListScreen -> "pharmacySearch:detail"
            NavigationRouteNames.PharmacySearchMapsScreen -> "pharmacySearch:map"
            NavigationRouteNames.PharmacyOrderOverviewScreen -> "redeem:viaTI"
            NavigationRouteNames.PharmacyEditShippingContactScreen -> "redeem:editContactInformation"
            NavigationRouteNames.PharmacyPrescriptionSelectionScreen -> "redeem:prescriptionChooseSubset"

            NavigationRouteNames.SampleOverviewScreen -> null
            NavigationRouteNames.BottomSheetSampleScreen -> null
            NavigationRouteNames.BottomSheetSampleLargeScreen -> null
            NavigationRouteNames.BottomSheetSampleSmallScreen -> null
            NavigationRouteNames.DemoTrackerScreen -> null

            NavigationRouteNames.AppUpdateScreen -> "appUpdate"
        }
}
