/*
 * Copyright (c) 2023 gematik GmbH
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
import com.contentsquare.android.Contentsquare
import de.gematik.ti.erp.app.core.AppScopedCache
import de.gematik.ti.erp.app.di.allModules
import de.gematik.ti.erp.app.userauthentication.ui.AuthenticationUseCase
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.bindSingleton
import org.kodein.di.instance

class App : Application(), DIAware {

    override val di by DI.lazy {
        import(androidXModule(this@App))
        importAll(allModules)
        bindSingleton { AuthenticationUseCase(instance(), instance()) }
        bindSingleton { VisibleDebugTree() }
    }

    private val authUseCase: AuthenticationUseCase by instance()

    private val visibleDebugTree: VisibleDebugTree by instance()

    override fun onCreate() {
        super.onCreate()
        appContext = this
        if (BuildKonfig.INTERNAL) {
            Napier.base(DebugAntilog())
            Napier.base(visibleDebugTree)
        }

        ProcessLifecycleOwner.get().lifecycle.apply {
            addObserver(authUseCase)
        }

        Contentsquare.start(this)
    }

    companion object {
        lateinit var appContext: Context

        val cache = AppScopedCache()
    }
}

fun app(): Application = App.appContext as Application
