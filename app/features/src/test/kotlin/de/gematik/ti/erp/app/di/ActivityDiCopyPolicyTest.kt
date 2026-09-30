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
 */

package de.gematik.ti.erp.app.di

import de.gematik.ti.erp.app.base.BaseConstants.applicationScope
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.DefaultHkdfSha256
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushKeyChainAdvancer
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushKeyChainManager
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationKeyStorage
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationKeyGeneration
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationData
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationStorage
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ActivityDiCopyPolicyTest {
    @Test
    fun `activity DI keeps application scoped state and copies ordinary singletons`() = runTest {
        val applicationDi = DI {
            bindSingleton<PushKeyChainManager>(applicationScope) {
                PushKeyChainManager(
                    hkdf = DefaultHkdfSha256(),
                    keyStorage = InMemoryKeyStorage(),
                    registrationStorage = EmptyRegistrationStorage
                )
            }
            bindSingleton<PushKeyChainManager> { instance(tag = applicationScope) }
            bindSingleton<PushKeyChainAdvancer> { instance<PushKeyChainManager>() }
            bindSingleton { OrdinarySingleton() }
            bindSingleton<ApplicationScopedSingleton>(applicationScope) { ApplicationScopedSingleton() }
        }
        val activityDi = DI {
            extend(applicationDi, copy = activityDiCopyPolicy)
        }

        val taggedApplicationManager =
            applicationDi.direct.instance<PushKeyChainManager>(tag = applicationScope)
        val activityManager = activityDi.direct.instance<PushKeyChainManager>()
        val activityAdvancer = activityDi.direct.instance<PushKeyChainAdvancer>()

        assertSame(taggedApplicationManager, activityManager)
        assertSame(taggedApplicationManager, activityAdvancer)
        assertNotSame(
            applicationDi.direct.instance<OrdinarySingleton>(),
            activityDi.direct.instance<OrdinarySingleton>()
        )
        assertSame(
            applicationDi.direct.instance<ApplicationScopedSingleton>(tag = applicationScope),
            activityDi.direct.instance<ApplicationScopedSingleton>(tag = applicationScope)
        )

        activityManager.initializeAndAdvance(
            iss = "00".repeat(32),
            keyIdentifier = "key-1",
            timeIssCreated = "2026-07"
        )

        assertTrue("key-1" in taggedApplicationManager.knownKeyIdentifiers())
    }

    private class OrdinarySingleton

    private class ApplicationScopedSingleton

    private class InMemoryKeyStorage : PushNotificationKeyStorage {
        private val values = mutableMapOf<String, List<PushNotificationKeyGeneration>>()

        override suspend fun save(
            keyIdentifier: String,
            generations: List<PushNotificationKeyGeneration>
        ): Result<Unit> = runCatching {
            values[keyIdentifier] = generations
        }

        override suspend fun load(keyIdentifier: String): Result<List<PushNotificationKeyGeneration>> =
            Result.success(values[keyIdentifier].orEmpty())

        override suspend fun clear(keyIdentifier: String): Result<Unit> = runCatching {
            values.remove(keyIdentifier)
            Unit
        }
    }

    private object EmptyRegistrationStorage : PushRegistrationStorage {
        override suspend fun save(
            profileId: ProfileIdentifier,
            data: PushRegistrationData
        ): Result<Unit> = Result.success(Unit)

        override suspend fun load(profileId: ProfileIdentifier): Result<PushRegistrationData?> =
            Result.success(null)

        override suspend fun loadAll(): Result<Map<ProfileIdentifier, PushRegistrationData>> =
            Result.success(emptyMap())

        override suspend fun hasCompletedPermissionPrompt(
            profileId: ProfileIdentifier
        ): Result<Boolean> = Result.success(false)

        override suspend fun markPermissionPromptCompleted(
            profileId: ProfileIdentifier
        ): Result<Unit> = Result.success(Unit)

        override suspend fun clear(profileId: ProfileIdentifier): Result<Unit> = Result.success(Unit)
    }
}
