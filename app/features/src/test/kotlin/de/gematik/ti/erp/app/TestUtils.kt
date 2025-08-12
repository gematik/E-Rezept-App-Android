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

import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Retrieves the current value of a private [StateFlow] property from the given instance using Kotlin reflection.
 *
 * This utility is intended for use in **unit tests** to verify internal state held in private or internal
 * `StateFlow` properties of a class, such as `ViewModel`s, without requiring changes to visibility.
 *
 * ### Example usage:
 * ```
 * val taskIds: List<String> = getPrivateStateFlowValue(viewModel, "selectedTaskIdsFromOrder")
 * assertEquals(listOf("task-1"), taskIds)
 * ```
 *
 * ### Parameters:
 * @param T The expected return type of the value inside the StateFlow.
 * @param instance The object instance containing the private StateFlow property.
 * @param propertyName The name of the private property to retrieve (must be a [StateFlow]).
 *
 * ### Returns:
 * The current value stored inside the [StateFlow] property.
 *
 * ### Throws:
 * - [NoSuchElementException] if the property with the given name is not found.
 * - [IllegalStateException] if the property exists but is not a [StateFlow].
 *
 * ### Notes:
 * - This function uses Kotlin reflection and `isAccessible = true` to bypass visibility modifiers like `private` or `internal`.
 * - Only use this in testing scenarios. It should **not** be used in production code.
 * - Requires the `kotlin-reflect` library to be present in your test dependencies.
 *
 * @see StateFlow
 * @see kotlinx.coroutines.flow.MutableStateFlow
 */
@Suppress("UNCHECKED_CAST")
fun <T> getPrivateStateFlowValue(instance: Any, propertyName: String): T {
    val kClass = instance::class
    val property = kClass.declaredMemberProperties
        .first { it.name == propertyName }
        .apply { isAccessible = true } as KProperty1<Any, *>

    val value = property.get(instance)
    return when (value) {
        is StateFlow<*> -> value.value as T
        else -> error("Property '$propertyName' is not a StateFlow")
    }
}
