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

package de.gematik.ti.erp.app.communication.model

// TODO CommResV3 maybe rename to CommProfileEntity without versioning since it is used generally not only in v1
enum class CommunicationProfileV1 {
    ErxCommunicationDispReq, ErxCommunicationReply, Unknown, InApp
}

fun CommunicationProfileV1.toCommunicationProfile(): CommunicationErpModel.CommunicationProfile =
    when (this) {
        CommunicationProfileV1.ErxCommunicationDispReq -> CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq
        CommunicationProfileV1.ErxCommunicationReply -> CommunicationErpModel.CommunicationProfile.ErxCommunicationReply
        CommunicationProfileV1.InApp -> CommunicationErpModel.CommunicationProfile.InApp
        CommunicationProfileV1.Unknown -> CommunicationErpModel.CommunicationProfile.InApp // Map Unknown to InApp as fallback
    }
