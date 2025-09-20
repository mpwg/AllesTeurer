package eu.mpwg.allesteurer.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import eu.mpwg.allesteurer.database.AllesTeuerDatabase

/**
 * Android-specific implementation of DatabaseDriverFactory.
 * Uses AndroidSqliteDriver for SQLite database access on Android.
 */
actual class DatabaseDriverFactory(private val context: Context) {
    
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = AllesTeuerDatabase.Schema,
            context = context,
            name = "allesteurer.db"
        )
    }
}