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

package de.gematik.ti.erp.app.navigation

enum class NavigationRouteNames {
    // App-security
    DeviceCheckLoadingScreen,
    InsecureDeviceScreen,
    IntegrityWarningScreen,

    // Profile
    ProfileScreen,
    ProfileEditPictureScreen,
    ProfileImageCropperScreen,
    ProfileTokenScreen,
    ProfileAuditEventsScreen,
    ProfilePairedDevicesScreen,

    // Invoices/ pkv
    InvoiceListScreen,
    InvoiceDetailsScreen,
    InvoiceExpandedDetailsScreen,
    InvoiceLocalCorrectionScreen,
    InvoiceShareScreen,

    // Prescription-detail
    PrescriptionDetailScreen,
    PrescriptionDetailMedicationScreen,
    PrescriptionDetailPatientScreen,
    PrescriptionDetailPractitionerScreen,
    PrescriptionDetailOrganizationScreen,
    PrescriptionDetailMedicationOverviewScreen,
    PrescriptionDetailMedicationIngredientsScreen,
    PrescriptionDetailAccidentInfoScreen,
    PrescriptionDetailTechnicalInfoScreen,

    // Onboarding
    OnboardingWelcomeScreen,
    OnboardingSelectAppLoginScreen,
    OnboardingDataProtectionAndTermsOfUseOverviewScreen,
    TermsOfUseScreen, // TODO is duplicate with Settings
    DataProtectionScreen, // TODO is duplicate with Settings
    OnboardingAnalyticsPreviewScreen,

    // Biometric
    BiometricScreen,

    // Analytics
    AllowAnalyticsScreen, // TODO is duplicate with Settings

    // Settings
    SettingsScreen,
    SettingsAccessibilityScreen,
    SettingsProductImprovementScreen,
    SettingsAllowAnalyticsScreen,
    SettingsDeviceSecurityScreen,
    SettingsSetAppPasswordScreen,
    SettingsDataProtectionScreen,
    SettingsTermsOfUseScreen,
    SettingsLegalNoticeScreen,
    SettingsOpenSourceLicencesScreen,
    SettingsAdditionalLicencesScreen,

    // MlKit
    MlKit,
    MlKitInformationScreen,

    // Main
    PrescriptionScanScreen,

    // sample screen
    SampleOverviewScreen,
    BottomSheetSampleScreen,
    BottomSheetSampleSmallScreen,
    BottomSheetSampleLargeScreen,

    // tracker
    DemoTrackerScreen,

    // CardUnlock
    CardUnlockIntroScreen,
    CardUnlockCanScreen,
    CardUnlockPukScreen,
    CardUnlockOldSecretScreen,
    CardUnlockNewSecretScreen,
    CardUnlockEgkScreen,

    // CardWall
    CardWallIntroScreen,
    CardWallCanScreen,
    CardWallPinScreen,
    CardWallSaveCredentialsScreen,
    CardWallSaveCredentialsInfoScreen,
    CardWallReadCardScreen,
    CardWallExternalAuthenticationScreen,

    // TroubleShooting
    TroubleShootingIntroScreen,
    TroubleShootingDeviceOnTopScreen,
    TroubleShootingFindNfcPositionScreen,
    TroubleShootingNoSuccessScreen,

    // App Update
    AppUpdateScreen,

    // Pharmacy
    PharmacyStartScreen,
    PharmacyFilterSheetScreen,
    PharmacySearchListScreen,
    PharmacySearchMapsScreen,
    PharmacyOrderOverviewScreen,
    PharmacyEditShippingContactScreen,
    PharmacyPrescriptionSelectionScreen
}
