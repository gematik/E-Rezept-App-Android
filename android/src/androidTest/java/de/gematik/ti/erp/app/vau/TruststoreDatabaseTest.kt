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

package de.gematik.ti.erp.app.vau

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.squareup.moshi.Moshi
import de.gematik.ti.erp.app.db.AppDatabase
import de.gematik.ti.erp.app.db.converter.TruststoreConverter
import de.gematik.ti.erp.app.db.daos.TruststoreDao
import de.gematik.ti.erp.app.db.entities.TruststoreEntity
import de.gematik.ti.erp.app.vau.api.model.OCSPAdapter
import de.gematik.ti.erp.app.vau.api.model.X509Adapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class TruststoreDatabaseTest {

    private lateinit var truststoreDao: TruststoreDao
    private lateinit var db: AppDatabase

    private val moshi = Moshi.Builder().add(OCSPAdapter()).add(X509Adapter()).build()

    @Before
    fun createDB() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        )
            .addTypeConverter(TruststoreConverter(moshi))
            .build()
        truststoreDao = db.truststoreDao()
    }

    @After
    fun closeDB() {
        db.close()
    }

    @Test
    fun trustStoreSavesBothLists() = runBlocking {
        assertEquals(null, truststoreDao.getUntrusted())

        val entity = TruststoreEntity(
            TestCertificates.Vau.CertList,
            TestCertificates.OCSPList.OCSPList
        )

        truststoreDao.insert(entity)

        assertEquals(entity, truststoreDao.getUntrusted())

        truststoreDao.deleteAll()

        assertEquals(null, truststoreDao.getUntrusted())
    }
}
