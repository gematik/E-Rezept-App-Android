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
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationUseCase
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationUseCaseDelegate
import de.gematik.ti.erp.app.cardwall.usecase.CardWallUseCase
import de.gematik.ti.erp.app.cardwall.usecase.CardWallUseCaseDelegate
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

private const val SECURE_PREFS_FILE_NAME = "CARD_WALL_PREFS"
private const val DEMO_SECURE_PREFS_FILE_NAME = "DEMO_CARD_WALL_PREFS"
private const val MASTER_KEY_ALIAS = "CARD_WALL_KEY_ALIAS"

@ActivityScoped
class AppSharedPreferences @Inject constructor(
    @ApplicationPreferences
    private val appNormalPrefs: SharedPreferences,
    @ApplicationDemoPreferences
    private val appDemoPrefs: SharedPreferences,
    private val demoUseCase: DemoUseCase
) {
    operator fun invoke(): SharedPreferences =
        if (demoUseCase.isDemoModeActive) {
            appDemoPrefs
        } else {
            appNormalPrefs
        }
}

@ActivityScoped
class SecureCardWallSharedPreferences @Inject constructor(
    @Named("cardWallSecurePrefs")
    private val secNormalPrefs: SharedPreferences,
    @Named("cardWallDemoSecurePrefs")
    private val secDemoPrefs: SharedPreferences,
    private val demoUseCase: DemoUseCase
) {
    operator fun invoke(): SharedPreferences =
        if (demoUseCase.isDemoModeActive) {
            secDemoPrefs
        } else {
            secNormalPrefs
        }
}

@Module
@InstallIn(SingletonComponent::class)
object CardWallModule {

    @Singleton
    @Provides
    @Named("cardWallSecurePrefs")
    fun providesSecPrefs(@ApplicationContext context: Context): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_FILE_NAME,
            MasterKey.Builder(context, MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Singleton
    @Provides
    @Named("cardWallDemoSecurePrefs")
    fun providesSecDemoPrefs(@ApplicationContext context: Context): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            DEMO_SECURE_PREFS_FILE_NAME,
            MasterKey.Builder(context, MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AbstractCardWallModule {
    @Binds
    abstract fun bindsCardWallUseCase(delegate: CardWallUseCaseDelegate): CardWallUseCase

    @Binds
    abstract fun bindsAuthenticationUseCase(delegate: AuthenticationUseCaseDelegate): AuthenticationUseCase
}
