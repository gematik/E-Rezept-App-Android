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
        `package` = context.packageName
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
