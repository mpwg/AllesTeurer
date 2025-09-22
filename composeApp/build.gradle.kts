import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Base64
import java.io.File

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.sqldelight.driver.android)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(projects.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        
        nativeMain.dependencies {
            implementation(libs.sqldelight.driver.native)
        }
    }
}

// Create keystore from base64 data if available, before Android configuration
val keystoreFile = run {
    val keystoreBase64 = System.getenv("ANDROID_KEYSTORE_BASE64") ?: System.getProperty("ANDROID_KEYSTORE_BASE64")
    if (keystoreBase64 != null && keystoreBase64.isNotBlank()) {
        val keystoreDir = File(projectDir, "build/keystore")
        keystoreDir.mkdirs()
        val keystoreFile = File(keystoreDir, "release.keystore")
        if (!keystoreFile.exists()) {
            val keystoreData = Base64.getDecoder().decode(keystoreBase64.trim())
            keystoreFile.writeBytes(keystoreData)
        }
        keystoreFile
    } else {
        null
    }
}

android {
    namespace = "eu.mpwg.allesteurer"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "eu.mpwg.allesteurer"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    
    // Android signing configuration
    signingConfigs {
        create("release") {
            // Use environment variables for CI/CD, fall back to gradle.properties for local
            keyAlias = System.getenv("ANDROID_KEY_ALIAS") ?: System.getProperty("ANDROID_KEY_ALIAS") ?: findProperty("ANDROID_KEY_ALIAS") as String? ?: "allesteurer"
            keyPassword = System.getenv("ANDROID_KEY_PASSWORD") ?: System.getProperty("ANDROID_KEY_PASSWORD") ?: findProperty("ANDROID_KEY_PASSWORD") as String?
            storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD") ?: System.getProperty("ANDROID_KEYSTORE_PASSWORD") ?: findProperty("ANDROID_KEYSTORE_PASSWORD") as String?
            
            // Use keystore file created earlier or fall back to file path
            if (keystoreFile != null) {
                storeFile = keystoreFile
            } else {
                val keystorePath = System.getenv("ANDROID_KEYSTORE_PATH") ?: System.getProperty("ANDROID_KEYSTORE_PATH") ?: findProperty("ANDROID_KEYSTORE_PATH") ?: "keystore/release.keystore"
                storeFile = file(keystorePath)
            }
        }
        
        getByName("debug") {
            // Use default debug signing for development
        }
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            // Only use release signing if we have the necessary credentials
            val hasKeystore = keystoreFile != null || file(System.getenv("ANDROID_KEYSTORE_PATH") ?: System.getProperty("ANDROID_KEYSTORE_PATH") ?: findProperty("ANDROID_KEYSTORE_PATH") ?: "keystore/release.keystore").exists()
            if (hasKeystore) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                println("Warning: No release keystore found, using debug signing for release build")
                signingConfig = signingConfigs.getByName("debug")
            }
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

sqldelight {
    databases {
        create("AllesTeuerDatabase") {
            packageName.set("eu.mpwg.allesteurer.database")
            srcDirs.from("src/commonMain/sqldelight")
        }
    }
}
