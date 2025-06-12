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

package docgen

import io.github.classgraph.ClassGraph
import java.io.File

class FhirMarkdownGenerator {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val outputFile = File(args[0])
            val basePackage = args[1]

            val output = StringBuilder()
            output.appendLine("# Module") // ✅ required by Dokka
            output.appendLine()
            output.appendLine("This module documents FHIR Parser models.")
            output.appendLine()

            val scanResult = ClassGraph()
                .enableAllInfo()
                .acceptPackages(basePackage)
                .scan()

            val classes = scanResult.allClasses
                .filter { classInfo ->
                    val isSerializable = classInfo.annotationInfo.any { it.name == "kotlinx.serialization.Serializable" }
                    val isSynthetic = classInfo.name.contains('$') || classInfo.simpleName.any { it.isDigit() }
                    val isInFhirModelPackage = classInfo.packageName.startsWith("de.gematik.ti.erp.app.fhir")

                    isSerializable && !isSynthetic && isInFhirModelPackage
                }
            classes.forEach { clazzInfo ->
                output.append("## ${clazzInfo.simpleName}\n")
                output.append("- Package: `${clazzInfo.packageName}`\n")
                output.append("- Fields:\n")

                val clazz = Class.forName(clazzInfo.name)
                clazz.declaredFields.forEach { field ->
                    output.append("  - `${field.name}`: `${field.type.simpleName}`\n")
                }
                output.append("\n")
            }

            outputFile.writeText(output.toString())
            println("✅ Generated: ${outputFile.absolutePath}")
        }
    }
}
