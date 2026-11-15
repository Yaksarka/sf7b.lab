plugins { id("com.android.application"); kotlin("android"); kotlin("plugin.serialization"); id("org.jetbrains.kotlin.plugin.compose") }
android {
    namespace = "lab.sf7b"
    compileSdk = 36
    buildToolsVersion = "35.0.0"
    defaultConfig {
        applicationId = "lab.sf7b"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "0.2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    buildFeatures { compose = true; buildConfig = true }
    signingConfigs.getByName("debug") {
        val keyPath = providers.environmentVariable("SF7B_KEYSTORE_PATH").orNull
        if (keyPath != null) {
            val retainedKey = file(keyPath)
            require(retainedKey.isAbsolute && retainedKey.isFile) { "SF7B_KEYSTORE_PATH must point to an existing external keystore" }
            require(!retainedKey.canonicalFile.toPath().startsWith(rootDir.canonicalFile.toPath())) { "Keep the signing key outside the repository" }
            storeFile = retainedKey
            storePassword = providers.environmentVariable("SF7B_STORE_PASSWORD").get()
            keyAlias = providers.environmentVariable("SF7B_KEY_ALIAS").get()
            keyPassword = providers.environmentVariable("SF7B_KEY_PASSWORD").get()
        } else if (providers.gradleProperty("sf7bRequireRetainedKey").orNull == "true") {
            error("Retained signing key required; set SF7B_KEYSTORE_PATH and signing environment variables")
        }
    }
    testOptions { animationsDisabled = true }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}
kotlin { jvmToolchain(17) }
dependencies {
    implementation(project(":core"))
    implementation(platform("androidx.compose:compose-bom:2025.11.01"))
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.exifinterface:exifinterface:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.11.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
