# Database Versioning & Migration Policy

## Current Schema Version
- **Database Version**: `1`
- **Database File**: `learnwitme_secure.db` (SQLCipher encrypted)
- **Schema Export Path**: `core/storage/schemas/`

## Production Migration Rules
1. **NO Destructive Migrations**: `fallbackToDestructiveMigration()` MUST NOT be re-enabled in production builds.
2. **Schema Export Verification**: Room schema JSON outputs are committed to version control under `core/storage/schemas/`.
3. **Adding Schema Changes**:
   - Increment `version = N+1` in `LearnWitMeDatabase.kt`.
   - Implement `MIGRATION_N_N+1` in `DatabaseMigrations.kt`.
   - Add the migration object to `DatabaseMigrations.ALL_MIGRATIONS`.
   - Run `gradlew :core:storage:kspDebugKotlin` to verify the generated schema JSON.
