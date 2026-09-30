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
            .file("database/src/commonMain/kotlin/de/gematik/ti/erp/app/database/realm/v1/migrations/SchemaVersion.kt")

        val outputFile = project.rootProject
            .file("database/build/schema/schema_migrations.md")

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

fun TaskContainer.generateRoomSchemaMigrationsFile() {
    register(TaskNames.generateRoomSchemaMigrationsFile) {
        group = "build"
        description = "Generates a markdown file with Room schema migration history."

        val schemaSourceFile = project.rootProject
            .file("database/src/commonMain/kotlin/de/gematik/ti/erp/app/database/room/RoomSchemaVersion.kt")

        val outputFile = project.rootProject
            .file("database/build/schema/room_schema_migrations.md")

        doLast {
            println("✅ Running Room schema migration generator")
            if (!schemaSourceFile.exists()) {
                throw GradleScriptException(
                    "Room schema version file not found",
                    Exception("Expected file: ${schemaSourceFile.absolutePath}")
                )
            }

            val migrationLines = schemaSourceFile.readLines()
                .filter { it.trim().startsWith("RoomSchemaMigration(") }
                .mapNotNull { line ->
                    Regex("""RoomSchemaMigration\((\d+),\s*"(.*?)"\)""")
                        .find(line)
                        ?.destructured
                        ?.let { (version, description) -> "- **$version**: $description" }
                }

            outputFile.parentFile.mkdirs()
            outputFile.writeText(
                buildString {
                    appendLine("# Room Schema Migrations")
                    appendLine()
                    migrationLines.forEach { appendLine(it) }
                }
            )

            println("✅ Wrote Room schema migration file to: ${outputFile.absolutePath}")
        }
    }
}

/**
 * Validates that Room database migrations are properly implemented when entities are modified.
 * This task ensures developers can't build the app if they:
 * 1. Modified Room entity files
 * 2. But didn't increment RoomSchemaVersion.ACTUAL
 * 3. And didn't add corresponding migrations in Database.android.kt
 *
 * This prevents data loss issues when switching between branches with different database schemas.
 */
fun TaskContainer.validateRoomMigrations() {
    register(TaskNames.validateRoomMigrations) {
        group = "verification"
        description = "Validates that Room database migrations are properly implemented when entities are modified."

        val schemaVersionFile = project.rootProject
            .file("database/src/commonMain/kotlin/de/gematik/ti/erp/app/database/room/RoomSchemaVersion.kt")

        val databaseAndroidFile = project.rootProject
            .file("database/src/androidMain/kotlin/de/gematik/ti/erp/app/database/room/Database.android.kt")

        val entityDir = project.rootProject
            .file("database/src/commonMain/kotlin/de/gematik/ti/erp/app/database/room/v2")

        doLast {
            println("🔍 Validating Room database migrations...")

            if (!schemaVersionFile.exists()) {
                throw GradleScriptException(
                    "RoomSchemaVersion.kt file not found",
                    Exception("Expected file: ${schemaVersionFile.absolutePath}")
                )
            }

            if (!databaseAndroidFile.exists()) {
                throw GradleScriptException(
                    "Database.android.kt file not found",
                    Exception("Expected file: ${databaseAndroidFile.absolutePath}")
                )
            }

            // Extract current schema version from RoomSchemaVersion.kt
            val schemaVersionContent = schemaVersionFile.readText()
            val currentVersion = Regex("""const val ACTUAL = (\d+)""")
                .find(schemaVersionContent)
                ?.groupValues
                ?.get(1)
                ?.toIntOrNull()
                ?: throw GradleScriptException(
                    "Could not parse current Room schema version",
                    Exception("Check RoomSchemaVersion.ACTUAL value in ${schemaVersionFile.absolutePath}")
                )

            // Extract migration count from Database.android.kt
            // This gets the highest target version from all migration_X_Y definitions
            val databaseContent = databaseAndroidFile.readText()
            val highestMigrationTarget = Regex("""val migration_(\d+)_(\d+)""")
                .findAll(databaseContent)
                .map { it.groupValues[2].toInt() }
                .maxOrNull()
                ?: 0

            // Extract annotations from RoomSchemaVersion.kt
            val annotationCount = Regex("""RoomSchemaMigration\((\d+),""")
                .findAll(schemaVersionContent)
                .count()

            println("  📊 Schema version: $currentVersion")
            println("  📝 Highest migration target version: $highestMigrationTarget")
            println("  📚 Annotations declared: $annotationCount")

            // Validate migration consistency
            val errors = mutableListOf<String>()

            // The highest migration target should equal the current schema version
            // E.g., if we have migration_11_12, highest target is 12, ACTUAL should be 12
            if (highestMigrationTarget != currentVersion) {
                errors.add(
                    """
                    ❌ Migration target version mismatch!
                       - Expected highest migration target: $currentVersion
                       - Actual highest migration target: $highestMigrationTarget
                       
                    You must implement migrations for each version increment.
                    
                    To fix:
                    1. If you modified Room entity files, you MUST increment RoomSchemaVersion.ACTUAL to $highestMigrationTarget
                    2. Or remove the extra migration and keep ACTUAL at $currentVersion
                    3. See: database/src/androidMain/kotlin/de/gematik/ti/erp/app/database/room/Database.android.kt
                    """.trimIndent()
                )
            }

            if (annotationCount != currentVersion - 1) {
                errors.add(
                    """
                    ❌ Migration annotation mismatch!
                       - Expected annotations: ${currentVersion - 1}
                       - Actual annotations: $annotationCount
                       
                    Add RoomSchemaMigration annotations for each migration in RoomSchemaVersion.kt
                    """.trimIndent()
                )
            }

            if (errors.isNotEmpty()) {
                throw GradleScriptException(
                    """
                    
                    🚨 ROOM DATABASE MIGRATION VALIDATION FAILED 🚨
                    
                    ${errors.joinToString("\n\n")}
                    
                    ℹ️  This check ensures that database schema changes include proper migrations.
                    ℹ️  Without migrations, users switching between branches will lose data!
                    
                    📖 Documentation:
                       - Copilot instructions: .github/copilot-instructions.md
                       - Database module: database/src/androidMain/kotlin/de/gematik/ti/erp/app/database/room/
                    """.trimIndent(),
                    Exception("Migration validation failed")
                )
            }

            println("✅ Room migrations validation passed!")
        }
    }
}
