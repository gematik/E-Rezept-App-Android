# Database Migration Guide

## Overview

This guide explains how to properly modify the Room database schema and create migrations to prevent
data loss when developers switch between branches or users update the app.

## Problem We're Solving

Previously, developers could modify Room entity files without creating proper migrations. This
would:

- Cause the build to fail when trying to switch branches
- Result in data loss when users updated the app
- Leave the database in an inconsistent state

**Now there's an automated check that prevents builds from succeeding without proper migrations!**

## How the Validation Works

When you try to build the app, a Gradle task automatically validates that:

1. ✅ If you modified any Room entity files
2. ✅ You **incremented** `RoomSchemaVersion.ACTUAL`
3. ✅ You **added migrations** in `Database.android.kt`

If any of these are missing, **your build will FAIL** with a helpful error message.

## Step-by-Step: How to Add a Migration

### Step 1: Modify Your Entity

Edit any file in `database/src/commonMain/kotlin/de/gematik/ti/erp/app/database/room/v2/`

**Example:** Adding a new field to `ProfileEntity.kt`

```kotlin
@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey
    val identifier: String,
    val name: String,
    val newField: String  // ← NEW FIELD
)
```

### Step 2: Increment the Schema Version

Edit `database/src/commonMain/kotlin/de/gematik/ti/erp/app/database/room/RoomSchemaVersion.kt`

```kotlin
object RoomSchemaVersion {
    @RoomSchemaMigrations(
        RoomSchemaMigration(1, "Initial Room database schema"),
        // ... existing migrations ...
        RoomSchemaMigration(12, "Change payload column to structured CommunicationPayloadErpModel"),
        RoomSchemaMigration(13, "Add newField to ProfileEntity")  // ← ADD THIS
    )
    const val ACTUAL = 13  // ← INCREMENT THIS (was 12)
}
```

### Step 3: Add the Migration SQL

Edit `database/src/androidMain/kotlin/de/gematik/ti/erp/app/database/room/Database.android.kt`

```kotlin
fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    // ... existing code ...

    // Migration 11->12: Change payload column to structured CommunicationPayloadErpModel
    val migration_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE communications ADD COLUMN payload_structured TEXT DEFAULT NULL")
        }
    }

    // Migration 12->13: Add newField to ProfileEntity  ← ADD THIS
    val migration_12_13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE profiles ADD COLUMN newField TEXT DEFAULT NULL")
        }
    }

    builder.addMigrations(
        migration_1_2, migration_2_3, migration_3_4, migration_4_5,
        migration_5_6, migration_6_7, migration_7_8, migration_8_9,
        migration_9_10, migration_10_11, migration_11_12,
        migration_12_13  // ← ADD THIS
    )

    return builder
}
```

### Step 4: Build and Verify

```bash
./gradlew :database:compileDebugKotlin
```

If successful, you'll see:

```
🔍 Validating Room database migrations...
  📊 Schema version: 13
  📝 Migrations defined: 12
  📚 Annotations declared: 12
✅ Room migrations validation passed!
```

## If the Validation Fails

### Error: Migration count mismatch

```
❌ Migration count mismatch!
   - Expected migrations: 12 (for version 13)
   - Actual migrations: 11
```

**Fix:** You're missing a migration! Did you increment `RoomSchemaVersion.ACTUAL` but forget to add
`migration_12_13` to `Database.android.kt`?

### Error: Migration annotation mismatch

```
❌ Migration annotation mismatch!
   - Expected annotations: 12
   - Actual annotations: 11
```

**Fix:** Did you add a migration to `Database.android.kt` but forget to add the
`RoomSchemaMigration` annotation to `RoomSchemaVersion.kt`?

## Migration Types

### ALTER TABLE (Adding Columns)

```kotlin
val migration_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add a new nullable column
        db.execSQL("ALTER TABLE profiles ADD COLUMN newColumn TEXT DEFAULT NULL")

        // Add a new non-nullable column with default value
        db.execSQL("ALTER TABLE profiles ADD COLUMN status TEXT NOT NULL DEFAULT 'active'")
    }
}
```

### CREATE TABLE (New Tables)

```kotlin
val migration_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS eu_orders (
                id TEXT NOT NULL PRIMARY KEY,
                taskId TEXT,
                createdAt INTEGER
            )
        """.trimIndent()
        )
    }
}
```

### DROP/RECREATE TABLE (Schema Changes)

For complex changes like making fields nullable, you need to recreate the table:

```kotlin
val migration_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create new table with updated schema
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS patient_new (
                patientId TEXT NOT NULL PRIMARY KEY,
                name TEXT,  -- now nullable
                dob TEXT    -- now nullable
            )
        """.trimIndent()
        )

        // Copy data from old table
        db.execSQL("INSERT INTO patient_new SELECT patientId, name, dob FROM patient")

        // Drop old table
        db.execSQL("DROP TABLE patient")

        // Rename new table
        db.execSQL("ALTER TABLE patient_new RENAME TO patient")
    }
}
```

### Data Migrations (Clearing/Updating Data)

```kotlin
val migration_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Clear invoice cache to force re-mapping
        db.execSQL("DELETE FROM invoices")
    }
}
```

## Files Involved

```
database/
├── src/
│   ├── commonMain/kotlin/de/gematik/ti/erp/app/database/room/
│   │   ├── RoomSchemaVersion.kt          ← Update version & add annotation
│   │   ├── v2/
│   │   │   ├── profile/
│   │   │   │   └── ProfileEntity.kt      ← Modify entity
│   │   │   └── ... other entities
│   │   └── AppDatabase.kt
│   └── androidMain/kotlin/de/gematik/ti/erp/app/database/room/
│       └── Database.android.kt            ← Add migration SQL
└── build/schemas/                         ← Auto-generated schema snapshots
    └── de.gematik.ti.erp.app.database.room.AppDatabase/
        └── 13.json                        ← Room generates this
```

## Testing Your Migrations

Run the migration tests to verify your changes work correctly:

```bash
# Run all migration tests
./gradlew database:testDebugUnitTest --tests "RoomMigrationTest"

# Run specific test
./gradlew database:testDebugUnitTest --tests "RoomMigrationTest.testAllRequiredTablesExist"
```

## Common Mistakes

### ❌ Don't: Modify entity without incrementing version

```kotlin
// BAD - Will fail validation
const val ACTUAL = 12  // Didn't increment!
```

### ✅ Do: Always increment when modifying entities

```kotlin
// GOOD
const val ACTUAL = 13  // Incremented for the entity change
```

### ❌ Don't: Add version annotation without migration

```kotlin
// BAD - Will fail validation
RoomSchemaMigration(13, "Added newField")  // But no migration_12_13 in Database.android.kt
```

### ✅ Do: Add both annotation AND migration

```kotlin
// GOOD - Both are present
RoomSchemaMigration(13, "Added newField")  // ← Annotation in RoomSchemaVersion.kt
val migration_12_13 = object : Migration(12, 13) { ... }  // ← Migration in Database.android.kt
```

## FAQ

### Q: Can I skip the migration validation?

**A:** No. The validation runs before every build. It's there to protect users from data loss.

### Q: What if I made a mistake and need to fix it?

**A:**

1. Revert your entity changes
2. Revert the version increment
3. Remove the migration you added
4. Make the changes correctly
5. Rebuild

### Q: Can I test migrations locally?

**A:** Yes! Run the migration tests:

```bash
./gradlew database:testDebugUnitTest
```

### Q: How do I know which version to increment to?

**A:** Look at `RoomSchemaVersion.ACTUAL`. If it says 12, increment to 13.

### Q: What if I'm merging conflicting migrations?

**A:** Don't modify `RoomSchemaVersion.ACTUAL` manually. Always increment by 1 from the current
value. If there's a conflict, coordinate with the other developer.

## For More Information

- Migration Tests:
  `database/src/androidUnitTest/kotlin/de/gematik/ti/erp/app/database/room/RoomMigrationTest.kt`
- Build Validation: `buildSrc/src/main/kotlin/de/gematik/ti/erp/app/tasks/SchemaMigrationTask.kt`

---

**Remember:** Database migrations are critical for user data. Always test them thoroughly before
committing!

