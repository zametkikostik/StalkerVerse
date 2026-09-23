plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "org.web3browser"
    compileSdk = 35
    defaultConfig {
        applicationId = "org.web3browser"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-dev"
        val localProps = rootProject.file("local.properties")
        val props = java.util.Properties()
        if (localProps.exists()) localProps.inputStream().use { props.load(it) }
        val reownProjectId = props.getProperty("reown.project.id") ?: "YOUR_REOWN_PROJECT_ID"
        buildConfigField("String", "REOWN_PROJECT_ID", "\"$reownProjectId\"")
        buildConfigField("String", "WEB3_RPC_URL", "\"https://eth.llamarpc.com\"")
    }
    buildTypes {
        debug { isMinifyEnabled = false; applicationIdSuffix = ".debug" }
        release { isMinifyEnabled = true }
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.webkit:webkit:1.12.1")
    implementation("org.web3j:core:4.12.2")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    implementation("com.reown:android-core:1.7.0")
    implementation("com.reown:walletkit:1.7.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
