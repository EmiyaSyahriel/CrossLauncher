
plugins {
    id("org.gradle.kotlin.kotlin-dsl") version "4.0.6"
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(gradleApi())
    implementation(localGroovy())
    implementation("com.android.tools.build:gradle:8.0.2")
}