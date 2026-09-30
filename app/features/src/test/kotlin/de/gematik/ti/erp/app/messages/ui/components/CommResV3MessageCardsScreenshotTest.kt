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

package de.gematik.ti.erp.app.messages.ui.components

import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import de.gematik.ti.erp.app.screenshot.ScreenShotConfigComponent
import de.gematik.ti.erp.app.screenshot.ScreenshotConfig
import de.gematik.ti.erp.app.screenshot.ScreenshotTestDifference
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class CommResV3MessageCardsScreenshotTest(
    private val config: ScreenshotConfig
) {

    companion object {
        private const val SCREENSHOT_SCREEN_HEIGHT = 3500

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): List<ScreenshotConfig> = ScreenShotConfigComponent.entries
    }

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = config.deviceConfig.copy(
            screenHeight = SCREENSHOT_SCREEN_HEIGHT
        ),
        theme = config.theme,
        validateAccessibility = false,
        maxPercentDifference = ScreenshotTestDifference.DIFFERENCE,
        renderingMode = SessionParams.RenderingMode.SHRINK
    )

    @Test
    fun orderedCards() {
        paparazzi.snapshot("ordered_cards") {
            CommResV3OrderedMessageCardsPreview()
        }
    }

    @Test
    fun completedCards() {
        paparazzi.snapshot("completed_cards") {
            CommResV3CompletedMessageCardsPreview()
        }
    }

    @Test
    fun invoiceCards() {
        paparazzi.snapshot("invoice_cards") {
            CommResV3InvoiceCardsPreview()
        }
    }

    @Test
    fun pickUpCodeCards() {
        paparazzi.snapshot("pickup_code_cards") {
            CommResV3PickUpCodeCardsPreview()
        }
    }

    @Test
    fun reservationStateCards() {
        paparazzi.snapshot("reservation_state_cards") {
            CommResV3ReservationStatePreview()
        }
    }

    @Test
    fun linkCards() {
        paparazzi.snapshot("link_cards") {
            CommResV3LinkCardsPreview()
        }
    }

    @Test
    fun sentAndReceivedCards() {
        paparazzi.snapshot("message_cards") {
            CommResV3MessageCardsPreview()
        }
    }

    @Test
    fun paymentCards() {
        paparazzi.snapshot("payment_cards") {
            CommResV3PaymentCardsPreview()
        }
    }

    @Test
    fun paymentInfoCards() {
        paparazzi.snapshot("payment_info_cards") {
            CommResV3PaymentInfoCardsPreview()
        }
    }

    @Test
    fun deliveryCards() {
        paparazzi.snapshot("delivery_cards") {
            CommResV3DeliveryCardsPreview()
        }
    }

    @Test
    fun deliveryStatusCards() {
        paparazzi.snapshot("delivery_status_cards") {
            CommResV3DeliveryStatusCardsPreview()
        }
    }

    @Test
    fun euCards() {
        paparazzi.snapshot("eu_cards") {
            CommResV3EuCardsPreview()
        }
    }
}
