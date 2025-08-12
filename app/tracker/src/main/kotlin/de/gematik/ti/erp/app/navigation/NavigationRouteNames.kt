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

package de.gematik.ti.erp.app.navigation

enum class NavigationRouteNames(
    val doNoTrack: Boolean = false
) {
    // App-security
    DeviceCheckLoadingScreen,
    InsecureDeviceScreen,
    IntegrityWarningScreen,

    // Profile
    ProfileScreen,
    ProfileEditPictureScreen,
    ProfileImageCropperScreen,
    ProfileImageEmojiScreen,
    ProfileImageCameraScreen,
    ProfileAuditEventsScreen,
    ProfilePairedDevicesScreen,
    ProfileEditPictureBottomSheetScreen,
    ProfileEditNameBottomSheetScreen,
    ProfileAddNameBottomSheetScreen,
    ProfileChangeInsuranceTypeBottomSheetScreen,

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
    PrescriptionDetailSelfPayerPrescriptionBottomSheetScreen,
    PrescriptionDetailAdditionalFeeNotExemptBottomSheetScreen,
    PrescriptionDetailAdditionalFeeExemptBottomSheetScreen,
    PrescriptionDetailFailureBottomSheetScreen,
    PrescriptionDetailScannedBottomSheetScreen,
    PrescriptionDetailDirectAssignmentBottomSheetScreen,
    PrescriptionDetailSubstitutionAllowedBottomSheetScreen,
    PrescriptionDetailSubstitutionNotAllowedBottomSheetScreen,
    PrescriptionDetailEmergencyFeeExemptBottomSheetScreen,
    PrescriptionDetailEmergencyFeeNotExemptBottomSheetScreen,
    PrescriptionDetailHowLongValidBottomSheetScreen,

    // Onboarding
    OnboardingWelcomeScreen,
    OnboardingSelectAppLoginScreen,
    OnboardingPasswordAuthenticationScreen,
    OnboardingDataProtectionAndTermsOfUseOverviewScreen,
    TermsOfUseScreen, // TODO is duplicate with Settings
    DataProtectionScreen, // TODO is duplicate with Settings
    OnboardingAnalyticsPreviewScreen,

    // Analytics
    AllowAnalyticsScreen, // TODO is duplicate with Settings

    // Settings
    SettingsScreen,
    SettingsProductImprovementScreen,
    SettingsAllowAnalyticsScreen,
    SettingsAppSecurityScreen,
    SettingsSetAppPasswordScreen,
    SettingsDataProtectionScreen,
    SettingsTermsOfUseScreen,
    SettingsLegalNoticeScreen,
    SettingsOpenSourceLicencesScreen,
    SettingsAdditionalLicencesScreen,
    SettingsLanguageScreen,

    // MlKit
    MlKit,
    MlKitInformationScreen,

    // Prescriptions
    PrescriptionListScreen,
    PrescriptionsArchiveScreen,
    PrescriptionScanScreen,
    CardWallSelectInsuranceTypeBottomSheetScreen,
    GrantConsentBottomSheetScreen,

    // Messages
    MessageListScreen,
    MessageDetailScreen,
    MessageBottomSheetScreen,

    // showcase screen
    BottomSheetShowcaseScreen,

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
    CardWallGidListScreen,
    CardWallGidHelpScreen,
    CardWallScannerScreen,

    // TroubleShooting
    TroubleShootingIntroScreen,
    TroubleShootingDeviceOnTopScreen,
    TroubleShootingFindNfcPositionScreen,
    TroubleShootingNoSuccessScreen,

    // App Update
    AppUpdateScreen,

    // Pharmacy
    PharmacyStartScreen,
    PharmacyStartScreenModal,
    PharmacyFilterSheetScreen,
    PharmacySearchListScreen,
    PharmacySearchMapsScreen,
    PharmacyDetailsFromMessageScreen,
    RedeemOrderOverviewScreen,
    RedeemEditShippingContactScreen,
    RedeemPrescriptionSelectionScreen,
    PharmacyDetailsFromPharmacyScreen,

    // Redeem
    HowToRedeemScreen,
    RedeemPrescriptionSelection,
    RedeemLocal,
    RedeemOnline,

    // OrderHealthCard
    OrderHealthCardSelectInsuranceCompanyScreen,
    OrderHealthCardSelectOptionScreen,
    OrderHealthCardSelectMethodScreen,

    // MedicationPlan
    MedicationPlanNotificationScreen,
    MedicationPlanScheduleListScreen,
    MedicationPlanScheduleDetailScreen,
    MedicationPlanDosageInstructionBottomSheetScreen,
    MedicationPlanScheduleDurationAndIntervalScreen,

    // UserAuthentication
    UserAuthenticationScreen,

    // Digas
    DigasMainScreen,
    DigasValidityBottomSheetScreen,
    DigasSupportBottomSheetScreen,
    DigaContributionInfoSheetScreen,
    DigasDescriptionScreen,
    DigaFeedbackPromptScreen,
    InsuranceSearchListScreen,

    // Translations
    TranslationConsentBottomSheetScreen,
    TranslationSettingsScreen,
    TranslationPickLanguageScreen
}
