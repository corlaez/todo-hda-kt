package com.corlaez

fun main() {
    val runModeString = System.getenv("env")?.lowercase()
    val runMode = if (runModeString == "prod") RunMode.PROD else RunMode.NON_PROD
    runMode.startKoin()
    Http4kApp().start(3031)
}
