package com.corlaez.todo

import com.corlaez.todo.tech.TodoHttp4k
import com.corlaez.todo.tech.TodoKtor
import com.corlaez.todo.tech.todoRepoModule
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val todoModules = module {
    singleOf(::TodoUseCases)
    singleOf(::TodoKtor)
    single { TodoHttp4k(get()) }
} + todoRepoModule
