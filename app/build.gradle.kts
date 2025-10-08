plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.simats.educonnect"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.simats.educonnect"
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
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.github.bumptech.glide:glide:4.16.0")




    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("com.android.billingclient:billing-ktx:6.1.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    androidTestImplementation(libs.androidx.espresso.core)
}
dependencies {

    implementation("com.android.volley:volley:1.2.1")
}
