import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins{
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    kotlin("android")
}

android {
    compileSdk = 30

    defaultConfig {
        applicationId = "id.psw.vshlauncher.plugin_example"
        minSdk = 19
        targetSdk = 30
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes{
        getByName("release"){
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar") )))
    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.core:core-ktx:1.6.0")
    testImplementation("junit:junit:4.+")
    implementation("com.google.android.material:material:1.4.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
