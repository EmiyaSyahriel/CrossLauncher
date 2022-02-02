import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins{
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    kotlin("android")
}

android {
    compileSdk = 30

    // buildToolVersion = "30.0.2"

    defaultConfig {
        applicationId = "id.psw.vshlauncher"
        minSdk = 19
        targetSdk = 30
        versionCode = 2
        versionName = "0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionNameSuffix = "0.1"

        ndk {
            abiFilters.clear()
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
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

    externalNativeBuild {
        cmake {
            // relativePath("src/main/cpp/CMakeLists.txt")
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    packagingOptions{
        resources.excludes.add("META-INF/*.kotlin_module")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar") )))
    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.media:media:1.4.3")
    implementation("androidx.constraintlayout:constraintlayout:2.1.1")
    implementation("androidx.preference:preference:1.1.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("com.facebook.fresco:fresco:2.6.0")
    implementation("com.facebook.fresco:animated-base:2.6.0")
    implementation("com.facebook.fresco:animated-webp:2.6.0")
    implementation("com.facebook.fresco:animated-gif:2.6.0")
    implementation("com.facebook.fresco:webpsupport:2.6.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
