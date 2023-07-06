import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
    id("org.jlleitschuh.gradle.ktlint") version "11.3.2"
    id("com.google.gms.google-services")
}

android {
    buildFeatures.viewBinding = true
    buildFeatures.dataBinding = true
    namespace = "com.quyt.mqttchat"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.quyt.mqttchat"
        minSdk = 26
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    packagingOptions {
        resources.excludes.add("META-INF/*")
    }
}

ktlint {
    android.set(true)
    ignoreFailures.set(false)
    debug.set(true)
    reporters {
        reporter(ReporterType.CHECKSTYLE)
        reporter(ReporterType.PLAIN)
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.8.0")
    implementation("com.google.firebase:firebase-messaging-ktx:23.1.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Glide - Load image
    implementation("com.github.bumptech.glide:glide:4.14.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.13.2")
    // SwipeRefreshLayout - Pull to refresh
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    // Retrofit - Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // OkHttp - Logging
    implementation("com.squareup.okhttp3:okhttp:3.14.9")
    implementation("com.squareup.okhttp3:logging-interceptor:3.14.9")
    // Hilt - Dependency Injection
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-compiler:2.44")
    // Navigation - Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    // Room - Database
    implementation("androidx.room:room-runtime:2.5.1")
    kapt("androidx.room:room-compiler:2.5.1")
    implementation("androidx.room:room-ktx:2.5.1")
    // Paging 3 - Pagination
    implementation("androidx.paging:paging-runtime-ktx:3.1.1")
    implementation("androidx.room:room-paging:2.5.1")
    // Realtime Protocol - MQTT
    implementation("com.hivemq:hivemq-mqtt-client:1.3.0")
    // Animation - Lottie
    implementation("com.airbnb.android:lottie:6.0.0")
    // FlexboxLayout - Flexbox
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    // Image Viewer - Stfalcon
    implementation("com.github.stfalcon-studio:StfalconImageViewer:v1.0.1")
    // Camera View
    api("com.otaliastudios:cameraview:2.7.2")

}

// Hilt - Allow references to generated code
kapt {
    correctErrorTypes = true
}
