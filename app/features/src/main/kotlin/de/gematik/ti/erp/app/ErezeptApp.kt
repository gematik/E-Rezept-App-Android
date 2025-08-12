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

import android.app.Application
import android.os.StrictMode
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.appmattus.certificatetransparency.installCertificateTransparencyProvider
import com.google.mlkit.common.MlKit
import de.gematik.ti.erp.app.core.AppScopedCache
import de.gematik.ti.erp.app.database.settings.initSharedPrefsSettings
import de.gematik.ti.erp.app.di.ApplicationModule
import de.gematik.ti.erp.app.di.delayedLeakCanary
import de.gematik.ti.erp.app.medicationplan.alarm.MedicationPlanRescheduleAllSchedulesManager
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import de.gematik.ti.erp.app.utils.buildImageLoader

open class ErezeptApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        initSharedPrefsSettings(this)
        installCertificateTransparencyProvider()
        applicationModule = ApplicationModule(this)
        debugChecks()

        MedicationPlanRescheduleAllSchedulesManager(context = this).enqueueRescheduling()
        MlKit.initialize(this)
    }

    /**
     * Creates a new [ImageLoader] instance for the application.
     * This method is used to configure the image loading library with custom settings.
     *
     * @return A new instance of [ImageLoader] configured for the application.
     */
    @Suppress("MagicNumber")
    override fun newImageLoader(): ImageLoader = buildImageLoader()

    /**
     * Performs debug checks if the application is in internal debug mode.
     * This method enables strict thread and VM modes, and configures LeakCanary
     * to help identify potential issues during development.
     */
    private fun debugChecks() {
        if (BuildConfigExtension.isInternalDebug) {
            enabledStrictThreadMode()
            enabledStrictVmMode()
            delayedLeakCanary()
        }
    }

    /**
     * Enables strict thread mode for the application.
     * This method sets a strict thread policy that detects all potential issues
     * related to threading and logs them, also showing a dialog for violations.
     */
    private fun enabledStrictThreadMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyFlashScreen()
                .build()
        )
    }

    /**
     * Enables strict VM mode for the application.
     * This method sets a strict VM policy that detects all potential issues
     * related to the virtual machine and logs them.
     */
    private fun enabledStrictVmMode() {
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
    }

    companion object {
        val cache = AppScopedCache()
        lateinit var applicationModule: ApplicationModule
    }
}
