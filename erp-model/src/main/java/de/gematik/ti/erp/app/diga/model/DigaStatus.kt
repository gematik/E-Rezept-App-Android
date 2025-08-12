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

package de.gematik.ti.erp.app.diga.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
sealed class DigaStatus(val step: Int) {

    @Serializable
    data object Ready : DigaStatus(DigaStatusSteps.Ready.step)

    // this step is updates when the communication is sent or TaskState is in progress
    @Serializable
    data class InProgress(val sentOn: Instant?) : DigaStatus(DigaStatusSteps.InProgress.step)

    @Serializable
    data class CompletedWithRejection(val sentOn: Instant?) : DigaStatus(DigaStatusSteps.CompletedWithRejection.step)

    @Serializable
    data object CompletedSuccessfully : DigaStatus(DigaStatusSteps.CompletedSuccessfully.step)

    // user action changes the app to these status

    @Serializable
    data object DownloadDigaApp : DigaStatus(DigaStatusSteps.DownloadDigaApp.step)

    @Serializable
    data object OpenAppWithRedeemCode : DigaStatus(DigaStatusSteps.OpenAppWithRedeemCode.step)

    @Serializable
    data object ReadyForSelfArchiveDiga : DigaStatus(DigaStatusSteps.ReadyForSelfArchiveDiga.step)

    @Serializable
    data object SelfArchiveDiga : DigaStatus(DigaStatusSteps.SelfArchiveDiga.step)

    // other states from task that we don't process in the UI

    @Suppress("MagicNumber")
    @Serializable
    data class WrappedTaskStatus(val taskStatus: String) : DigaStatus(-1)
}

@Suppress("MagicNumber")
enum class DigaStatusSteps(val step: Int) {
    Ready(1),
    InProgress(2),
    CompletedWithRejection(2),
    CompletedSuccessfully(3),
    DownloadDigaApp(4),
    OpenAppWithRedeemCode(5),
    ReadyForSelfArchiveDiga(6),
    SelfArchiveDiga(7);
}
