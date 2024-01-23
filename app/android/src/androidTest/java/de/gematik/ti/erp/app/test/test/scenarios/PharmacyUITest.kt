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
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.sharedtest.testresources.actions.PharmacyScreenAction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PharmacyUITest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val actions = PharmacyScreenAction(composeRule)

    @Test
    fun pickupServiceSuccessTest() {
        actions.pickupServiceSuccessTest()
    }

    @Test
    fun courierDeliverySuccessTest() {
        actions.courierDeliverySuccessTest()
    }

    @Test
    fun pickupServiceMailDeliverySuccessTest() {
        actions.pickupServiceMailDeliverySuccessTest()
    }

    @Test
    fun mailDeliverySuccessTest() {
        actions.mailDeliverySuccessTest()
    }

    @Test
    fun pickupServiceCourierSuccessTest() {
        actions.pickupServiceCourierSuccessTest()
    }

    @Test
    fun pickupServiceMailDeliveryCourierDeliverySuccessTest() {
        actions.pickupServiceMailDeliveryCourierDeliverySuccessTest()
    }

    @Test
    fun mailDeliveryCourierDeliverySuccessTest() {
        actions.mailDeliveryCourierDeliverySuccessTest()
    }

    @Test
    fun pickupServiceFailTest() {
        actions.pickupServiceFailTest()
    }

    @Test
    fun courierDeliveryFailTest() {
        actions.courierDeliveryFailTest()
    }

    @Test
    fun mailDeliveryFailTest() {
        actions.mailDeliveryFailTest()
    }

    @Test
    fun pickupServiceMailDeliveryCourierDeliveryFailTest() {
        actions.pickupServiceMailDeliveryCourierDeliveryFailTest()
    }

    @Test
    fun pickupServiceMailDeliveryFailTest() {
        actions.pickupServiceMailDeliveryFailTest()
    }
}
