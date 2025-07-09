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

package de.gematik.ti.erp.app.analytics.mapper

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.navigation.NavigationRouteNames

@Requirement(
    "A_19094-01#3",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Every screen name has its own tracking string which is statically typed and only that is sent."
)
class ContentSquareScreenMapper {
    @Suppress("ComplexMethod")
    fun map(routeName: NavigationRouteNames): String? =
        if (routeName.doNoTrack) {
            null
        } else {
            when (routeName) {
                NavigationRouteNames.DeviceCheckLoadingScreen -> null
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

                NavigationRouteNames.PrescriptionDetailSelfPayerPrescriptionBottomSheetScreen ->
                    "prescriptionDetail:selfPayerPrescriptionBottomSheet"

                NavigationRouteNames.PrescriptionDetailAdditionalFeeNotExemptBottomSheetScreen ->
                    "prescriptionDetail:additionalFeeNotExemptBottomSheet"

                NavigationRouteNames.PrescriptionDetailAdditionalFeeExemptBottomSheetScreen ->
                    "prescriptionDetail:additionalFeeExemptBottomSheet"

                NavigationRouteNames.PrescriptionDetailFailureBottomSheetScreen -> "prescriptionDetail:failureBottomSheet"
                NavigationRouteNames.PrescriptionDetailScannedBottomSheetScreen -> "prescriptionDetail:scannedBottomSheet"
                NavigationRouteNames.PrescriptionDetailDirectAssignmentBottomSheetScreen -> "prescriptionDetail:directAssignmentBottomSheet"
                NavigationRouteNames.PrescriptionDetailSubstitutionAllowedBottomSheetScreen ->
                    "prescriptionDetail:substitutionAllowedBottomSheet"

                NavigationRouteNames.PrescriptionDetailSubstitutionNotAllowedBottomSheetScreen ->
                    "prescriptionDetail:substitutionNotAllowedBottomSheet"

                NavigationRouteNames.PrescriptionDetailEmergencyFeeExemptBottomSheetScreen ->
                    "prescriptionDetail:emergencyFeeExemptBottomSheet"

                NavigationRouteNames.PrescriptionDetailEmergencyFeeNotExemptBottomSheetScreen ->
                    "prescriptionDetail:emergencyFeeNotExemptBottomSheet"

                NavigationRouteNames.PrescriptionDetailHowLongValidBottomSheetScreen -> "prescriptionDetail:howLongValidBottomSheet"

                NavigationRouteNames.ProfileScreen -> "profile"
                NavigationRouteNames.ProfileEditPictureScreen -> "profile:editPicture:fullscreen"
                NavigationRouteNames.ProfileImageCropperScreen -> "profile:editPicture:imageCropper"
                NavigationRouteNames.ProfileImageEmojiScreen -> "profile:editPicture:imageEmoji"
                NavigationRouteNames.ProfileImageCameraScreen -> "profile:editPicture:imageCamera"
                NavigationRouteNames.ProfileAuditEventsScreen -> "profile:auditEvents"
                NavigationRouteNames.ProfilePairedDevicesScreen -> "profile:registeredDevices"
                NavigationRouteNames.ProfileEditPictureBottomSheetScreen -> "main:editProfilePicture"
                NavigationRouteNames.ProfileEditNameBottomSheetScreen -> "main:editName"
                NavigationRouteNames.ProfileAddNameBottomSheetScreen -> "main:createProfile"

                NavigationRouteNames.InvoiceListScreen -> "chargeItem:list"
                NavigationRouteNames.InvoiceDetailsScreen -> "chargeItem:details"
                NavigationRouteNames.InvoiceExpandedDetailsScreen -> "chargeItem:details:expanded"
                NavigationRouteNames.InvoiceLocalCorrectionScreen -> "chargeItem:localCorrection"
                NavigationRouteNames.InvoiceShareScreen -> "chargeItem:share"

                NavigationRouteNames.OnboardingWelcomeScreen -> "onboarding:welcome"
                NavigationRouteNames.AllowAnalyticsScreen -> "onboarding:allowAnalytics"
                NavigationRouteNames.TermsOfUseScreen -> "onboarding:termsOfUse"
                NavigationRouteNames.DataProtectionScreen -> "onboarding:dataProtection"
                NavigationRouteNames.OnboardingSelectAppLoginScreen -> "onboarding:selectAppLogin"
                NavigationRouteNames.OnboardingPasswordAuthenticationScreen -> "onboarding:passwordAuthentication"
                NavigationRouteNames.OnboardingDataProtectionAndTermsOfUseOverviewScreen ->
                    "onboarding:termsOfUseAndDataProtection"

                NavigationRouteNames.OnboardingAnalyticsPreviewScreen -> "onboarding:analyticsPreview"

                NavigationRouteNames.MlKit -> "mlKit:intro"
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
                NavigationRouteNames.CardWallGidListScreen -> "cardWall:extAuth"
                NavigationRouteNames.CardWallGidHelpScreen -> "cardWall:extAuth:help"

                NavigationRouteNames.TroubleShootingIntroScreen -> "troubleShooting"
                NavigationRouteNames.TroubleShootingDeviceOnTopScreen -> "troubleShooting:readCardHelp1"
                NavigationRouteNames.TroubleShootingFindNfcPositionScreen -> "troubleShooting:readCardHelp2"
                NavigationRouteNames.TroubleShootingNoSuccessScreen -> "troubleShooting:readCardHelp3"

                NavigationRouteNames.SettingsScreen -> "settings"
                NavigationRouteNames.SettingsProductImprovementScreen -> "settings:productImprovements"
                NavigationRouteNames.SettingsAllowAnalyticsScreen -> "settings:productImprovements:complyTracking"
                NavigationRouteNames.SettingsAppSecurityScreen -> "settings:authenticationMethods"
                NavigationRouteNames.SettingsSetAppPasswordScreen -> "settings:authenticationMethods:setAppPassword"
                NavigationRouteNames.SettingsDataProtectionScreen -> "settings:dataProtection"
                NavigationRouteNames.SettingsTermsOfUseScreen -> "settings:termsOfUse"
                NavigationRouteNames.SettingsLegalNoticeScreen -> "settings:legalNotice"
                NavigationRouteNames.SettingsOpenSourceLicencesScreen -> "settings:openSourceLicence"
                NavigationRouteNames.SettingsAdditionalLicencesScreen -> "settings:additionalLicence"
                NavigationRouteNames.SettingsLanguageScreen -> "settings:language"

                NavigationRouteNames.PrescriptionListScreen -> "main"
                NavigationRouteNames.PrescriptionsArchiveScreen -> "main:prescriptionArchive"
                NavigationRouteNames.PrescriptionScanScreen -> "main:scanner"
                NavigationRouteNames.WelcomeDrawerBottomSheetScreen -> "main:welcomeDrawer"
                NavigationRouteNames.GrantConsentBottomSheetScreen -> "main:grantConsent"

                NavigationRouteNames.MessageListScreen -> "orders"
                NavigationRouteNames.MessageDetailScreen -> "orders:details"
                NavigationRouteNames.MessageBottomSheetScreen -> "orders:details:reply"
                NavigationRouteNames.PharmacyDetailsFromMessageScreen -> "orders:details:selectedPharmacy"

                NavigationRouteNames.PharmacyStartScreen -> "pharmacySearch"
                NavigationRouteNames.PharmacyStartScreenModal -> "pharmacySearch"
                NavigationRouteNames.PharmacyFilterSheetScreen -> "pharmacySearch:filter"
                NavigationRouteNames.PharmacySearchListScreen -> "pharmacySearch:detail"
                NavigationRouteNames.PharmacySearchMapsScreen -> "pharmacySearch:map"
                NavigationRouteNames.PharmacyDetailsFromPharmacyScreen -> "pharmacySearch:selectedPharmacy"

                NavigationRouteNames.BottomSheetShowcaseScreen -> null
                NavigationRouteNames.DemoTrackerScreen -> null

                NavigationRouteNames.AppUpdateScreen -> "appUpdate"
                NavigationRouteNames.RedeemMethodSelection -> "redeem:methodSelection"
                NavigationRouteNames.RedeemPrescriptionSelection -> "redeem:prescriptionChooseSubset"
                NavigationRouteNames.RedeemLocal -> "redeem:matrixcode"
                NavigationRouteNames.RedeemOnline -> "redeem:prescriptionAllOrSelection"
                NavigationRouteNames.SuccessScreen -> "medicationplan:success"
                NavigationRouteNames.DosageInfoScreen -> "medicationplan:dosageInfo"
                NavigationRouteNames.ScheduleListScreen -> "medicationplan:scheduleList"
                NavigationRouteNames.ScheduleScreen -> "medicationplan:schedule"
                NavigationRouteNames.ScheduleDateRangeScreen -> "medicationplan:scheduleDateRange"
                NavigationRouteNames.RedeemOrderOverviewScreen -> "redeem:viaTI"
                NavigationRouteNames.RedeemEditShippingContactScreen -> "redeem:editContactInformation"
                NavigationRouteNames.RedeemPrescriptionSelectionScreen -> "redeem:prescriptionChooseSubset"

                NavigationRouteNames.OrderHealthCardSelectInsuranceCompanyScreen -> "contactInsuranceCompany"
                NavigationRouteNames.OrderHealthCardSelectOptionScreen -> "contactInsuranceCompany:selectReason"
                NavigationRouteNames.OrderHealthCardSelectMethodScreen -> "contactInsuranceCompany:selectMethod"

                NavigationRouteNames.UserAuthenticationScreen -> "userAuthentication"

                NavigationRouteNames.DigasMainScreen -> "digas:detail"
                NavigationRouteNames.DigasValidityBottomSheetScreen -> "digas:validity"
                NavigationRouteNames.DigasSupportBottomSheetScreen -> "digas:support"
                NavigationRouteNames.DigasDescriptionScreen -> "digas:description"
                NavigationRouteNames.DigaFeedbackPromptScreen -> "digas:feedback"
                NavigationRouteNames.InsuranceSearchListScreen -> "digas:insuranceSearch"
                NavigationRouteNames.TranslationConsentBottomSheetScreen -> "translation:consent"
                NavigationRouteNames.TranslationSettingsScreen -> "translation:settings"
                NavigationRouteNames.TranslationPickLanguageScreen -> "translation:pickLanguage"
            }
        }
}
