plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.weather_app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.weather_app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
// RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
// CardView
    implementation("androidx.cardview:cardview:1.0.0")
// Retrofit (cho Networking)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
// GSON Converter (QUAN TRỌNG: Thay thế Moshi)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
// GSON Library
    implementation("com.google.code.gson:gson:2.10.1")
}