
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Modifica importante: Rimosso 'version' e 'apply false' perché già gestito dal progetto
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.pushapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.pushapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {
    // --- Core Android ---
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.12.0") // Updated to match requires
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.navigation.runtime)
    implementation(libs.room.common.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.blurview)
    //LIBRERIE ESTERNE
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")

    // --- Compose ---
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // --- UI Claws ---
    implementation("androidx.appcompat:appcompat:1.7.1") // Updated to 1.7.1
    implementation("com.google.android.material:material:1.13.0") // Updated to 1.13.0
    implementation("androidx.constraintlayout:constraintlayout:2.2.1") // Updated
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // --- Navigation ---
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.6") // Updated
    implementation("androidx.navigation:navigation-ui-ktx:2.9.6") // Updated

    // --- Lifecycle ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0") // Updated
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.10.0") // Updated

    // --- Firebase ---
    implementation(platform("com.google.firebase:firebase-bom:34.7.0")) // Updated
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // --- Google Auth ---
    implementation("com.google.android.gms:play-services-auth:21.3.0") // Updated

    // --- Networking ---
    implementation("com.squareup.retrofit2:retrofit:2.11.0") // Updated
    implementation("com.squareup.retrofit2:converter-gson:2.11.0") // Updated

    // --- Charts ---
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // --- JSON & Scanner (Per FoodFragment) ---
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.3")

    // --- Debugging ---
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // dependencies per utilizzare Room
    val room_version = "2.8.4"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

}
