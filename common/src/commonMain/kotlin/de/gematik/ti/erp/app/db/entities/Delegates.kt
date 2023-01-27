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

package de.gematik.ti.erp.app.db.entities

import de.gematik.ti.erp.app.fhir.parser.asFormattedString
import de.gematik.ti.erp.app.fhir.parser.asTemporalAccessor
import java.lang.IllegalArgumentException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import org.bouncycastle.util.encoders.Base64
import java.time.temporal.TemporalAccessor

inline fun <reified T : Enum<T>> enumName(backingProperty: KMutableProperty<String>) =
    object : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T =
            enumValueOf(backingProperty.getter.call())

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            backingProperty.setter.call(value.name)
        }
    }

inline fun <reified T : Enum<T>> enumName(backingProperty: KMutableProperty<String>, defaultValue: T) =
    object : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T =
            try {
                enumValueOf(backingProperty.getter.call())
            } catch (_: IllegalArgumentException) {
                defaultValue
            }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            backingProperty.setter.call(value.name)
        }
    }

fun byteArrayBase64(backingProperty: KMutableProperty<String>) =
    object : ReadWriteProperty<Any?, ByteArray> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): ByteArray =
            Base64.decode(backingProperty.getter.call())

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: ByteArray) {
            backingProperty.setter.call(Base64.toBase64String(value))
        }
    }

fun byteArrayBase64Nullable(backingProperty: KMutableProperty<String?>) =
    object : ReadWriteProperty<Any?, ByteArray?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): ByteArray? =
            backingProperty.getter.call()?.let { Base64.decode(it) }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: ByteArray?) {
            backingProperty.setter.call(value?.let { Base64.toBase64String(it) })
        }
    }

fun temporalAccessorNullable(backingProperty: KMutableProperty<String?>) =
    object : ReadWriteProperty<Any?, TemporalAccessor?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): TemporalAccessor? =
            backingProperty.getter.call()?.asTemporalAccessor()

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: TemporalAccessor?) {
            backingProperty.setter.call(value?.asFormattedString())
        }
    }
