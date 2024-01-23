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

package de.gematik.ti.erp.app.test.test.scenarios

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.filters.LargeTest
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.test.test.TestConfig
import de.gematik.ti.erp.app.test.test.WithFontScale
import de.gematik.ti.erp.app.test.test.core.TaskCollection
import de.gematik.ti.erp.app.test.test.core.prescription.CommunicationPayloadInbox
import de.gematik.ti.erp.app.test.test.core.prescription.PrescriptionUtils
import de.gematik.ti.erp.app.test.test.core.prescription.SupplyOptionsType
import de.gematik.ti.erp.app.test.test.screens.MainScreen
import de.gematik.ti.erp.app.test.test.screens.OnboardingScreen
import de.gematik.ti.erp.app.test.test.screens.PharmacySearchScreen
import de.gematik.ti.erp.app.test.test.screens.PrescriptionOrderScreen
import de.gematik.ti.erp.app.test.test.screens.PrescriptionsScreen
import org.junit.Rule
import org.junit.Test

@LargeTest
class Orders(fontScale: String) : WithFontScale(fontScale) {
    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 0)
    val intentsRule = IntentsRule()

    private val onboardingScreen by lazy { OnboardingScreen(composeRule) }
    private val mainScreen by lazy { MainScreen(composeRule) }
    private val prescriptionsScreen by lazy { PrescriptionsScreen(composeRule) }
    private val pharmacySearchScreen by lazy { PharmacySearchScreen(composeRule) }
    private val prescriptionOrderScreen by lazy { PrescriptionOrderScreen(composeRule) }
    private val prescriptionUtils by lazy { PrescriptionUtils(composeRule, mainScreen, prescriptionsScreen) }

    @Test
    fun order_prescription() {
        val tasks = TaskCollection.generate(
            1,
            TestConfig.AppDefaultVirtualEgkKvnr,
            composeRule.activity.testWrapper
        )

        fun firstTask() = tasks.taskData.first()

        try {
            onboardingScreen.tapSkipOnboardingButton()
            mainScreen.tapConnectLater()
            mainScreen.tapTooltips()
            prescriptionUtils.loginWithVirtualHealthCardFromMainScreen()
            prescriptionsScreen.awaitPrescriptions()

            prescriptionsScreen.userSeesPZNPrescription(
                data = firstTask().prescription,
                inState = PrescriptionsScreen.PrescriptionState.Redeemable
            )

            mainScreen.userClicksBottomBarPharmacy()

            pharmacySearchScreen.userSeesPharmacyOverviewScreen()

            pharmacySearchScreen.userClicksSearchButton()
            pharmacySearchScreen.userSeesPharmacySearchResultScreen()
            pharmacySearchScreen.userSearchesForTestPharmacy()
            pharmacySearchScreen.awaitSearchResults()
            pharmacySearchScreen.userClicksOnTestPharmacy()

            pharmacySearchScreen.userSeesPharmacyOrderOptions()
            pharmacySearchScreen.awaitOrderOptionsEnabled()
            pharmacySearchScreen.userClicksOnOrderByPickUp()

            pharmacySearchScreen.userSeesPharmacyOrderSummaryScreen()

            // all contact information should be available; therefore the order button is enabled
            pharmacySearchScreen.userSeesSendOrderButtonEnabled()

            pharmacySearchScreen.userClicksPrescriptionSelection()
            pharmacySearchScreen.userSeesPrescriptionSelectionScreen()
            pharmacySearchScreen.userDeselectsAllPrescriptions()
            pharmacySearchScreen.userSelectsPrescription(firstTask().prescription)
            pharmacySearchScreen.userClicksBack()
            pharmacySearchScreen.userSeesPharmacyOrderSummaryScreen()

            pharmacySearchScreen.userSeesSendOrderButtonEnabled()
            pharmacySearchScreen.userClicksSendOrderButton()

            mainScreen.userSeesMainScreen(10_000L)
            prescriptionsScreen.refreshPrescriptions()

            prescriptionsScreen.userSeesPZNPrescription(
                data = firstTask().prescription,
                inState = PrescriptionsScreen.PrescriptionState.WaitForResponse
            )

            // pharmacy accepts task
            tasks.accept(firstTask())
            val message = CommunicationPayloadInbox(
                supplyOptionsType = SupplyOptionsType.Delivery,
                url = "https://www.whatever.de/blablub",
                infoText = "Hey u!",
                pickUpCodeHR = "a1234567890",
                pickUpCodeDMC = "b1234567890"
            )
            tasks.reply(firstTask(), message)

            prescriptionsScreen.refreshPrescriptions()

            prescriptionsScreen.userSeesPZNPrescription(
                data = firstTask().prescription,
                inState = PrescriptionsScreen.PrescriptionState.InProgress
            )

            mainScreen.userClicksBottomBarOrders()

            prescriptionOrderScreen.userSeesOrderScreen()
            prescriptionOrderScreen.awaitOrders()

            prescriptionOrderScreen.userClicksNewestOrder()
            prescriptionOrderScreen.userSeesOrderDetailsScreen()
            prescriptionOrderScreen.userSeesOnePrescription(firstTask().prescription)
            prescriptionOrderScreen.userSeesAndClicksMessage()
            prescriptionOrderScreen.userExpectsMessageContent(message)
            prescriptionOrderScreen.userClicksMessageLink(message)

            prescriptionOrderScreen.userClosesMessageSheetBySwipe()

            prescriptionOrderScreen.userClicksPrescription(firstTask().prescription)
            prescriptionsScreen.userSeesPrescriptionDetails()
            prescriptionsScreen.userClicksClose()
            prescriptionOrderScreen.userClicksBack()

            // dispense medication
            tasks.dispense(firstTask())

            mainScreen.userClicksBottomBarPrescriptions()

            prescriptionsScreen.awaitPrescriptionScreen()
            prescriptionsScreen.refreshPrescriptions()

            prescriptionsScreen.userMissesPrescription(firstTask().prescription)
            prescriptionsScreen.userClicksArchiveButton()
            prescriptionsScreen.awaitArchivedPrescriptions()
            prescriptionsScreen.userSeesPrescriptionInArchive(
                data = firstTask().prescription,
                inState = PrescriptionsScreen.PrescriptionState.Redeemed
            )
        } finally {
            tasks.deleteAll()
        }
    }
}
