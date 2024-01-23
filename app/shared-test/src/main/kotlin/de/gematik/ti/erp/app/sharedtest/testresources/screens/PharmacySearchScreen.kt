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

package de.gematik.ti.erp.app.sharedtest.testresources.screens

import android.view.KeyEvent
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.pressKey
import androidx.test.espresso.matcher.ViewMatchers
import de.gematik.ti.erp.app.PrescriptionIds
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import de.gematik.ti.erp.app.sharedtest.testresources.config.TestConfig
import de.gematik.ti.erp.app.sharedtest.testresources.config.TestConfig.LoadPrescriptionsTimeout
import de.gematik.ti.erp.app.sharedtest.testresources.config.TestConfig.WaitTimeout1Sec
import de.gematik.ti.erp.app.sharedtest.testresources.utils.awaitDisplay
import de.gematik.ti.erp.app.sharedtest.testresources.utils.hasPharmacyId
import de.gematik.ti.erp.app.sharedtest.testresources.utils.hasPrescriptionId
import de.gematik.ti.erp.app.sharedtest.testresources.utils.sleep

@Suppress("TooManyFunctions")
class PharmacySearchScreen(private val composeRule: ComposeTestRule) :
    SemanticsNodeInteractionsProvider by composeRule {

    fun userSeesPharmacySearchResultScreen() {
        onNodeWithTag(TestTag.PharmacySearch.ResultScreen)
            .assertIsDisplayed()
    }

    fun awaitSearchResults() {
        composeRule.awaitDisplay(LoadPrescriptionsTimeout) {
            onNodeWithTag(TestTag.PharmacySearch.ResultContent)
                .assertIsDisplayed()
                .assert(hasAnyChild(hasTestTag(TestTag.PharmacySearch.PharmacyListEntry)))
        }
        composeRule.sleep(WaitTimeout1Sec)
    }

    fun userClicksOnTestPharmacy() {
        onNodeWithTag(TestTag.PharmacySearch.ResultContent, useUnmergedTree = true)
            .assertIsDisplayed()
            .performScrollToNode(hasPharmacyId(TestConfig.PharmacyTelematikId))
            .onChildren()
            .filterToOne(hasPharmacyId(TestConfig.PharmacyTelematikId))
            .performClick()
    }

    fun userClicksOnPharmacyFromListByName(name: String) {
        composeRule.sleep(WaitTimeout1Sec)
        onNodeWithTag(TestTag.PharmacySearch.ResultContent, useUnmergedTree = true)
            .performScrollToNode(hasAnyDescendant(hasText(name)))
            .onChildren()
            .filterToOne(hasAnyDescendant(hasText(name)))
            .performClick()
    }

    fun userSeesPharmacyOrderOptions() {
        composeRule.sleep(WaitTimeout1Sec)
        onNodeWithTag(TestTag.PharmacySearch.OrderOptions.Content, useUnmergedTree = true)
            .assertExists()
    }

    fun awaitOrderOptionsEnabled() {
        composeRule.sleep(WaitTimeout1Sec)
    }

    fun dismissOrderOptionsBottomSheet() {
        onNodeWithTag(TestTag.PharmacySearch.TextSearchField)
            .assertIsDisplayed()
            .performClick()
    }

    fun userClicksOnOrderByCourierDelivery() {
        onNodeWithTag(TestTag.PharmacySearch.OrderOptions.CourierDeliveryOptionButton)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
    }

    fun userClicksOnOrderByPickUp() {
        onNodeWithTag(TestTag.PharmacySearch.OrderOptions.PickUpOptionButton)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
    }

    fun checkToastMessageWhenOrderOptionClicked() {
        onNodeWithTag(TestTag.PharmacySearch.OrderOptions.ComposeToast, useUnmergedTree = true)
            .assertExists()
    }

    fun checkAndClickNoPrescriptionDialog() {
        onNodeWithTag(TestTag.AlertDialog.ConfirmButton).assertIsDisplayed().performClick()
    }

    fun userClicksOnOrderByMailDelivery() {
        onNodeWithTag(TestTag.PharmacySearch.OrderOptions.MailDeliveryOptionButton)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
    }

    fun userSeesPharmacyOrderSummaryScreen() {
        onNodeWithTag(TestTag.PharmacySearch.OrderSummary.Screen)
            .assertIsDisplayed()
    }

    fun userSeesSendOrderButtonEnabled() {
        // asynchronous process enabling the button
        composeRule.awaitDisplay(WaitTimeout1Sec) {
            onNodeWithTag(TestTag.PharmacySearch.OrderSummary.SendOrderButton)
                .assertIsDisplayed()
                .assertIsEnabled()
        }
    }

    fun userClicksPrescriptionSelection() {
        onNodeWithTag(TestTag.PharmacySearch.OrderSummary.PrescriptionSelectionButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun userSeesPrescriptionSelectionScreen() {
        onNodeWithTag(TestTag.PharmacySearch.OrderPrescriptionSelection.Screen)
            .assertIsDisplayed()
    }

    fun userDeselectsAllPrescriptions() {
        val prescriptionIds = onNodeWithTag(TestTag.PharmacySearch.OrderPrescriptionSelection.Content)
            .fetchSemanticsNode()
            .config[PrescriptionIds]!!

        prescriptionIds.forEach {
            onNodeWithTag(TestTag.PharmacySearch.OrderPrescriptionSelection.Content)
                .performScrollToKey("prescription-$it")
                .onChildren()
                .filterToOne(hasPrescriptionId(it).and(isSelected()))
                .performClick()
        }
    }

    fun userSelectsPrescription(prescription: PrescriptionUseCaseData.Prescription) {
        onNodeWithTag(TestTag.PharmacySearch.OrderPrescriptionSelection.Content)
            .assertIsDisplayed()
            .performScrollToKey("prescription-${prescription.taskId}")
            .onChildren()
            .filterToOne(hasPrescriptionId(prescription.taskId))
            .assertIsNotSelected()
            .assertIsDisplayed()
            .performClick()
            .assertIsSelected()
    }

    fun userClicksBack() {
        onNodeWithTag(TestTag.TopNavigation.BackButton)
            .assertIsDisplayed()
            .performClick()
    }

    fun userClicksSendOrderButton() {
        onNodeWithTag(TestTag.PharmacySearch.OrderSummary.SendOrderButton)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
    }

    fun userSearchesForTestPharmacy() {
        onNodeWithTag(TestTag.PharmacySearch.TextSearchField)
            .performClick()
            .assertIsFocused()
            .performTextInput(TestConfig.PharmacyZoti)

        Espresso.onView(ViewMatchers.isRoot()).perform(pressKey(KeyEvent.KEYCODE_ENTER))
    }

    fun openOrderOptionsAndClickSearchButton() {
        onNodeWithTag(TestTag.PharmacySearch.OverviewScreen, useUnmergedTree = true)
            .assertIsDisplayed()
        onNodeWithTag(TestTag.PharmacySearch.TextSearchButton)
            .performClick()
    }

    fun searchPharmacyByName(name: String) {
        userSearchesForTestPharmacy()
        awaitSearchResults()
        userClicksOnPharmacyFromListByName(name)
        userSeesPharmacyOrderOptions()
        awaitOrderOptionsEnabled()
    }
}
