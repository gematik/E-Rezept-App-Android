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

package de.gematik.ti.erp.app.test.test.screens

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.test.test.core.await
import de.gematik.ti.erp.app.test.test.core.hasPrescriptionId
import de.gematik.ti.erp.app.test.test.core.prescription.CommunicationPayloadInbox
import de.gematik.ti.erp.app.test.test.core.prescription.Prescription
import de.gematik.ti.erp.app.test.test.core.sleep

class PrescriptionOrderScreen(private val composeRule: ComposeTestRule) :
    SemanticsNodeInteractionsProvider by composeRule {

    fun userSeesOrderScreen() {
        onNodeWithTag(TestTag.Orders.Content)
            .assertIsDisplayed()
    }

    fun awaitOrders() {
        composeRule.await(20_000L) {
            onNodeWithTag(TestTag.Orders.Content)
                .assertIsDisplayed()
                .assert(hasAnyChild(hasTestTag(TestTag.Orders.OrderListItem)))
        }
        composeRule.sleep(2500L)
    }

    fun userClicksNewestOrder() {
        onNodeWithTag(TestTag.Orders.Content)
            .assertIsDisplayed()
            .onChildren()
            .filter(hasTestTag(TestTag.Orders.OrderListItem))
            .onFirst()
            .assertIsDisplayed()
            .performClick()
    }

    fun userSeesOrderDetailsScreen() {
        onNodeWithTag(TestTag.Orders.Details.Screen)
            .assertIsDisplayed()
    }

    fun userSeesOnePrescription(prescription: Prescription) {
        onNodeWithTag(TestTag.Orders.Details.Content)
            .assertIsDisplayed()
            .performScrollToKey("prescriptions")
            .onChildren()
            .filterToOne(hasPrescriptionId(prescription.taskId))
            .assertIsDisplayed()
    }

    fun userSeesAndClicksMessage() {
        onNodeWithTag(TestTag.Orders.Details.Content)
            .assertIsDisplayed()
            .performScrollToKey("prescriptions")
            .onChildren()
            .filterToOne(hasTestTag(TestTag.Orders.Details.MessageListItem))
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun userExpectsMessageContent(message: CommunicationPayloadInbox) {
        onNodeWithTag(TestTag.Orders.Messages.Content)
            .assertIsDisplayed()

        message.infoText?.let {
            onNodeWithTag(TestTag.Orders.Messages.Text)
                .assertIsDisplayed()
                .assertTextContains(message.infoText)
        }
            ?: onNodeWithTag(TestTag.Orders.Messages.Text).assertDoesNotExist()

        message.url?.let {
            onNodeWithTag(TestTag.Orders.Messages.Link)
                .assertIsDisplayed()

            onNodeWithTag(TestTag.Orders.Messages.LinkButton)
                .assertIsDisplayed()
                .assertIsEnabled()
                .assertHasClickAction()
        }
            ?: onNodeWithTag(TestTag.Orders.Messages.Link).assertDoesNotExist()

        if (message.pickUpCodeHR != null || message.pickUpCodeDMC != null) {
            onNodeWithTag(TestTag.Orders.Messages.Code)
                .assertIsDisplayed()

            if (message.pickUpCodeDMC != null) {
                onNodeWithTag(TestTag.Orders.Messages.CodeLabelContent)
                    .assertTextContains(message.pickUpCodeDMC, substring = true)
            } else if (message.pickUpCodeHR != null) {
                onNodeWithTag(TestTag.Orders.Messages.CodeLabelContent)
                    .assertTextContains(message.pickUpCodeHR, substring = true)
            }
        } else {
            onNodeWithTag(TestTag.Orders.Messages.Code).assertDoesNotExist()
        }
    }

    fun userClicksMessageLink(message: CommunicationPayloadInbox) {
        Intents.intending(IntentMatchers.hasData(message.url))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, Intent()))

        onNodeWithTag(TestTag.Orders.Messages.LinkButton)
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
            .performClick()

        Intents.intended(IntentMatchers.hasData(message.url))
    }

    fun userClosesMessageSheetBySwipe() {
        onNodeWithTag(TestTag.Orders.Details.Content)
            .performTouchInput {
                swipeDown()
            }
    }

    fun userClicksPrescription(prescription: Prescription) {
        onNodeWithTag(TestTag.Orders.Details.Content)
            .assertIsDisplayed()
            .performScrollToKey("prescriptions")
            .onChildren()
            .filterToOne(hasPrescriptionId(prescription.taskId))
            .assertIsDisplayed()
            .performClick()
    }

    fun userClicksBack() {
        onNodeWithTag(TestTag.TopNavigation.BackButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }
}
