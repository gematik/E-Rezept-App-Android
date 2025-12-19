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

package de.gematik.ti.erp.app.database.realm.schema

import io.realm.kotlin.migration.AutomaticSchemaMigration

/**
 * Composite schema migration handler for Realm database.
 *
 * This class orchestrates the execution of multiple schema migrations in a sequential manner.
 * It's designed to handle complex database schema evolution where multiple versions need to
 * be applied incrementally.
 *
 * ## How it works:
 * 1. Receives a set of schema definitions ([AppRealmSchema])
 * 2. Sorts them by version number (oldest to newest)
 * 3. Filters out schemas that are already applied (version <= oldVersion)
 * 4. Executes each pending migration in order
 *
 * ## Why use this?
 * - **Incremental migrations**: Apply only the migrations needed between old and new versions
 * - **Ordered execution**: Ensures migrations run in the correct sequence
 * - **Flexibility**: Each schema can define its own migration logic
 * - **Maintainability**: Separates migration logic into individual schema definitions
 *
 * ## Example:
 * ```kotlin
 * val schemas = setOf(
 *     AppRealmSchemaV1(),
 *     AppRealmSchemaV2(),
 *     AppRealmSchemaV3()
 * )
 * val migration = CompositeAutomaticSchemaMigration(schemas)
 * ```
 *
 * If the database is at version 1 and needs to upgrade to version 3,
 * only V2 and V3 migrations will be executed.
 *
 * @param schemas A set of [AppRealmSchema] definitions containing version-specific migration logic
 * @see AutomaticSchemaMigration
 * @see AppRealmSchema
 */
class CompositeAutomaticSchemaMigration(
    private val schemas: Set<AppRealmSchema>
) : AutomaticSchemaMigration {

    /**
     * Executes the migration process from the old schema version to the new one.
     *
     * This method:
     * 1. Determines the current database version (oldVersion)
     * 2. Determines the target version (newVersion)
     * 3. Selects all schema definitions between these versions
     * 4. Executes their migration logic in ascending version order
     *
     * ## Migration process:
     * - Schemas are sorted by version number to ensure correct order
     * - Only schemas with version > oldVersion are processed (already applied schemas are skipped)
     * - Each schema's `migrateOrInitialize` function is invoked with migration context
     *
     * ## Important notes:
     * - If a schema doesn't define migration logic (`migrateOrInitialize` is null), it's skipped
     * - All migrations are executed within a single transaction managed by Realm
     * - If any migration fails, the entire transaction is rolled back
     *
     * @param migrationContext The Realm migration context containing old and new realm instances,
     *                        used to access and modify database schema and data
     *
     * @see AutomaticSchemaMigration.MigrationContext
     * @see AppRealmSchema.migrateSchema
     */
    override fun migrate(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        // Get the current database schema version (before migration)
        val oldVersion = migrationContext.oldRealm.schemaVersion()

        // Get the target schema version (after migration)
        val newVersion = migrationContext.newRealm.schemaVersion()

        // Sort schemas by version and execute only those that need to be applied
        schemas.sortedBy { it.version } // Ensure migrations run in correct order (oldest to newest)
            .filter { it.version > oldVersion } // Skip already applied migrations
            .forEach { schema ->
                // Execute the migration logic for this schema version
                // If migrateOrInitialize is null, the schema has no migration logic and is skipped
                schema.migrateSchema?.invoke(migrationContext, oldVersion, newVersion)
            }
    }
}
