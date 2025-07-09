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

package de.gematik.ti.erp.app.tasks

import de.gematik.ti.erp.app.utils.TaskNames
import org.gradle.api.GradleScriptException
import org.gradle.api.tasks.TaskContainer

fun TaskContainer.generateSchemaMigrationsFile() {
    register(TaskNames.generateSchemaMigrationsFile) {
        group = "build"
        description = "Generates a markdown file with schema migration history and commits it to the repo."

        val schemaSourceFile = project.rootProject
            .file("common/src/commonMain/kotlin/de/gematik/ti/erp/app/db/SchemaVersion.kt")

        val outputFile = project.rootProject
            .file("common/build/schema/schema_migrations.md")

        doLast {
            println("✅ Running schema migration generator")
            if (!schemaSourceFile.exists()) {
                throw GradleScriptException(
                    "Schema version file not found",
                    Exception("Expected file: ${schemaSourceFile.absolutePath}")
                )
            }

            val migrationLines = schemaSourceFile.readLines()
                .filter { it.trim().startsWith("SchemaMigration(") }
                .mapNotNull { line ->
                    Regex("""SchemaMigration\((\d+),\s*"(.*?)"\)""")
                        .find(line)
                        ?.destructured
                        ?.let { (version, description) -> "- **$version**: $description" }
                }

            outputFile.parentFile.mkdirs()
            outputFile.writeText(
                buildString {
                    appendLine("# Schema Migrations")
                    appendLine()
                    migrationLines.forEach { appendLine(it) }
                }
            )

            println("✅ Wrote schema migration file to: ${outputFile.absolutePath}")
        }
    }
}
