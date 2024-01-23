/*
 * "${GEMATIK_COPYRIGHT_STATEMENT}"
 */

package de.gematik.ti.erp.app

import androidx.lifecycle.ProcessLifecycleOwner
import com.contentsquare.android.Contentsquare
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import de.gematik.ti.erp.app.di.appModules
import de.gematik.ti.erp.app.di.mockFeatureModule
import de.gematik.ti.erp.app.userauthentication.observer.InactivityTimeoutObserver
import de.gematik.ti.erp.app.userauthentication.observer.ProcessLifecycleObserver
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.bindSingleton
import org.kodein.di.instance

class DefaultErezeptMockApp : ErezeptApp(), DIAware {

    override val di by DI.lazy {
        import(androidXModule(this@DefaultErezeptMockApp))
        importAll(appModules)
        importAll(mockFeatureModule, allowOverride = true)
        bindSingleton { InactivityTimeoutObserver(instance(), instance()) }
        bindSingleton { ProcessLifecycleObserver(ProcessLifecycleOwner, instance()) }
        bindSingleton { VisibleDebugTree() }
    }

    private val processLifecycleObserver: ProcessLifecycleObserver by instance()

    private val visibleDebugTree: VisibleDebugTree by instance()

    override fun onCreate() {
        super.onCreate()
        Napier.base(DebugAntilog())
        Napier.base(visibleDebugTree)

        processLifecycleObserver.observeForInactivity()

        PDFBoxResourceLoader.init(this)

        Contentsquare.start(this)
    }
}
