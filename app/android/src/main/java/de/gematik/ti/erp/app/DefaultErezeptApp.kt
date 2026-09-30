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

package de.gematik.ti.erp.app

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import de.gematik.ti.erp.app.appauthentication.observer.InactivityTimeoutObserver
import de.gematik.ti.erp.app.appauthentication.observer.ProcessLifecycleObserver
import de.gematik.ti.erp.app.di.appModules
import de.gematik.ti.erp.app.di.featureModule
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.AdvancePushKeyChainUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.UpdateFcmTokenUseCase
import de.gematik.ti.erp.app.translation.di.textTranslatorModule
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.bindSingleton
import org.kodein.di.instance

class DefaultErezeptApp : ErezeptApp(), DIAware {

    override val di by DI.lazy {
        import(androidXModule(this@DefaultErezeptApp))
        importAll(appModules)
        importAll(textTranslatorModule, allowOverride = true)
        importAll(featureModule, allowOverride = true)
        bindSingleton { InactivityTimeoutObserver(instance(), instance()) }
        bindSingleton { ProcessLifecycleObserver(ProcessLifecycleOwner, instance()) }
        bindSingleton { VisibleDebugTree() }
    }

    private val processLifecycleObserver: ProcessLifecycleObserver by instance()

    private val visibleDebugTree: VisibleDebugTree by instance()

    private val advancePushKeyChainUseCase: AdvancePushKeyChainUseCase by instance()
    private val updateFcmTokenUseCase: UpdateFcmTokenUseCase by instance()

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

        @Requirement(
            "A_27171#1",
            sourceSpecification = "gemF_PushNotification",
            rationale = "Triggers provider pushkey synchronization after application startup.",
            codeLines = 8
        )
        val processLifecycleScope = ProcessLifecycleOwner.get().lifecycleScope
        processLifecycleScope.launch(Dispatchers.IO + startupExceptionHandler) {
            advancePushKeyChainUseCase()
        }
        processLifecycleScope.launch(Dispatchers.IO + startupExceptionHandler) {
            updateFcmTokenUseCase.syncAtAppStart()
        }
    }

    private val startupExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Napier.e("Unhandled exception during application startup background work", throwable)
    }
}
