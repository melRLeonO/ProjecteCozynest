plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.0.21"
    // Add the dependency for the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
}

android {
    namespace = "institut.montilivi.projectecozynest"
    compileSdk = 35

    defaultConfig {
        applicationId = "institut.montilivi.projectecozynest"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }
}

dependencies {
    implementation("com.google.android.libraries.places:places:3.3.0")
    implementation ("androidx.appcompat:appcompat:1.6.1")

    //Navegació
    implementation("androidx.navigation:navigation-compose:2.8.5")
    //Biblioteca extesa d'icones
    implementation("androidx.compose.material:material-icons-extended")
    //Serialització
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    implementation(platform("com.google.firebase:firebase-bom:33.11.0"))

    // Add the dependencies for the Crashlytics and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")

    implementation("com.jakewharton.threetenabp:threetenabp:1.4.4")
    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    implementation("io.coil-kt:coil-compose:2.4.0")

    //Firebase FireStore
    implementation ("com.google.firebase:firebase-firestore-ktx")

    //Firebase DataStorage
    implementation ("com.google.firebase:firebase-storage-ktx")

//Firebase Remote config
    implementation ("com.google.firebase:firebase-config-ktx")

//Descàrregues d'imatges d'Internet
    implementation ("io.coil-kt:coil-compose:2.5.0")

    implementation("androidx.compose.material:material-icons-extended:1.6.7")

    implementation("com.google.firebase:firebase-storage-ktx")
    implementation ("androidx.compose.material:material:1.5.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}