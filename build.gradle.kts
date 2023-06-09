plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    kotlin("kapt") version "1.8.21"
    id("io.ktor.plugin") version "2.3.1"
    application
}

group = "com.corlaez"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val daggerVersion: String by project
val exposedVersion: String by project

dependencies {
    // logger that ktor requires
    implementation("ch.qos.logback:logback-classic:1.4.7")
    // ktor server
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-html-builder")
    // webjars
    implementation("org.webjars:webjars-locator-core:0.52")
    implementation("org.webjars.npm:todomvc-app-css:2.4.1")
    implementation("org.webjars.npm:todomvc-common:1.0.5")
    implementation("org.webjars.npm:htmx.org:1.9.2")
    // di
    implementation("com.google.dagger:dagger:$daggerVersion")
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
    // db
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.xerial:sqlite-jdbc:3.30.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("com.corlaez.MainKt")
}
