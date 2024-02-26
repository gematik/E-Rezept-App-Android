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

package de.gematik.ti.erp.app.utils.extensions

import io.github.aakira.napier.Napier

fun <T> riskyOperation(
    block: () -> T,
    defaultValue: T? = null
): T? {
    return try {
        block()
    } catch (e1: Throwable) {
        Napier.e { "Exception on first attempt $e1. Trying again" }
        try {
            // Attempt to execute the block again
            block()
        } catch (e2: Throwable) {
            Napier.e { "Exception on second attempt $e2. Giving up" }
            defaultValue
        }
    }
}
