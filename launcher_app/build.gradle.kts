import java.util.Date
import java.text.SimpleDateFormat

plugins{
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    kotlin("android")
}

fun getDate() : String {
    val date = Date().time
    val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    return fmt.format(date)
}

android {
    compileSdk = 33
    namespace = "id.psw.vshlauncher"
    // buildToolVersion = "30.0.2"

    buildFeatures{
        buildConfig = true
    }

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

    buildTypes {
        val buildDate = "\"${getDate()}\""
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
        }
        all {
            buildConfigField("String", "BUILD_DATE",buildDate)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    externalNativeBuild {
        cmake {
            // relativePath("src/main/cpp/CMakeLists.txt")
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    tasks.create<GenerateEmbeddedResourceCppSourceTask>("generateEmbeddedResourceCppSource"){
        inputDir = file("src/main/cpp/res")
        inputExtensions = arrayOf("frag","vert")
        cppNamespace = "R"
        cppDelimiter = "EMBEDRES"
        cppIncludes = arrayOf("SHADERS.HPP")
        outputHeaderFile = file("src/main/cpp/wave/SHADERS.HPP")
        outputSourceFile = file("src/main/cpp/wave/SHADERS.CPP")
    }

    tasks.create<ForgeButtonFont>("generateButtonFont") {
        sourceFile = project.rootProject.file("other_asset/vshbtn/vshbtn.svg")
        targetFile = project.rootProject.file("other_asset/vshbtn/vshbtn.ttf")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar") )))

    // Kotlin and Android Compatibility Library
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.10.1")

    // Kotlin Coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
    implementation("androidx.lifecycle:lifecycle-process:2.4.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")

    // Date Time Compatibility Library
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.6")

    // Media Library
    implementation("androidx.media:media:1.6.0")

    // Animated Icon Library
    implementation("com.github.penfeizhou.android.animation:awebp:2.17.2")
    implementation("com.github.penfeizhou.android.animation:apng:2.17.2")
    implementation("com.github.penfeizhou.android.animation:gif:2.17.2")

    // Multidex support (This project has >65000 methods)
    implementation("com.android.support:multidex:1.0.3")

    // JSON Parsing
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(project(mapOf("path" to ":launcher_xlib")))

    // Test Implementation
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:rules:1.5.0")

    constraints {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.0") {
            because("kotlin-stdlib-jdk7 is now a part of kotlin-stdlib")
        }
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.0") {
            because("kotlin-stdlib-jdk8 is now a part of kotlin-stdlib")
        }
    }

    debugImplementation(kotlin("reflect"))
}

configurations {
    all {
        exclude(group = "androidx.lifecycle", module = "lifecycle-viewmodel-ktx")
    }
}
