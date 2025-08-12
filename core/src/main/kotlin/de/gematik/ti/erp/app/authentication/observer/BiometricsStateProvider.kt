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

package de.gematik.ti.erp.app.authentication.observer

import de.gematik.ti.erp.app.authentication.model.BiometricMethod
import kotlinx.coroutines.flow.SharedFlow

/**
 * Provides a shared flow that emits the currently active [BiometricMethod].
 *
 * This interface is used to abstract the source of biometric method state changes,
 * allowing other components (such as those in core modules) to observe changes
 * without directly depending on platform-specific classes like `BaseActivity`.
 */
interface BiometricStateProvider {
    /**
     * A [SharedFlow] that emits updates to the current [BiometricMethod],
     * typically triggered from an Android lifecycle method like `onResume`.
     */
    val biometricStateChangedFlow: SharedFlow<BiometricMethod>
}

/**
 * Singleton holder for the current [BiometricStateProvider] implementation.
 *
 * This allows decoupled modules to access biometric method changes without
 * directly referencing the Android layer. The provider must be assigned once,
 * typically from an Android entry point (e.g., `BaseActivity` in the feature module).
 */
object BiometricStateProviderHolder {
    /**
     * The current [BiometricStateProvider] instance.
     *
     * Must be initialized before use (e.g., during application or activity startup).
     */
    lateinit var provider: BiometricStateProvider
}
