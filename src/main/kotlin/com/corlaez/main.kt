package com.corlaez

private val initFunctions = mutableListOf<() -> Unit>()
/** Make sure to call this method in companion objects init method to ensure registration is complete before DI starts*/
fun registerInitFunction(fn: () -> Unit) = initFunctions.add(fn)
fun registerInitFunction(i: Int, fn: () -> Unit) = initFunctions.add(i, fn)

fun main() {
    initFunctions.forEach { it() }
    KtorApp().start(3031)
}
