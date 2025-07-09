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
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.content.getSystemService
import java.util.Locale

/**
 * Any exception that is not thrown correctly is caught here as a fallback mechanism
 */
fun MainActivity.catchAllUnCaughtExceptions(activity: MainActivity) {
    val isDemoMode = activity.isDemoMode()
    val packageName = activity.packageName
    val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->

        val crashContext = mutableMapOf<String, Any?>()

        // Thread info
        crashContext["threadName"] = thread.name
        crashContext["threadId"] = thread.id
        crashContext["isMainThread"] = thread == Looper.getMainLooper().thread

        // App foreground state (approximate)
        crashContext["appInForeground"] = activity.isAppInForeground()

        // Device state
        crashContext["powerSaveMode"] = activity.getSystemService<PowerManager>()?.isPowerSaveMode

        val batteryStatus = activity.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val batteryLevel = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        crashContext["batteryLevel"] = batteryLevel

        // Memory snapshot
        val runtime = Runtime.getRuntime()
        crashContext["usedMem"] = runtime.totalMemory() - runtime.freeMemory()
        crashContext["maxMem"] = runtime.maxMemory()

        // Device info
        crashContext["abi"] = Build.SUPPORTED_ABIS.joinToString()
        crashContext["buildType"] = Build.TYPE
        crashContext["buildTime"] = Build.TIME
        crashContext["fingerprint"] = Build.FINGERPRINT

        // OS info
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            crashContext["currentProcessName"] = Application.getProcessName()
        }

        // Timestamp
        crashContext["timestamp"] = System.currentTimeMillis()

        // Locale info
        crashContext["locale"] = Locale.getDefault().toLanguageTag()
        crashContext["keyboardLocale"] = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        )

        // Accessibility info
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        crashContext["accessibilityEnabled"] = am?.isEnabled
        crashContext["touchExplorationEnabled"] = am?.isTouchExplorationEnabled

        // Additional metadata
        val otherTraces = Thread.getAllStackTraces()
            .filterKeys { it != Thread.currentThread() }
            .mapValues { it.value.joinToString("\n") }

        crashContext["otherThreads"] = otherTraces

        defaultExceptionHandler?.uncaughtException(
            thread,
            UncaughtException(
                isDemoMode = isDemoMode,
                packageName = packageName,
                throwable = throwable,
                metadata = crashContext
            )
        )
    }
}
