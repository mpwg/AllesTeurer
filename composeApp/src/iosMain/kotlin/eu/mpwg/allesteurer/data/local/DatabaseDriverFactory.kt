package eu.mpwg.allesteurer.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import eu.mpwg.allesteurer.database.AllesTeuerDatabase

/**
 * iOS-specific implementation of DatabaseDriverFactory.
 * Uses NativeSqliteDriver for SQLite database access on iOS.
 */
actual class DatabaseDriverFactory {
    
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = AllesTeuerDatabase.Schema,
            name = "allesteurer.db"
        )
    }
}