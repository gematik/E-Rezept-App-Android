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

package de.gematik.ti.erp.app

import androidx.lifecycle.ProcessLifecycleOwner
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import de.gematik.ti.erp.app.di.appModules
import de.gematik.ti.erp.app.di.featureModule
import de.gematik.ti.erp.app.userauthentication.observer.InactivityTimeoutObserver
import de.gematik.ti.erp.app.userauthentication.observer.ProcessLifecycleObserver
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.bindSingleton
import org.kodein.di.instance

class DefaultErezeptApp : ErezeptApp(), DIAware {

    override val di by DI.lazy {
        import(androidXModule(this@DefaultErezeptApp))
        importAll(appModules)
        importAll(featureModule, allowOverride = true)
        bindSingleton { InactivityTimeoutObserver(instance(), instance()) }
        bindSingleton { ProcessLifecycleObserver(ProcessLifecycleOwner, instance()) }
        bindSingleton { VisibleDebugTree() }
    }

    private val processLifecycleObserver: ProcessLifecycleObserver by instance()

    private val visibleDebugTree: VisibleDebugTree by instance()

    @Requirement(
        "O.Source_3#2",
        "O.Source_8#3",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Enabling the logs only for debug builds only.",
        codeLines = 10
    )
    override fun onCreate() {
        super.onCreate()
        if (BuildConfigExtension.isInternalDebug) {
            Napier.base(DebugAntilog())
            Napier.base(visibleDebugTree)
        }

        processLifecycleObserver.observeForInactivity()

        PDFBoxResourceLoader.init(this)
    }
}
