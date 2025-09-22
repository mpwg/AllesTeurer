import java.util.Properties
import java.io.FileInputStream

// Load .env file into system properties
fun loadEnvFile() {
    val envFile = rootProject.file(".env")
    if (envFile.exists()) {
        val properties = Properties()
        envFile.inputStream().use { properties.load(it) }
        properties.forEach { key, value ->
            if (System.getenv(key.toString()) == null) {
                System.setProperty(key.toString(), value.toString())
            }
        }
    }
}

loadEnvFile()