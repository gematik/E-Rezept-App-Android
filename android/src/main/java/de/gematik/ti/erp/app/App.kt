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

package de.gematik.ti.erp.app

import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import de.gematik.ti.erp.app.userauthentication.ui.AuthenticationUseCase
import org.bouncycastle.jce.provider.BouncyCastleProvider
import timber.log.Timber
import javax.inject.Inject

val BCProvider = BouncyCastleProvider()

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var demoUseCase: DemoUseCase

    @Inject
    lateinit var authUseCase: AuthenticationUseCase

    override fun onCreate() {
        super.onCreate()
        appContext = this
        if (BuildKonfig.INTERNAL) {
            Timber.plant(Timber.DebugTree())
        }

        ProcessLifecycleOwner.get().lifecycle.apply {
            addObserver(demoUseCase)
            addObserver(authUseCase)
        }
    }

    companion object {
        lateinit var appContext: Context
    }
}

fun app(): Application = App.appContext as Application
