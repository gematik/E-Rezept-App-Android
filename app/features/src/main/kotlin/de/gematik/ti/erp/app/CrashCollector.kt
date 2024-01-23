/*
 * Copyright (c) 2024 gematik GmbH
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

import android.util.Log
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.crashes.Crashes
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension

/**
 * Only in a non-release mode the crashes are captured for analysis
 */
fun MainActivity.collectCrashOnlyForDebug() {
    if (BuildConfigExtension.isDebugOrMinifiedDebug) {
        AppCenter.start(
            application,
            BuildKonfig.APP_CENTER_SECRET,
            Crashes::class.java
        )
        AppCenter.setLogLevel(Log.VERBOSE)
    }
}

/**
 * Any exception that is not thrown correctly is caught here as a fallback mechanism
 */
fun MainActivity.catchAllUnCaughtExceptions() {
    if (BuildConfigExtension.isReleaseMode) {
        val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            defaultExceptionHandler!!.uncaughtException(thread, MessageConversionException(throwable))
        }
    }
}
