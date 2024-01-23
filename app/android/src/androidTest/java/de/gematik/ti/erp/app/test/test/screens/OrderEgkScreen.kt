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

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.test.test.TestConfig
import de.gematik.ti.erp.app.test.test.core.awaitDisplay

class OrderEgkScreen(private val composeRule: ComposeTestRule) :
    SemanticsNodeInteractionsProvider by composeRule {

    fun userSeesOrderEgkScreenScreen(timeoutMillis: Long = TestConfig.ScreenChangeTimeout) {
        composeRule.awaitDisplay(timeoutMillis, TestTag.Settings.OrderEgk.OrderEgkScreen)
    }

    fun tapNFCExplanationPageLink() {
        onNodeWithTag(TestTag.Settings.OrderEgk.NFCExplanationPageLink)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun checkIfAtLeastFourInsurerIsVisible() {
        for (i in 0..3) {
            onAllNodesWithTag(TestTag.Settings.InsuranceCompanyList.ListOfInsuranceButtons)[i]
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }

    fun chooseInsurance(insurance: String) {
        onNodeWithText(insurance, substring = true)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }

    fun checkOrderPossibilities(orderPossibility: String) {
        when (orderPossibility) {
            "Keine Bestellmöglichkeit" -> {
                onNodeWithTag(TestTag.Settings.ContactInsuranceCompany.OrderEgkAndPinButton)
                    .assertDoesNotExist()
                onNodeWithTag(TestTag.Settings.ContactInsuranceCompany.OrderPinButton)
                    .assertDoesNotExist()
            }
            "Karten & PIN, Nur PIN" -> {
                onNodeWithTag(TestTag.Settings.ContactInsuranceCompany.OrderPinButton)
                    .assertIsDisplayed()
                    .assertHasClickAction()
                onNodeWithTag(TestTag.Settings.ContactInsuranceCompany.OrderEgkAndPinButton)
                    .assertIsDisplayed()
                    .assertHasClickAction()
                    .performClick()
            }
        }
    }

    fun checkContactPossibilities(contactPossibility: String) {
        if (contactPossibility.contains("Telefon")) {
            onNodeWithTag(TestTag.Settings.ContactInsuranceCompany.TelephoneButton)
                .assertIsDisplayed()
                .assertHasClickAction()
        }
        if (contactPossibility.contains("Webseite")) {
            onNodeWithTag(TestTag.Settings.ContactInsuranceCompany.WebsiteButton)
                .assertIsDisplayed()
                .assertHasClickAction()
        }
        if (contactPossibility.contains("Mail")) {
            onNodeWithTag(TestTag.Settings.ContactInsuranceCompany.MailToButton)
                .assertIsDisplayed()
                .assertHasClickAction()
        }
        if (contactPossibility.contains("Keine Kontaktmöglichkeit")) {
            onNodeWithTag(TestTag.Settings.ContactInsuranceCompany.TelephoneButton)
                .assertDoesNotExist()
            onNodeWithTag(TestTag.Settings.ContactInsuranceCompany.WebsiteButton)
                .assertDoesNotExist()
            onNodeWithTag(TestTag.Settings.ContactInsuranceCompany.MailToButton)
                .assertDoesNotExist()
            onNodeWithTag(TestTag.Settings.ContactInsuranceCompany.NoContactInfoTextBox)
                .assertIsDisplayed()
        }
    }

    fun tapOrderCardAbort() {
        onNodeWithTag(TestTag.TopNavigation.BackButton)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
    }
}
