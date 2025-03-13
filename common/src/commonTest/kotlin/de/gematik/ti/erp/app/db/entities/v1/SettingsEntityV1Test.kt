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

package de.gematik.ti.erp.app.db.entities.v1

import de.gematik.ti.erp.app.db.TestDB
import de.gematik.ti.erp.app.db.entities.deleteAll
import de.gematik.ti.erp.app.db.queryFirst
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import org.junit.Test

import kotlin.test.assertEquals

class SettingsEntityV1Test : TestDB() {
    @Test
    fun `cascading delete`() {
        Realm.open(
            RealmConfiguration.Builder(
                schema = setOf(
                    SettingsEntityV1::class,
                    PharmacySearchEntityV1::class,
                    PasswordEntityV1::class,
                    ShippingContactEntityV1::class,
                    PharmacySearchEntityV1::class,
                    AddressEntityV1::class,
                    AuthenticationEntityV1::class,
                    AuthenticationPasswordEntityV1::class
                )
            )
                .schemaVersion(0)
                .directory(tempDBPath)
                .build()
        ).also { realm ->
            realm.writeBlocking {
                copyToRealm(
                    SettingsEntityV1().apply {
                        this.pharmacySearch = PharmacySearchEntityV1()
                        this.password = PasswordEntityV1()
                    }
                )
            }

            assertEquals(1, realm.query<SettingsEntityV1>().count().find())
            assertEquals(1, realm.query<PharmacySearchEntityV1>().count().find())
            assertEquals(1, realm.query<PasswordEntityV1>().count().find())

            realm.writeBlocking {
                val settings = queryFirst<SettingsEntityV1>()!!
                deleteAll(settings)
            }

            assertEquals(0, realm.query<SettingsEntityV1>().count().find())
            assertEquals(0, realm.query<PharmacySearchEntityV1>().count().find())
            assertEquals(0, realm.query<PasswordEntityV1>().count().find())
        }
    }
}
