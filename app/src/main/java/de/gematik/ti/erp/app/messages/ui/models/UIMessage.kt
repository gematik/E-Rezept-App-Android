/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.messages.ui.models

sealed class CommunicationReply(
    open val communicationId: String,
    open val supplyOptionsType: String,
    open val header: Int,
    open val message: String?,
    open val actionText: Int = -1,
    open val consumed: Boolean
)

data class UIMessage(
    override val communicationId: String,
    override val supplyOptionsType: String,
    override val header: Int,
    override val message: String?,
    val pickUpCodeHR: String? = null,
    val pickUpCodeDMC: String? = null,
    val url: String? = null,
    override val actionText: Int = -1,
    override val consumed: Boolean
) : CommunicationReply(
    communicationId, supplyOptionsType, header, message, actionText, consumed
)

data class ErrorUIMessage(
    override val communicationId: String,
    override val supplyOptionsType: String,
    override val header: Int,
    override val message: String?,
    val displayText: Int,
    val timeStamp: String,
    override val actionText: Int = -1,
    override val consumed: Boolean
) : CommunicationReply(
    communicationId, supplyOptionsType, header, message, actionText, consumed
)
