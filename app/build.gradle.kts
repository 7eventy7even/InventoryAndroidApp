plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "vkx64.android.scanventory"
    compileSdk = 34

    defaultConfig {
        applicationId = "vkx64.android.scanventory"
        minSdk = 30
        targetSdk = 34
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

    // Room
    implementation(libs.room.runtime)
    testImplementation(libs.ext.junit)
    annotationProcessor(libs.room.compiler)
    androidTestImplementation(libs.room.testing)

    // XLSX Parser
    implementation(libs.poi.ooxml)

    // QR Scanner
    implementation(libs.zxing.android.embedded)

    // Glide
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // Expresso
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.room:room-testing:2.5.0")

    // Mockito
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")

    // Robolectric
    testImplementation("org.robolectric:robolectric:4.10")

    // log4j2
    testImplementation("org.apache.logging.log4j:log4j-core:2.20.0")
    testImplementation("org.apache.logging.log4j:log4j-api:2.20.0")

    // Pull to refresh
    implementation(libs.swiperefreshlayout)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}