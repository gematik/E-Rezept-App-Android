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

package de.gematik.ti.erp.app.messages.ui.model

import kotlinx.datetime.Instant

sealed class EuOrderMessageUiModel : MessageUiModel {
    abstract val id: String

    abstract val underlyingEventIds: List<String>
    abstract val orderId: String
    abstract val accessCode: String
    abstract val dateTimeString: String
    abstract val timestamp: Instant?
    abstract val isFirst: Boolean
    abstract val isLast: Boolean
    abstract val showButtons: Boolean
    abstract val taskIds: List<String>

    abstract val prescriptionNames: List<String>
    abstract val countryCode: String

    abstract val title: String
    abstract val description: String?

    abstract val isUnread: Boolean

    abstract val isRevoked: Boolean

    data class AccessCodeCreated(
        override val id: String,
        override val underlyingEventIds: List<String>,
        override val orderId: String,
        override val accessCode: String,
        override val dateTimeString: String,
        override val timestamp: Instant?,
        override val isFirst: Boolean = false,
        override val isLast: Boolean = false,
        override val showButtons: Boolean,
        override val prescriptionNames: List<String> = emptyList(),
        override val taskIds: List<String>,
        override val countryCode: String,
        override val title: String,
        val flagEmoji: String,
        override val description: String?,
        override val isUnread: Boolean,
        override val isRevoked: Boolean
    ) : EuOrderMessageUiModel()

    data class AccessCodeRecreated(
        override val id: String,
        override val underlyingEventIds: List<String>,
        override val orderId: String,
        override val accessCode: String,
        override val dateTimeString: String,
        override val timestamp: Instant?,
        override val isFirst: Boolean = false,
        override val isLast: Boolean = false,
        override val showButtons: Boolean,
        override val prescriptionNames: List<String> = emptyList(),
        override val taskIds: List<String>,
        override val countryCode: String,
        override val title: String,
        override val description: String?,
        override val isUnread: Boolean,
        override val isRevoked: Boolean
    ) : EuOrderMessageUiModel()

    data class TaskRedeemed(
        override val id: String,
        override val underlyingEventIds: List<String>,
        override val orderId: String,
        override val accessCode: String,
        override val dateTimeString: String,
        override val timestamp: Instant?,
        override val isFirst: Boolean = false,
        override val isLast: Boolean = false,
        override val showButtons: Boolean,
        override val prescriptionNames: List<String> = emptyList(),
        override val taskIds: List<String>,
        override val countryCode: String,
        override val title: String,
        override val description: String?,
        override val isUnread: Boolean,
        override val isRevoked: Boolean = false
    ) : EuOrderMessageUiModel()

    data class TaskAdded(
        override val id: String,
        override val underlyingEventIds: List<String>,
        override val orderId: String,
        override val accessCode: String,
        override val dateTimeString: String,
        override val timestamp: Instant?,
        override val isFirst: Boolean = false,
        override val isLast: Boolean = false,
        override val showButtons: Boolean,
        override val prescriptionNames: List<String> = emptyList(),
        override val taskIds: List<String>,
        override val countryCode: String,
        override val title: String,
        override val description: String?,
        override val isUnread: Boolean,
        override val isRevoked: Boolean = false
    ) : EuOrderMessageUiModel()

    data class TaskRemoved(
        override val id: String,
        override val underlyingEventIds: List<String>,
        override val orderId: String,
        override val accessCode: String,
        override val dateTimeString: String,
        override val timestamp: Instant?,
        override val isFirst: Boolean = false,
        override val isLast: Boolean = false,
        override val showButtons: Boolean,
        override val prescriptionNames: List<String> = emptyList(),
        override val taskIds: List<String>,
        override val countryCode: String,
        override val title: String,
        override val description: String?,
        override val isUnread: Boolean,
        override val isRevoked: Boolean = false
    ) : EuOrderMessageUiModel()
}
