/*
 * "${GEMATIK_COPYRIGHT_STATEMENT}"
 */

package de.gematik.ti.erp.app

import androidx.lifecycle.ProcessLifecycleOwner
import com.contentsquare.android.Contentsquare
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import de.gematik.ti.erp.app.di.allModules
import de.gematik.ti.erp.app.userauthentication.ui.AuthenticationUseCase
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
        importAll(allModules)
        bindSingleton { AuthenticationUseCase(instance()) }
        bindSingleton { VisibleDebugTree() }
    }

    private val authUseCase: AuthenticationUseCase by instance()

    private val visibleDebugTree: VisibleDebugTree by instance()

    override fun onCreate() {
        super.onCreate()
        if (BuildKonfig.INTERNAL) {
            Napier.base(DebugAntilog())
            Napier.base(visibleDebugTree)
        }
        ProcessLifecycleOwner.get().lifecycle.apply {
            addObserver(authUseCase)
        }
        PDFBoxResourceLoader.init(this)
        Contentsquare.start(this)
    }
}
