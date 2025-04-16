/*
 * Copyright 2025, gematik GmbH
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

import android.app.Application
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.appmattus.certificatetransparency.installCertificateTransparencyProvider
import de.gematik.ti.erp.app.core.AppScopedCache
import de.gematik.ti.erp.app.di.ApplicationModule
import de.gematik.ti.erp.app.medicationplan.worker.createNotificationReminderChannel
import de.gematik.ti.erp.app.medicationplan.worker.createNotificationReminderGroup
import de.gematik.ti.erp.app.medicationplan.worker.scheduleReminderWorker
import kotlin.time.Duration

open class ErezeptApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        installCertificateTransparencyProvider()
        applicationModule = ApplicationModule(this)

        createNotificationReminderGroup()
        createNotificationReminderChannel()
        scheduleReminderWorker(Duration.ZERO)
    }

    @Suppress("MagicNumber")
    override fun newImageLoader(): ImageLoader {
        return ImageLoader(this)
            .newBuilder()
            .components {
                when {
                    Build.VERSION.SDK_INT >= 28 -> add(ImageDecoderDecoder.Factory())
                    else -> add(GifDecoder.Factory())
                }
            }
            .build()
    }

    companion object {
        val cache = AppScopedCache()
        lateinit var applicationModule: ApplicationModule
    }
}
