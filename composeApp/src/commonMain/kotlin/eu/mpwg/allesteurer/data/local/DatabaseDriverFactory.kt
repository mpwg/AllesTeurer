package eu.mpwg.allesteurer.data.local

import app.cash.sqldelight.db.SqlDriver
import eu.mpwg.allesteurer.database.AllesTeuerDatabase

/**
 * Multiplatform database driver factory interface.
 * Platform-specific implementations provide the appropriate SQLDelight driver.
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

/**
 * Database wrapper providing access to SQLDelight database with proper initialization.
 */
class DatabaseManager(driverFactory: DatabaseDriverFactory) {
    
    private val driver: SqlDriver = driverFactory.createDriver()
    
    val database: AllesTeuerDatabase = AllesTeuerDatabase(driver)
    
    /**
     * Initialize database with schema if needed.
     * This is called automatically when the database is first accessed.
     */
    fun initializeDatabase() {
        // Database schema is created automatically by SQLDelight
        // when the first query is executed
    }
    
    /**
     * Close the database connection.
     * Call this when the application is shutting down.
     */
    fun close() {
        driver.close()
    }
}