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

package de.gematik.ti.erp.app.demomode

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Immutable

@Immutable
object DemoModeIntent {
    inline fun <reified T> intent(
        context: Context,
        demoModeAction: DemoModeIntentAction
    ) = Intent(context, T::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        action = demoModeAction.name
    }
}

inline fun <reified T> DemoModeIntent.startAppWithDemoMode(activity: ComponentActivity) {
    activity.finish()
    activity.startActivity(
        intent<T>(
            context = activity,
            demoModeAction = DemoModeIntentAction.DemoModeStarted
        )
    )
}

inline fun <reified T> DemoModeIntent.startAppWithNormalMode(activity: ComponentActivity) {
    activity.finish()
    activity.startActivity(
        intent<T>(
            context = activity,
            demoModeAction = DemoModeIntentAction.DemoModeEnded
        )
    )
}

enum class DemoModeIntentAction {
    DemoModeStarted,
    DemoModeEnded
}
