package com.corlaez

import com.corlaez.todo.todoModules
import com.corlaez.util.AppConfig
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

val appModule = module {
    singleOf(::SqliteExposedConfig) withOptions { createdAtStart() }
}
fun main() {
    startKoin {
        modules(appModule + todoModules)
    }
    AppConfig.initialize()
    KtorApp().start(3031)
}
