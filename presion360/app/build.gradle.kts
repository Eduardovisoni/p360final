plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.presion360"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.presion360"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.cardview:cardview:1.0.0")

    implementation("androidx.sqlite:sqlite:2.4.0")

    // Declaraciones de red corregidas para ser 100% compatibles
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // Revertido a OkHttp v3 para ser compatible con Retrofit 2.9.0 y arreglar el crash
    implementation("com.squareup.okhttp3:logging-interceptor:3.14.9")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
