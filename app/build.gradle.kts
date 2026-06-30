import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

fun localProperty(name: String): String = localProperties.getProperty(name, "")

android {
    namespace = "com.wisatakita.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wisatakita.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "PEXELS_API_KEY", "\"${localProperty("PEXELS_API_KEY")}\"")
        buildConfigField("String", "GEOAPIFY_API_KEY", "\"${localProperty("GEOAPIFY_API_KEY")}\"")
        buildConfigField("String", "OPENWEATHER_API_KEY", "\"${localProperty("OPENWEATHER_API_KEY")}\"")
        buildConfigField("String", "DESTINATION_API_URL", "\"https://raw.githubusercontent.com/Hesham-prog/WisataKita/master/app/src/main/assets/destinations.json\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // ViewPager2 — for onboarding flow
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    // Lottie — splash, onboarding, empty states, floating lantern
    implementation("com.airbnb.android:lottie:6.7.1")

    // MPAndroidChart — radar/spider chart on Profile stats
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Google Credential Manager — Google Sign-In (modern API)
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Coroutines & Lifecycle for Async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
}
