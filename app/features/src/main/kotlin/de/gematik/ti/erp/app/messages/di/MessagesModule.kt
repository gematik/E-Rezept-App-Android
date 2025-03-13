/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.messages.di

import de.gematik.ti.erp.app.base.usecase.UpdateInAppMessageUseCase
import de.gematik.ti.erp.app.changelogs.DefaultInAppMessageRepository
import de.gematik.ti.erp.app.changelogs.InAppDataSource
import de.gematik.ti.erp.app.changelogs.InAppMessageRepository
import de.gematik.ti.erp.app.messages.domain.model.InAppMessageResources
import de.gematik.ti.erp.app.messages.domain.repository.InAppLocalMessageRepository
import de.gematik.ti.erp.app.messages.domain.usecase.FetchInAppMessageUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.FetchWelcomeMessageUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetMessageUsingOrderIdUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetMessagesUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetProfileByOrderIdUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetRepliedMessagesUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetUnreadMessagesCountUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.MarkAllUnreadMessagesAsReadUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.SaveLocalCommunicationUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.SetInternalMessageAsReadUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.UpdateCommunicationConsumedStatusUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.UpdateInvoicesByOrderIdAndTaskIdUseCase
import de.gematik.ti.erp.app.messages.repository.CommunicationLocalDataSource
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.messages.repository.DefaultCommunicationRepository
import de.gematik.ti.erp.app.messages.repository.PharmacyCacheLocalDataSource
import de.gematik.ti.erp.app.messages.repository.PharmacyCacheRemoteDataSource
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

val messagesModule = DI.Module("messagesModule") {
    bindProvider { PharmacyCacheLocalDataSource(instance()) }
    bindProvider { PharmacyCacheRemoteDataSource(instance()) }
    bindProvider { CommunicationLocalDataSource(instance()) }
    bindProvider { GetRepliedMessagesUseCase(instance(), instance()) }
    bindProvider { GetMessagesUseCase(instance(), instance(), instance()) }
    bindProvider { GetMessageUsingOrderIdUseCase(instance(), instance()) }
    bindProvider { GetProfileByOrderIdUseCase(instance()) }
    bindProvider { GetUnreadMessagesCountUseCase(instance(), instance(), instance()) }
    bindProvider { MarkAllUnreadMessagesAsReadUseCase(instance(), instance(), instance()) }
    bindProvider { SaveLocalCommunicationUseCase(instance()) }
    bindProvider { UpdateCommunicationConsumedStatusUseCase(instance()) }
    bindProvider { UpdateInvoicesByOrderIdAndTaskIdUseCase(instance(), instance()) }
    bindProvider { UpdateInAppMessageUseCase(instance(), instance(), instance()) }
    bindProvider { InAppMessageResources(instance()) }
    bindProvider { InAppLocalMessageRepository(instance()) }
    bindProvider { FetchWelcomeMessageUseCase(instance(), instance(), instance()) }
    bindProvider { FetchInAppMessageUseCase(instance(), instance(), instance(), instance()) }
    bindProvider { SetInternalMessageAsReadUseCase(instance()) }
}

val messageRepositoryModule = DI.Module("messageRepositoryModule", allowSilentOverride = true) {
    bindProvider<CommunicationRepository> {
        DefaultCommunicationRepository(
            instance(),
            instance(),
            instance(),
            instance(),
            instance(),
            instance()
        )
    }
    bindProvider<InAppDataSource> { InAppDataSource(instance()) }
    bindProvider<InAppMessageRepository> { DefaultInAppMessageRepository(instance()) }
}
