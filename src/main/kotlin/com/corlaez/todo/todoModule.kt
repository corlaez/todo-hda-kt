package com.corlaez.todo

import com.corlaez.todo.tech.TodoHttp4k
import com.corlaez.todo.tech.todoRepoModule
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val todoModules = module {
    factoryOf(::TodoUseCases)
    factoryOf(::TodoHttp4k)
} + todoRepoModule
