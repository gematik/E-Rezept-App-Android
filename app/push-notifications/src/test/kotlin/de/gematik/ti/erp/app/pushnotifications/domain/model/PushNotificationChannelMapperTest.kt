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
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND.
 */

package de.gematik.ti.erp.app.pushnotifications.domain.model

import de.gematik.ti.erp.app.profile.model.ProfilePushNotificationSettings
import de.gematik.ti.erp.app.pushnotifications.model.PushChannel
import de.gematik.ti.erp.app.pushnotifications.model.PushChannelStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class PushNotificationChannelMapperTest {

    @Test
    fun `maps each setting to every corresponding Fachdienst channel`() {
        val actualChannelStatusById = PushNotificationChannelMapper.toPushChannels(
            ProfilePushNotificationSettings(
                newPrescriptionEnabled = true,
                newMessageEnabled = false,
                statusChangeEnabled = true,
                newInvoiceEnabled = false,
                externalAccessEnabled = true
            )
        ).associate { it.id to it.status }

        val expectedChannelStatusById = mapOf(
            "erp.task.activate" to PushChannelStatus.ENABLED,
            "erp.communication.new" to PushChannelStatus.DISABLED,
            "erp.task.abort" to PushChannelStatus.ENABLED,
            "erp.task.accept" to PushChannelStatus.ENABLED,
            "erp.task.close" to PushChannelStatus.ENABLED,
            "erp.task.dispense" to PushChannelStatus.ENABLED,
            "erp.task.reject" to PushChannelStatus.ENABLED,
            "erp.chargeitem.create" to PushChannelStatus.DISABLED,
            "erp.chargeitem.update" to PushChannelStatus.DISABLED,
            "erp.task.vertreter" to PushChannelStatus.ENABLED,
            "erp.eu.prescription.get" to PushChannelStatus.ENABLED,
            "erp.eu.prescription.redeem" to PushChannelStatus.ENABLED,
            "erp.eu.prescription.close" to PushChannelStatus.ENABLED
        )

        assertEquals(expectedChannelStatusById, actualChannelStatusById)
    }

    @Test
    fun `maps the complete remote channel state back to product switches`() {
        val settings = ProfilePushNotificationSettings(
            newPrescriptionEnabled = true,
            newMessageEnabled = false,
            statusChangeEnabled = true,
            newInvoiceEnabled = false,
            externalAccessEnabled = true
        )

        assertEquals(
            settings,
            PushNotificationChannelMapper.toProfileSettings(
                PushNotificationChannelMapper.toPushChannels(settings)
            )
        )
    }

    @Test
    fun `treats missing and mixed channels as disabled`() {
        val channels = listOf(
            PushChannel("erp.task.abort", PushChannelStatus.ENABLED),
            PushChannel("erp.task.accept", PushChannelStatus.DISABLED)
        )

        val settings = PushNotificationChannelMapper.toProfileSettings(channels)

        assertEquals(false, settings.statusChangeEnabled)
        assertEquals(false, settings.newMessageEnabled)
    }
}
