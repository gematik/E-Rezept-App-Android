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

package de.gematik.ti.erp.app.sharedtest.testresources.actions

import androidx.compose.ui.test.junit4.ComposeTestRule
import de.gematik.ti.erp.app.sharedtest.testresources.config.TestConfig
import de.gematik.ti.erp.app.sharedtest.testresources.screens.MainScreen
import de.gematik.ti.erp.app.sharedtest.testresources.screens.OnboardingScreen
import de.gematik.ti.erp.app.sharedtest.testresources.screens.PharmacySearchScreen


// ZoTI - Testapotheken Pharmacies Reference: https://wiki.gematik.de/display/DEV/ZoTI+-+Testapotheken
class PharmacyScreenAction(private val composeRule: ComposeTestRule) {

    private val onboardingScreen by lazy { OnboardingScreen(composeRule) }
    private val mainScreen by lazy { MainScreen(composeRule) }
    private val pharmacySearchScreen by lazy { PharmacySearchScreen(composeRule) }

    /**
     * For Zoti Pharmacy -> 2
     * PickupService -> Enabled
     * */
    fun pickupServiceSuccessTest() {
        navigateToPharmacyOrderOptions(TestConfig.PharmacyZoti02)
        pharmacySearchScreen.userClicksOnOrderByPickUp()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
    }

    /**
     * For Zoti Pharmacy -> 1,5
     * PickupService -> Disabled
     * */
    fun pickupServiceFailTest() {
        navigateToPharmacyOrderOptions(TestConfig.PharmacyZoti01)
        pharmacySearchScreen.userClicksOnOrderByPickUp()
        pharmacySearchScreen.checkToastMessageWhenOrderOptionClicked()

        pharmacySearchScreen.dismissOrderOptionsBottomSheet()

        goToPharmacyOrderOptionsAfterBottomSheetDismissed(TestConfig.PharmacyZoti05)
        pharmacySearchScreen.userClicksOnOrderByPickUp()
        pharmacySearchScreen.checkToastMessageWhenOrderOptionClicked()
    }

    /**
     * For Zoti Pharmacy -> 3
     * CourierDelivery -> Enabled
     * */
    fun courierDeliverySuccessTest() {
        navigateToPharmacyOrderOptions(TestConfig.PharmacyZoti03)
        pharmacySearchScreen.userClicksOnOrderByCourierDelivery()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
    }

    /**
     * For Zoti Pharmacy -> 6
     * CourierDelivery > Disabled
     * */
    fun courierDeliveryFailTest() {
        navigateToPharmacyOrderOptions(TestConfig.PharmacyZoti06)
        pharmacySearchScreen.userClicksOnOrderByCourierDelivery()
        pharmacySearchScreen.checkToastMessageWhenOrderOptionClicked()
    }

    /**
     * For Zoti Pharmacy -> 7
     * MailDelivery > Disabled
     * */
    fun mailDeliveryFailTest() {
        navigateToPharmacyOrderOptions(TestConfig.PharmacyZoti07)
        pharmacySearchScreen.userClicksOnOrderByMailDelivery()
        pharmacySearchScreen.checkToastMessageWhenOrderOptionClicked()
    }

    /**
     * For Zoti Pharmacy -> 12
     * PickupService -> Disabled
     * MailDelivery > Disabled
     * CourierDelivery > Disabled
     * */
    fun pickupServiceMailDeliveryCourierDeliveryFailTest() {
        navigateToPharmacyOrderOptions(TestConfig.PharmacyZoti12)
        pharmacySearchScreen.userClicksOnOrderByPickUp()
        pharmacySearchScreen.checkToastMessageWhenOrderOptionClicked()
        pharmacySearchScreen.awaitOrderOptionsEnabled()
        pharmacySearchScreen.userClicksOnOrderByMailDelivery()
        pharmacySearchScreen.checkToastMessageWhenOrderOptionClicked()
        pharmacySearchScreen.awaitOrderOptionsEnabled()
        pharmacySearchScreen.userClicksOnOrderByCourierDelivery()
        pharmacySearchScreen.checkToastMessageWhenOrderOptionClicked()
    }

    /**
     * For Zoti Pharmacy -> 14, 15
     * PickupService -> Enabled
     * MailDelivery > Enabled
     * */
    fun pickupServiceMailDeliverySuccessTest() {
        navigateToPharmacyOrderOptions(TestConfig.PharmacyZoti14)
        pharmacySearchScreen.userClicksOnOrderByPickUp()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
        pharmacySearchScreen.userClicksOnOrderByMailDelivery()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()

        pharmacySearchScreen.dismissOrderOptionsBottomSheet()

        goToPharmacyOrderOptionsAfterBottomSheetDismissed(TestConfig.PharmacyZoti15)
        pharmacySearchScreen.userClicksOnOrderByPickUp()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
        pharmacySearchScreen.userClicksOnOrderByMailDelivery()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
    }

    /**
     * For Zoti Pharmacy ->4, 16
     * MailDelivery > Enabled
     * */
    fun mailDeliverySuccessTest() {
        navigateToPharmacyOrderOptions(TestConfig.PharmacyZoti04)
        pharmacySearchScreen.userClicksOnOrderByMailDelivery()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()

        pharmacySearchScreen.dismissOrderOptionsBottomSheet()

        goToPharmacyOrderOptionsAfterBottomSheetDismissed(TestConfig.PharmacyZoti16)
        pharmacySearchScreen.userClicksOnOrderByMailDelivery()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
    }

    /**
     * For Zoti Pharmacy -> 13, 17
     * PickupService -> Enabled
     * CourierDelivery > Enabled
     * */
    fun pickupServiceCourierSuccessTest() {
        navigateToPharmacyOrderOptions(TestConfig.PharmacyZoti13)
        pharmacySearchScreen.userClicksOnOrderByPickUp()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
        pharmacySearchScreen.userClicksOnOrderByCourierDelivery()
        pharmacySearchScreen.checkToastMessageWhenOrderOptionClicked()

        pharmacySearchScreen.dismissOrderOptionsBottomSheet()

        goToPharmacyOrderOptionsAfterBottomSheetDismissed(TestConfig.PharmacyZoti17)
        pharmacySearchScreen.userClicksOnOrderByPickUp()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
        pharmacySearchScreen.userClicksOnOrderByCourierDelivery()
        pharmacySearchScreen.checkToastMessageWhenOrderOptionClicked()
    }

    /**
     * For Zoti Pharmacy -> 8, 9, 10, 11 & 18
     * PickupService -> Enabled
     * MailDelivery > Enabled
     * CourierDelivery > Enabled
     * */
    fun pickupServiceMailDeliveryCourierDeliverySuccessTest() {
        navigateToPharmacyOrderOptions(TestConfig.PharmacyZoti08)
        pharmacySearchScreen.userClicksOnOrderByPickUp()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
        pharmacySearchScreen.userClicksOnOrderByMailDelivery()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
        pharmacySearchScreen.userClicksOnOrderByCourierDelivery()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()

        pharmacySearchScreen.dismissOrderOptionsBottomSheet()

        goToPharmacyOrderOptionsAfterBottomSheetDismissed(TestConfig.PharmacyZoti09)
        pharmacySearchScreen.userClicksOnOrderByPickUp()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
        pharmacySearchScreen.userClicksOnOrderByMailDelivery()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
        pharmacySearchScreen.userClicksOnOrderByCourierDelivery()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()

        pharmacySearchScreen.dismissOrderOptionsBottomSheet()

        goToPharmacyOrderOptionsAfterBottomSheetDismissed(TestConfig.PharmacyZoti10)
        pharmacySearchScreen.userClicksOnOrderByPickUp()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
        pharmacySearchScreen.userClicksOnOrderByMailDelivery()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
        pharmacySearchScreen.userClicksOnOrderByCourierDelivery()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()

        pharmacySearchScreen.dismissOrderOptionsBottomSheet()

        goToPharmacyOrderOptionsAfterBottomSheetDismissed(TestConfig.PharmacyZoti11)
        pharmacySearchScreen.userClicksOnOrderByPickUp()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
        pharmacySearchScreen.userClicksOnOrderByMailDelivery()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
        pharmacySearchScreen.userClicksOnOrderByCourierDelivery()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()

        pharmacySearchScreen.dismissOrderOptionsBottomSheet()

        goToPharmacyOrderOptionsAfterBottomSheetDismissed(TestConfig.PharmacyZoti18)
        pharmacySearchScreen.userClicksOnOrderByPickUp()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
        pharmacySearchScreen.userClicksOnOrderByMailDelivery()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
        pharmacySearchScreen.userClicksOnOrderByCourierDelivery()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
    }

    /**
     * For Zoti Pharmacy -> 19
     * MailDelivery > Enabled
     * CourierDelivery > Enabled
     * */
    fun mailDeliveryCourierDeliverySuccessTest() {
        navigateToPharmacyOrderOptions(TestConfig.PharmacyZoti19)
        pharmacySearchScreen.userClicksOnOrderByMailDelivery()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
        pharmacySearchScreen.userClicksOnOrderByCourierDelivery()
        pharmacySearchScreen.checkAndClickNoPrescriptionDialog()
    }

    /**
     * For Zoti Pharmacy -> 20
     * PickupService -> Disabled
     * MailDelivery > Disabled
     * */
    fun pickupServiceMailDeliveryFailTest() {
        navigateToPharmacyOrderOptions(TestConfig.PharmacyZoti20)
        pharmacySearchScreen.userClicksOnOrderByPickUp()
        pharmacySearchScreen.checkToastMessageWhenOrderOptionClicked()
        pharmacySearchScreen.awaitOrderOptionsEnabled()
        pharmacySearchScreen.userClicksOnOrderByMailDelivery()
        pharmacySearchScreen.checkToastMessageWhenOrderOptionClicked()
    }

    private fun navigateToPharmacyOrderOptions(pharmacyName: String) {
        onboardingScreen.tapSkipOnboardingButton()
        mainScreen.openPharmaciesFromBottomBarFromStart()
        pharmacySearchScreen.openOrderOptionsAndClickSearchButton()
        pharmacySearchScreen.searchPharmacyByName(pharmacyName)
    }

    private fun goToPharmacyOrderOptionsAfterBottomSheetDismissed(pharmacyName: String) {
        pharmacySearchScreen.userClicksOnPharmacyFromListByName(pharmacyName)
        pharmacySearchScreen.userSeesPharmacyOrderOptions()
        pharmacySearchScreen.awaitOrderOptionsEnabled()
    }
}
