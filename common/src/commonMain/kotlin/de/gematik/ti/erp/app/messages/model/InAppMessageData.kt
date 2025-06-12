/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.messages.model

import de.gematik.ti.erp.app.timestate.TimeState
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
data class InAppMessage(
    @PrimaryKey
    val id: String,
    val from: String,
    val text: String?,
    val timeState: TimeState,
    val prescriptionsCount: Int = 0,
    val tag: String,
    var isUnread: Boolean,
    val lastMessage: LastMessage?,
    val messageProfile: CommunicationProfile?,
    val version: String
)

@Serializable
data class InternalMessage(
    val id: String,
    val sender: String,
    val text: String?,
    val time: TimeState,
    val tag: String,
    var isUnread: Boolean,
    val messageProfile: CommunicationProfile?,
    val version: String,
    val languageCode: String
)

@Serializable
data class ChangeLogMessage(
    val id: String,
    val timestamp: String?,
    val text: String?,
    val version: String
)
