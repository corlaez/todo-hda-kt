package com.corlaez

import com.corlaez.todo.todoModules
import com.corlaez.util.singleModuleOrNull
import com.corlaez.util.slf4jKoinLogger
import org.koin.core.KoinApplication
import org.koin.core.context.GlobalContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

enum class RunMode {
    PROD, NON_PROD, TEST;
    fun isFakeDb() = this != PROD

    fun startKoin(): KoinApplication {
        return GlobalContext.startKoin {
            slf4jKoinLogger(this@RunMode)
            modules(createAppModule())
        }
    }

    private fun createAppModule(): List<Module> {
        val sqliteModule = module {
            singleOf(::SqliteExposedConfig) withOptions { createdAtStart() }
        }

        return buildList {
            add({ this@RunMode }.singleModuleOrNull())
            if (!this@RunMode.isFakeDb())
                add(sqliteModule)
            addAll(todoModules)
        }.filterNotNull()
    }
}
