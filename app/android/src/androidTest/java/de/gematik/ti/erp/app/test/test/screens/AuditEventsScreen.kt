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
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import de.gematik.ti.erp.app.test.test.TestConfig
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.test.test.core.awaitDisplay

class AuditEventsScreen(private val composeRule: ComposeTestRule) :
    SemanticsNodeInteractionsProvider by composeRule {

    fun userSeesAuditEventsScreen(timeoutMillis: Long = TestConfig.ScreenChangeTimeout) {
        composeRule.awaitDisplay(timeoutMillis, TestTag.Profile.AuditEvents.AuditEventsScreen)
    }

    fun checkAuditEventsDoNotExist() {
        onNodeWithTag(TestTag.Profile.AuditEvents.NoAuditEventHeader)
            .assertIsDisplayed()
        onNodeWithTag(TestTag.Profile.AuditEvents.NoAuditEventInfo)
            .assertIsDisplayed()
    }

    fun checkAuditEventsExist() {
        onAllNodesWithTag(TestTag.Profile.AuditEvents.AuditEvent)[0]
            .assertIsDisplayed()
    }

    fun checkNoAuditEventsHeaderAndInfoDoesNotExist() {
        onNodeWithTag(TestTag.Profile.AuditEvents.NoAuditEventHeader)
            .assertDoesNotExist()
        onNodeWithTag(TestTag.Profile.AuditEvents.NoAuditEventInfo)
            .assertDoesNotExist()
    }
}
