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

package de.gematik.ti.erp.app.di

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

const val PREFERENCES_FILE_NAME = "appPrefs"
const val DEMO_PREFERENCES_FILE_NAME = "appDemoPrefs"

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TruststoreMoshi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationPreferences

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationDemoPreferences

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @ApplicationPreferences
    fun providesPrefs(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    fun providesMoshi(): Moshi = Moshi.Builder().build()

    @ApplicationDemoPreferences
    @Provides
    fun providesDemoPrefs(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(DEMO_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    }
}
