/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.utils

/**
 * Calls the specified function [transform] with `this` value as its arguments and returns its result when no element is null.
 */
inline fun <A, B, R> Pair<A?, B?>.letNotNull(transform: (A, B) -> R): R? {
    // Using the values of the pair directly will confuse kotlin's null inference
    val firstFrozen = first
    val secondFrozen = second
    return if (firstFrozen != null && secondFrozen != null) {
        transform(firstFrozen, secondFrozen)
    } else {
        null
    }
}

inline fun <A, B, R> letNotNull(first: A?, second: B?, transform: (A, B) -> R): R? =
    if (first != null && second != null) {
        transform(first, second)
    } else {
        null
    }

inline fun <A, R> letNotNullOnCondition(first: A?, condition: () -> Boolean, transform: (A) -> R): R? {
    val result = condition()
    return if (first != null && result) {
        transform(first)
    } else {
        null
    }
}

inline fun <A, B, R> letNotNullOnCondition(
    first: A?,
    second: B?,
    condition: () -> Boolean,
    transform: (A, B) -> R
): R? {
    val result = condition()
    return if (first != null && second != null && result) {
        transform(first, second)
    } else {
        null
    }
}

inline fun <A, B, C, R> letNotNullOnCondition(
    first: A?,
    second: B?,
    third: C?,
    condition: () -> Boolean,
    transform: (A, B, C) -> R
): R? {
    val result = condition()
    return if (first != null && second != null && third != null && result) {
        transform(first, second, third)
    } else {
        null
    }
}

/**
 * Calls the specified function [transform] with `this` value as its arguments and returns its result when no element is null.
 */
inline fun <A, B, C, R> Triple<A?, B?, C?>.letNotNull(transform: (A, B, C) -> R): R? {
    val firstFrozen = first
    val secondFrozen = second
    val thirdFrozen = third
    return if (firstFrozen != null && secondFrozen != null && thirdFrozen != null) {
        transform(firstFrozen, secondFrozen, thirdFrozen)
    } else {
        null
    }
}

/**
 * Calls the specified function [transform] with `this` value as its arguments and returns its result when no element is null.
 */

inline fun <A, B, C, R> letNotNull(first: A?, second: B?, third: C?, transform: (A, B, C) -> R): R? =
    if (first != null && second != null && third != null) {
        transform(first, second, third)
    } else {
        null
    }

/**
 * Calls the specified function [block] with `this` value as its arguments and returns its result when no element is [null].
 */
inline fun <A, B, C, D, R> Quad<A, B, C, D>.letNotNull(transform: (A, B, C, D) -> R): R? {
    val firstFrozen = first
    val secondFrozen = second
    val thirdFrozen = third
    val fourthFrozen = fourth
    return if (firstFrozen != null && secondFrozen != null && thirdFrozen != null && fourthFrozen != null) {
        transform(firstFrozen, secondFrozen, thirdFrozen, fourthFrozen)
    } else {
        null
    }
}
