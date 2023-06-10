package com.corlaez.util

/** Make sure to call this method in companion objects init method to ensure registration is complete before DI starts*/

object AppConfig {
    private val initFunctions = mutableListOf<() -> Unit>()
    fun registerInitFunction(fn: () -> Unit) = initFunctions.add(fn)

    fun initialize() {
        initFunctions.forEach { it() }
    }
}