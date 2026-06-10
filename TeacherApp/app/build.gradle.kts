plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.cps.teacherapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cps.teacherapp"
        minSdk = 26
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

    // НОВО: ViewBinding — наместо findViewById()
    // ЗОШТО? Со viewBinding секој XML елемент е директно достапен
    // без ризик од NullPointerException
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Токен зачувување
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Room — локална база
    // ЗОШТО Room наместо директен SQLite?
    // Room прави compile-time проверка на SQL queries — грешките ги наоѓаш
    // уште додека пишуваш код, не кога апликацијата паѓа кај корисникот
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // ViewModel + LiveData — задолжително за MVVM
    // ЗОШТО? ViewModel преживува ротација на екранот,
    // LiveData автоматски го освежува UI-от
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.7")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}