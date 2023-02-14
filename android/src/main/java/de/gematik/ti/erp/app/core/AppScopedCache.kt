/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.core

import androidx.compose.runtime.saveable.Saver
import de.gematik.ti.erp.app.App
import java.util.UUID

class AppScopedCache {
    private val data: MutableMap<String, Any?> = mutableMapOf()
    private val lock = Any()

    fun store(key: String, value: Any?) {
        synchronized(lock) {
            data += key to value
        }
    }

    fun recover(key: String): Any? =
        synchronized(lock) {
            data.remove(key)
        }
}

fun <T : Any?> complexAutoSaver(): Saver<T, *> = complexAutoSaver(init = {})

fun <T : Any?> complexAutoSaver(
    init: T.() -> Unit
): Saver<T, *> = Saver(
    save = { state ->
        val key = UUID.randomUUID().toString()
        App.cache.store(key, state)
        key
    },
    restore = { key ->
        @Suppress("UNCHECKED_CAST")
        (App.cache.recover(key) as T).apply {
            init()
        }
    }
)
