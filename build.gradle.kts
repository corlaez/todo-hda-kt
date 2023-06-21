plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    application
}

group = "com.corlaez"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val exposedVersion: String by project
val slf4jVersion: String by project

dependencies {
    // webjars
    implementation("org.webjars.npm:htmx.org:1.9.2")
    implementation("org.webjars.npm:todomvc-app-css:2.4.1")// app css
    implementation("org.webjars.npm:todomvc-common:1.0.5") // learn sidebar css
    implementation("org.webjars:webjars-locator-core:0.52")
    // logger
    implementation("ch.qos.logback:logback-classic:1.4.8")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    // html dsl
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.8.1")
    // http4k server
    implementation(platform("org.http4k:http4k-bom:4.47.2.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-netty")
    // di
    implementation("io.insert-koin:koin-core:3.4.2")
    // db
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")
    // test
    testImplementation(kotlin("test"))
    testCompileOnly("org.htmlunit:htmlunit:3.3.0")
    testRuntimeOnly("org.htmlunit:htmlunit:3.3.0") {
        exclude("commons-logging", "commons-logging")
    }
    // Allows logback config to apply to libs using Jakarta Commons Logging (htmlunit includes jcl)
    testImplementation("org.slf4j:jcl-over-slf4j:$slf4jVersion")
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
