/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.gradleplugins.model

data class AnnotationBody(
    val fileName: String,
    val line: Int,
    val requirement: String,
    val specification: String,
    val rationale: String,
    val codeBlock: CodeBlock,
    val suffixNumber: Int = 0 // Default value for requirements without a suffix

) {

    fun fileLink() = "https://github.com/gematik/E-Rezept-App-Android/blob/master/$fileName#$line"

    @Suppress("MagicNumber")
    fun extractSuffixNumber(): Int {
        val suffixIndex = requirement.indexOf('#')
        return if (suffixIndex != -1 && suffixIndex < requirement.length - 1) {
            requirement.substring(suffixIndex + 1).toIntOrNull() ?: 0
        } else {
            0
        }
    }

    fun rationale(): String = rationale
}

data class CodeBlock(val code: List<String>)
