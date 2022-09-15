/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.di

import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.gematik.ti.erp.app.db.converter.TruststoreConverter
import de.gematik.ti.erp.app.vau.VauCryptoConfig
import de.gematik.ti.erp.app.vau.api.model.OCSPAdapter
import de.gematik.ti.erp.app.vau.api.model.X509Adapter
import de.gematik.ti.erp.app.vau.interceptor.DefaultCryptoConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TruststoreAbstractModule {
    @Singleton
    @Binds
    abstract fun bindCryptoConfig(
        defaultCryptoConfig: DefaultCryptoConfig
    ): VauCryptoConfig
}

@Module
@InstallIn(SingletonComponent::class)
object TruststoreModule {
    @TruststoreMoshi
    @Provides
    fun provideTruststoreMoshi(): Moshi = Moshi.Builder().add(OCSPAdapter()).add(X509Adapter()).build()

    @Provides
    fun providesRoomConverter(@TruststoreMoshi moshi: Moshi) = TruststoreConverter(moshi)
}
