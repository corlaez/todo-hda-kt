package com.corlaez

import com.corlaez.todo.todoModules
import com.corlaez.util.slf4jKoinLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

enum class RunMode {
    PROD, DEV, TEST;
    fun isDatabaseInMemory() = this != PROD
    fun isDbSchemaCreatedDuringInit() = this != PROD
}

val appModule = module {
    singleOf(::SqliteExposedConfig) withOptions { createdAtStart() }
} + todoModules

fun main(arg: Array<String>) {
    val runMode = if(arg.getOrNull(0)?.lowercase() == "prod") RunMode.PROD else RunMode.DEV
    startKoin {
        slf4jKoinLogger()
        modules(appModule + module { single { runMode } })
    }
    Http4kApp().start(3031)
}
