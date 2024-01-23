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

package de.gematik.ti.erp.app.test.test.functions

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.ui.PrescriptionStateInfo
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.toStartOfDayInUTC
import kotlinx.datetime.Clock
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.days

class ReadyPrescriptionStateInfoTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val now = Clock.System.now()

    @Test
    fun syncedTaskReadyState_27AcceptDaysLeftTest() {
        val acceptUntil = now.plus(27.days).toStartOfDayInUTC()
        val expiresOn = now.plus(90.days).toStartOfDayInUTC()
        val readyState = SyncedTaskData.SyncedTask.Ready(acceptUntil = acceptUntil, expiresOn = expiresOn)
        val expectedText = String.format(context.resources.getString(R.string.prescription_item_accept_days_left), "27")
        composeTestRule.setContent {
            AppTheme {
                PrescriptionStateInfo(state = readyState)
            }
        }
        composeTestRule.onNode(hasText(expectedText)).assertExists()
    }

    @Test
    fun syncedTaskReadyState_2AcceptDaysLeftTest() {
        val acceptUntil = now.plus(2.days).toStartOfDayInUTC()
        val expiresOn = now.plus(65.days).toStartOfDayInUTC()
        val readyState = SyncedTaskData.SyncedTask.Ready(acceptUntil = acceptUntil, expiresOn = expiresOn)
        val expectedText = context.resources.getString(R.string.prescription_item_warning_amber) +
            String.format(context.resources.getString(R.string.prescription_item_two_accept_days_left), "2")
        composeTestRule.setContent {
            AppTheme {
                PrescriptionStateInfo(state = readyState)
            }
        }
        composeTestRule.onNode(hasText(expectedText)).assertExists()
    }

    @Test
    fun syncedTaskReadyState_1AcceptDaysLeftTest() {
        val acceptUntil = now.plus(1.days).toStartOfDayInUTC()
        val expiresOn = now.plus(64.days).toStartOfDayInUTC()
        val readyState = SyncedTaskData.SyncedTask.Ready(acceptUntil = acceptUntil, expiresOn = expiresOn)
        val expectedText = context.resources.getString(R.string.prescription_item_warning_amber) +
            context.resources.getString(R.string.prescription_item_accept_only_tomorrow)
        composeTestRule.setContent {
            AppTheme {
                PrescriptionStateInfo(state = readyState)
            }
        }
        composeTestRule.onNode(hasText(expectedText)).assertExists()
    }

    @Test
    fun syncedTaskReadyState_0AcceptDaysLeftTest() {
        val acceptUntil = now.toStartOfDayInUTC()
        val expiresOn = now.plus(63.days).toStartOfDayInUTC()
        val readyState = SyncedTaskData.SyncedTask.Ready(acceptUntil = acceptUntil, expiresOn = expiresOn)
        val expectedText = String.format(context.resources.getString(R.string.prescription_item_warning_amber)) +
            context.resources.getString(R.string.prescription_item_accept_only_today)
        composeTestRule.setContent {
            AppTheme {
                PrescriptionStateInfo(state = readyState)
            }
        }

        composeTestRule.onNode(hasText(expectedText)).assertExists()
    }

    @Test
    fun syncedTaskReadyState_62ExpiryDaysLeftTest() {
        val acceptUntil = now.minus(1.days).toStartOfDayInUTC()
        val expiresOn = now.plus(62.days).toStartOfDayInUTC()
        val readyState = SyncedTaskData.SyncedTask.Ready(acceptUntil = acceptUntil, expiresOn = expiresOn)
        val expectedText =
            String.format(context.resources.getString(R.string.prescription_item_expiration_days_left), "62")
        composeTestRule.setContent {
            AppTheme {
                PrescriptionStateInfo(state = readyState)
            }
        }
        composeTestRule.onNode(hasText(expectedText)).assertExists()
    }

    @Test
    fun syncedTaskReadyState_2ExpiryDaysLeftTest() {
        val acceptUntil = now.minus(61.days).toStartOfDayInUTC()
        val expiresOn = now.plus(2.days).toStartOfDayInUTC()
        val readyState = SyncedTaskData.SyncedTask.Ready(acceptUntil = acceptUntil, expiresOn = expiresOn)
        val expectedText = context.resources.getString(R.string.prescription_item_warning_amber) +
            String.format(context.resources.getString(R.string.prescription_item_two_expiration_days_left), "2")
        composeTestRule.setContent {
            AppTheme {
                PrescriptionStateInfo(state = readyState)
            }
        }
        composeTestRule.onNode(hasText(expectedText)).assertExists()
    }

    @Test
    fun syncedTaskReadyState_1ExpiryDaysLeftTest() {
        val acceptUntil = now.minus(62.days).toStartOfDayInUTC()
        val expiresOn = now.plus(1.days).toStartOfDayInUTC()
        val readyState = SyncedTaskData.SyncedTask.Ready(acceptUntil = acceptUntil, expiresOn = expiresOn)
        val expectedText = context.resources.getString(R.string.prescription_item_warning_amber) +
            context.resources.getString(R.string.prescription_item_expiration_only_tomorrow)
        composeTestRule.setContent {
            AppTheme {
                PrescriptionStateInfo(state = readyState)
            }
        }
        composeTestRule.onNode(hasText(expectedText)).assertExists()
    }

    @Test
    fun syncedTaskReadyState_0ExpiryDaysLeftTest() {
        val acceptUntil = now.minus(63.days).toStartOfDayInUTC()
        val expiresOn = now.toStartOfDayInUTC()
        val readyState = SyncedTaskData.SyncedTask.Ready(acceptUntil = acceptUntil, expiresOn = expiresOn)
        val expectedText = context.resources.getString(R.string.prescription_item_warning_amber) +
            context.resources.getString(R.string.prescription_item_expiration_only_today)
        composeTestRule.setContent {
            AppTheme {
                PrescriptionStateInfo(state = readyState)
            }
        }
        composeTestRule.onNode(hasText(expectedText)).assertExists()
    }
}
