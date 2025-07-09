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

package de.gematik.ti.erp.app.utils

import java.util.Properties

/**
 * Properties class that does not escape the values when storing them.
 * By default, the store method escapes characters like = and : by adding a backslash before them.
 * This class does not escape the values.
 */
class UnescapedProperties : Properties() {
    override fun store(out: java.io.Writer?, comments: String?) {
        val writer = out as? java.io.BufferedWriter ?: out?.let { java.io.BufferedWriter(it) }
        if (writer != null) {
            storeNoEscape(writer, comments)
        }
    }

    fun storeNoEscape(out: java.io.BufferedWriter, comments: String?) {
        val lineSeparator = System.getProperty("line.separator")
        if (comments != null) {
            out.write("#$comments")
            out.write(lineSeparator)
        }
        synchronized(this) {
            for ((key, value) in this.entries) {
                out.write(key.toString())
                out.write("=")
                out.write(value.toString())
                out.write(lineSeparator)
            }
        }
        out.flush()
    }
}
