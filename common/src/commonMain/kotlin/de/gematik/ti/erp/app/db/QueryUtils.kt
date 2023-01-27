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

package de.gematik.ti.erp.app.db

import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.TypedRealm
import kotlinx.coroutines.delay

inline fun <reified T : RealmObject> TypedRealm.queryFirst(
    query: String = "TRUEPREDICATE",
    vararg args: Any?
): T? = query(T::class, query, *args).first().find()

inline fun <reified T : RealmObject> MutableRealm.queryFirst(
    query: String = "TRUEPREDICATE",
    vararg args: Any?
): T? = query(T::class, query, *args).first().find()

/**
 * If a query for [T] returns null a new object resulting from [factory] will be copied to the realm with
 * a previous call to [block].
 *
 * See also [writeToRealm].
 */
suspend inline fun <reified T : RealmObject, R> Realm.writeOrCopyToRealm(
    crossinline factory: () -> T,
    crossinline block: MutableRealm.(T) -> R
): R? =
    write {
        queryFirst<T>()?.let {
            block(it)
        } ?: run {
            block(copyToRealm(factory()))
        }
    }

/**
 * Queries [T] and calls [block] with the concrete instance of [T] as its receiver.
 * [block] will only be called if any object of type [T] is present.
 */
suspend inline fun <reified T : RealmObject, R> Realm.writeToRealm(
    crossinline block: MutableRealm.(T) -> R
): R? =
    write {
        queryFirst<T>()?.let {
            block(it)
        }
    }

/**
 * Queries [T] and calls [block] with the concrete instance of [T] as its receiver.
 * [block] will only be called if any object of type [T] is present.
 */
suspend inline fun <reified T : RealmObject, R> Realm.writeToRealm(
    query: String = "TRUEPREDICATE",
    vararg args: Any?,
    crossinline block: MutableRealm.(T) -> R
): R? =
    write {
        queryFirst<T>(query, *args)?.let {
            block(it)
        }
    }

suspend fun <R> Realm.tryWrite(block: MutableRealm.() -> R): R {
    delay(100)
    return write {
        try {
            block()
        } catch (t: Throwable) {
            cancelWrite()
            throw t
        }
    }
}
