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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik GmbH find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.pushnotifications.domain.model

import de.gematik.ti.erp.app.profile.model.ProfilePushNotificationSettings
import de.gematik.ti.erp.app.pushnotifications.model.PushChannel
import de.gematik.ti.erp.app.pushnotifications.model.PushChannelStatus

/** Maps profile notification settings to Fachdienst push channels and back. */
object PushNotificationChannelMapper {

    fun toPushChannels(settings: ProfilePushNotificationSettings): List<PushChannel> = buildList {
        addPushChannels(newPrescriptionChannelIds, settings.newPrescriptionEnabled)
        addPushChannels(newMessageChannelIds, settings.newMessageEnabled)
        addPushChannels(prescriptionStatusChangeChannelIds, settings.statusChangeEnabled)
        addPushChannels(newInvoiceChannelIds, settings.newInvoiceEnabled)
        addPushChannels(externalAccessChannelIds, settings.externalAccessEnabled)
    }

    /** Treats incomplete or mixed channel groups as disabled. */
    fun toProfileSettings(pushChannels: List<PushChannel>): ProfilePushNotificationSettings {
        val channelStatusById = pushChannels.associate { channel -> channel.id to channel.status }

        return ProfilePushNotificationSettings(
            newPrescriptionEnabled = channelStatusById.areAllEnabled(newPrescriptionChannelIds),
            newMessageEnabled = channelStatusById.areAllEnabled(newMessageChannelIds),
            statusChangeEnabled = channelStatusById.areAllEnabled(prescriptionStatusChangeChannelIds),
            newInvoiceEnabled = channelStatusById.areAllEnabled(newInvoiceChannelIds),
            externalAccessEnabled = channelStatusById.areAllEnabled(externalAccessChannelIds)
        )
    }

    private fun MutableList<PushChannel>.addPushChannels(
        channelIds: List<String>,
        isEnabled: Boolean
    ) {
        val channelStatus = if (isEnabled) PushChannelStatus.ENABLED else PushChannelStatus.DISABLED
        channelIds.forEach { channelId ->
            add(PushChannel(id = channelId, status = channelStatus))
        }
    }

    private fun Map<String, String>.areAllEnabled(expectedChannelIds: List<String>): Boolean =
        expectedChannelIds.all { channelId ->
            this[channelId] == PushChannelStatus.ENABLED
        }

    private val newPrescriptionChannelIds = listOf("erp.task.activate")
    private val newMessageChannelIds = listOf("erp.communication.new")
    private val prescriptionStatusChangeChannelIds = listOf(
        "erp.task.abort",
        "erp.task.accept",
        "erp.task.close",
        "erp.task.dispense",
        "erp.task.reject"
    )
    private val newInvoiceChannelIds = listOf(
        "erp.chargeitem.create",
        "erp.chargeitem.update"
    )
    private val externalAccessChannelIds = listOf(
        "erp.task.vertreter",
        "erp.eu.prescription.get",
        "erp.eu.prescription.redeem",
        "erp.eu.prescription.close"
    )
}

internal fun ProfilePushNotificationSettings.hasEnabledPushNotificationSetting(): Boolean = listOf(
    newPrescriptionEnabled,
    newMessageEnabled,
    statusChangeEnabled,
    newInvoiceEnabled,
    externalAccessEnabled
).any { it }
