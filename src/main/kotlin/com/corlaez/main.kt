package com.corlaez

import com.corlaez.todo.todoModules
import com.corlaez.util.slf4jKoinLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

enum class RunMode {
    PROD, NON_PROD;
    fun isFakeDb() = this != PROD
}

val appModule = module {
    single<RunMode> {
        val runModeString = System.getenv("env")?.lowercase()
        if (runModeString == "prod") RunMode.PROD else RunMode.NON_PROD
    }
    singleOf(::SqliteExposedConfig) withOptions { createdAtStart() }
} + todoModules

fun main() {
    startKoin {
        slf4jKoinLogger()
        modules(appModule)
    }
    Http4kApp().start(3031)
}
