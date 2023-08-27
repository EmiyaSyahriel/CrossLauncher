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
    val fmt = SimpleDateFormat("yyyy:MM:dd:HH:mm")
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

    buildTypes{
        val buildDate = "\"${getDate()}\""
        getByName("release"){
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug"){
            isMinifyEnabled = false
        }
        all {
            buildConfigField("String", "BUILD_DATE",buildDate)
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

    tasks.create<GenerateEmbeddedResourceCppSourceTask>("generateEmbeddedResourceCppSource"){
        inputDir = file("src/main/cpp/res")
        inputExtensions = arrayOf("frag","vert")
        cppNamespace = "R"
        cppDelimiter = "EMBEDRES"
        cppIncludes = arrayOf("SHADERS.HPP")
        outputHeaderFile = file("src/main/cpp/wave/SHADERS.HPP")
        outputSourceFile = file("src/main/cpp/wave/SHADERS.CPP")
    }

}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar") )))
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.media:media:1.6.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.github.penfeizhou.android.animation:awebp:2.17.2")
    implementation("com.github.penfeizhou.android.animation:apng:2.17.2")
    implementation("com.github.penfeizhou.android.animation:gif:2.17.2")
    implementation("androidx.test:rules:1.5.0")
    implementation("com.android.support:multidex:1.0.3")
    implementation(project(mapOf("path" to ":launcher_xlib")))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    constraints {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.0") {
            because("kotlin-stdlib-jdk7 is now a part of kotlin-stdlib")
        }
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.0") {
            because("kotlin-stdlib-jdk8 is now a part of kotlin-stdlib")
        }
    }
}

configurations {
    all {
        exclude(group = "androidx.lifecycle", module = "lifecycle-viewmodel-ktx")
    }
}
