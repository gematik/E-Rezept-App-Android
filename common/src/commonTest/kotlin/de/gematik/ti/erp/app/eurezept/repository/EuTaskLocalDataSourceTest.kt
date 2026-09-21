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
 * In case of specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.eurezept.repository

import de.gematik.ti.erp.app.database.api.eurezept.EuTaskLocalDataSource
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.v1.euredeem.EuAccessCodeEntityV1
import de.gematik.ti.erp.app.database.realm.v1.euredeem.EuOrderEntityV1
import de.gematik.ti.erp.app.database.realm.v1.euredeem.EuTaskEventLogEntityV1
import de.gematik.ti.erp.app.database.realm.v1.eurezept.EuTaskLocalDataSourceV1
import de.gematik.ti.erp.app.database.realm.v1.migrations.SchemaVersion
import de.gematik.ti.erp.app.db.TestDB
import de.gematik.ti.erp.app.eurezept.model.EuEventType
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class EuTaskLocalDataSourceTest : TestDB() {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var realm: Realm
    private lateinit var dataSource: EuTaskLocalDataSource

    @Before
    fun setUp() {
        realm = Realm.open(
            RealmConfiguration.Builder(
                schema = setOf(
                    EuOrderEntityV1::class,
                    EuAccessCodeEntityV1::class,
                    EuTaskEventLogEntityV1::class
                )
            )
                .schemaVersion(SchemaVersion.ACTUAL)
                .directory(tempDBPath)
                .build()
        )
        dataSource = EuTaskLocalDataSourceV1(realm = realm, dispatcher = dispatcher)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun now() = Clock.System.now()

    /** Pre-seed an EuOrderEntityV1 with an optional access code. */
    private suspend fun seedOrder(
        orderId: String = "order-1",
        profileId: String = PROFILE_ID,
        countryCode: String = COUNTRY_CODE,
        withValidAccessCode: Boolean = true,
        accessCodeExpired: Boolean = false
    ) {
        realm.write {
            val accessCode = if (withValidAccessCode) {
                EuAccessCodeEntityV1().apply {
                    this.accessCode = "access-code-$orderId"
                    this.countryCode = countryCode
                    this.profileId = profileId
                    this.createdAt = now().toRealmInstant()
                    this.validUntil = if (accessCodeExpired) {
                        (now() - 1.hours).toRealmInstant()
                    } else {
                        (now() + 24.hours).toRealmInstant()
                    }
                }
            } else {
                null
            }

            val order = EuOrderEntityV1().apply {
                this.orderId = orderId
                this.profileId = profileId
                this.countryCode = countryCode
                this.createdAt = now().toRealmInstant()
                this.lastModifiedAt = now().toRealmInstant()
                this.euAccessCode = accessCode
            }
            copyToRealm(order)
        }
    }

    private fun queryEvents() = realm.query<EuTaskEventLogEntityV1>().find()

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    fun `addRedeemedEventIfValidOrderExists - happy path - appends TASK_REDEEMED event`() = runTest(dispatcher) {
        seedOrder()

        dataSource.addRedeemedEventIfValidOrderExists(
            profileId = PROFILE_ID,
            countryCode = COUNTRY_CODE,
            taskId = TASK_ID
        )

        val events = queryEvents()
        assertEquals(1, events.size, "Expected exactly one event to be appended")
        val event = events.first()
        assertEquals(EuEventType.TASK_REDEEMED.name, event.event)
        assertEquals(TASK_ID, event.taskId)
        assertEquals("order-1", event.orderId)
        assertTrue(event.isUnread, "New event must be marked as unread")
    }

    @Test
    fun `addRedeemedEventIfValidOrderExists - no order for profile and country - no event added`() = runTest(dispatcher) {
        // Seed an order for a DIFFERENT country code
        seedOrder(countryCode = "FR")

        dataSource.addRedeemedEventIfValidOrderExists(
            profileId = PROFILE_ID,
            countryCode = COUNTRY_CODE, // "DE" - no order exists for this
            taskId = TASK_ID
        )

        assertEquals(0, queryEvents().size, "No event should be appended when no matching order exists")
    }

    @Test
    fun `addRedeemedEventIfValidOrderExists - order has expired access code - no event added`() = runTest(dispatcher) {
        seedOrder(withValidAccessCode = true, accessCodeExpired = true)

        dataSource.addRedeemedEventIfValidOrderExists(
            profileId = PROFILE_ID,
            countryCode = COUNTRY_CODE,
            taskId = TASK_ID
        )

        assertEquals(0, queryEvents().size, "No event should be appended when access code is expired")
    }

    @Test
    fun `addRedeemedEventIfValidOrderExists - order has no access code - no event added`() = runTest(dispatcher) {
        seedOrder(withValidAccessCode = false)

        dataSource.addRedeemedEventIfValidOrderExists(
            profileId = PROFILE_ID,
            countryCode = COUNTRY_CODE,
            taskId = TASK_ID
        )

        assertEquals(0, queryEvents().size, "No event should be appended when order has no access code")
    }

    @Test
    fun `addRedeemedEventIfValidOrderExists - no order at all - no event added`() = runTest(dispatcher) {
        // Don't seed anything

        dataSource.addRedeemedEventIfValidOrderExists(
            profileId = PROFILE_ID,
            countryCode = COUNTRY_CODE,
            taskId = TASK_ID
        )

        assertEquals(0, queryEvents().size, "No event should be appended when the database is empty")
    }

    @Test
    fun `addRedeemedEventIfValidOrderExists - multiple orders same profile and country - picks most recent`() = runTest(dispatcher) {
        val olderOrderId = "order-old"
        val newerOrderId = "order-new"

        // Seed an older valid order
        realm.write {
            val accessCode = EuAccessCodeEntityV1().apply {
                this.accessCode = "code-old"
                this.countryCode = COUNTRY_CODE
                this.profileId = PROFILE_ID
                this.createdAt = (now() - 2.hours).toRealmInstant()
                this.validUntil = (now() + 24.hours).toRealmInstant()
            }
            copyToRealm(
                EuOrderEntityV1().apply {
                    orderId = olderOrderId
                    profileId = PROFILE_ID
                    countryCode = COUNTRY_CODE
                    createdAt = (now() - 2.hours).toRealmInstant()
                    lastModifiedAt = (now() - 2.hours).toRealmInstant()
                    euAccessCode = accessCode
                }
            )
        }

        // Seed a newer valid order
        realm.write {
            val accessCode = EuAccessCodeEntityV1().apply {
                this.accessCode = "code-new"
                this.countryCode = COUNTRY_CODE
                this.profileId = PROFILE_ID
                this.createdAt = (now() - 1.minutes).toRealmInstant()
                this.validUntil = (now() + 24.hours).toRealmInstant()
            }
            copyToRealm(
                EuOrderEntityV1().apply {
                    orderId = newerOrderId
                    profileId = PROFILE_ID
                    countryCode = COUNTRY_CODE
                    createdAt = (now() - 1.minutes).toRealmInstant()
                    lastModifiedAt = (now() - 1.minutes).toRealmInstant()
                    euAccessCode = accessCode
                }
            )
        }

        dataSource.addRedeemedEventIfValidOrderExists(
            profileId = PROFILE_ID,
            countryCode = COUNTRY_CODE,
            taskId = TASK_ID
        )

        val events = queryEvents()
        assertEquals(1, events.size, "Expected exactly one event total")
        assertEquals(newerOrderId, events.first().orderId, "Event should be appended to the most recently modified order")
    }

    @Test
    fun `addRedeemedEventIfValidOrderExists - accumulates with existing events on the same order`() = runTest(dispatcher) {
        seedOrder()

        // Call twice for different tasks
        dataSource.addRedeemedEventIfValidOrderExists(PROFILE_ID, COUNTRY_CODE, "task-1")
        dataSource.addRedeemedEventIfValidOrderExists(PROFILE_ID, COUNTRY_CODE, "task-2")

        val events = queryEvents()
        assertEquals(2, events.size, "Two TASK_REDEEMED events should accumulate on the same order")
        assertTrue(events.all { it.event == EuEventType.TASK_REDEEMED.name })
    }

    companion object {
        private const val PROFILE_ID = "profile-123"
        private const val COUNTRY_CODE = "DE"
        private const val TASK_ID = "task-abc"
    }
}
