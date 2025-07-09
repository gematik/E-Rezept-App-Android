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

package de.gematik.ti.erp.app.db.entities

import io.realm.kotlin.Deleteable
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject

typealias Adjacent = Iterator<Deleteable>

interface Cascading : Deleteable {
    fun objectsToFollow(): Iterator<Deleteable>

    /**
     * Returns objects in reversed order; i.e. the most inner objects will be yielded first.
     */
    fun flatten(maxDepth: Int = Int.MAX_VALUE): Adjacent {
        require(maxDepth >= 0)
        return iterator {
            flatten(this@Cascading, 0, maxDepth)
        }
    }
}

fun Adjacent.objectIterator(): Iterator<RealmObject> =
    iterator {
        this@objectIterator.forEach { obj ->
            when (obj) {
                is RealmList<*> -> {
                    obj.forEach {
                        (it as? RealmObject)?.run { yield(it) }
                    }
                }
                is RealmObject ->
                    yield(obj)
            }
        }
    }

@Suppress("NestedBlockDepth")
private suspend fun SequenceScope<Deleteable>.flatten(
    currentObject: Cascading,
    currentDepth: Int,
    maxDepth: Int
) {
    if (currentDepth < maxDepth) {
        currentObject.objectsToFollow().forEach { obj ->
            when (obj) {
                is RealmList<*> -> {
                    obj.forEach { entry ->
                        if (entry is Cascading) {
                            flatten(entry, currentDepth + 1, maxDepth)
                        }
                    }
                }
                is Cascading -> {
                    flatten(obj, currentDepth + 1, maxDepth)
                }
            }
        }
    }
    yieldAll(currentObject.objectsToFollow())
}

fun MutableRealm.deleteAll(cascading: Cascading, maxDepth: Int = Int.MAX_VALUE) {
    cascading.flatten(maxDepth = maxDepth).forEachRemaining {
        delete(it)
    }
    delete(cascading)
}
