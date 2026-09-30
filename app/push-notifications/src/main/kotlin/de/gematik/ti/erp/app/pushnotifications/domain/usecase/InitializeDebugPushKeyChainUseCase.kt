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

package de.gematik.ti.erp.app.pushnotifications.domain.usecase

import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.pushnotifications.BuildConfig
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushKeyChainManager
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Initializes the configured debug push key chain for internal debug builds.
 */
class InitializeDebugPushKeyChainUseCase(
    private val keyChainManager: PushKeyChainManager,
    private val isInternalDebugBuild: () -> Boolean = { BuildKonfig.INTERNAL && BuildConfig.DEBUG },
    private val initialSharedSecret: String = BuildConfig.PUSH_NOTIFICATION_DEBUG_INITIAL_SHARED_SECRET,
    private val timeIssCreated: String = BuildConfig.PUSH_NOTIFICATION_DEBUG_TIME_ISS_CREATED,
    private val keyIdentifier: String = BuildConfig.PUSH_NOTIFICATION_DEBUG_KEY_IDENTIFIER,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(requiredKeyIdentifier: String? = null): Boolean = withContext(dispatcher) {
        if (!isInternalDebugBuild()) return@withContext false
        if (requiredKeyIdentifier != null && requiredKeyIdentifier != keyIdentifier) {
            return@withContext false
        }

        keyChainManager.restoreFromStorage()
        if (!keyChainManager.isInitialized(keyIdentifier)) {
            keyChainManager.initializeAndAdvance(
                iss = initialSharedSecret,
                timeIssCreated = timeIssCreated,
                keyIdentifier = keyIdentifier
            )
            Napier.d { "Initialized debug push key chain." }
        } else {
            keyChainManager.advanceToCurrentMonth(keyIdentifier)
        }

        true
    }
}
