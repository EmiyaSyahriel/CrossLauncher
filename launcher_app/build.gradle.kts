import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins{
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    kotlin("android")
}

android {
    compileSdk = 33

    // buildToolVersion = "30.0.2"

    defaultConfig {
        applicationId = "id.psw.vshlauncher"
        minSdk = 19
        targetSdk = 33
        versionCode = 3
        versionName = "0.11"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true

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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.7.10")
    implementation("androidx.appcompat:appcompat:1.5.0")
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.media:media:1.6.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("com.google.android.material:material:1.6.1")
    implementation("com.github.penfeizhou.android.animation:awebp:2.17.2")
    implementation("com.github.penfeizhou.android.animation:apng:2.17.2")
    implementation("com.github.penfeizhou.android.animation:gif:2.17.2")
    implementation("androidx.test:rules:1.4.0")
    implementation("com.android.support:multidex:1.0.3")
    implementation(project(mapOf("path" to ":launcher_xlib")))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}

configurations {
    all {
        exclude(group = "androidx.lifecycle", module = "lifecycle-viewmodel-ktx")
    }
}
