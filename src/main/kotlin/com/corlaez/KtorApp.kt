package com.corlaez

import com.corlaez.todo.tech.TodoKtor
import com.corlaez.todo.tech.TodoRepoModule
import com.corlaez.util.textFromWebjarWithArtifactId
import dagger.Component
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

@Component(modules = [TodoRepoModule::class])
interface TodoComponent {
    fun todoKtor(): TodoKtor
}

class KtorApp {
    fun start(port: Int) {
        embeddedServer(Netty, port) {
            val todoKtor = DaggerTodoComponent.create().todoKtor()

            routing {
                webjars()
                todoKtor.registerRoutes(this)
            }
        }.start(true)
    }
}

private fun Routing.webjars() {
    val htmxJs = textFromWebjarWithArtifactId("htmx.org", "htmx.js")
    get("/htmx.js") { call.respondText(htmxJs, ContentType.Application.JavaScript) }

    val todoAppCss = textFromWebjarWithArtifactId("todomvc-app-css", "index.css")
    get("/todoApp.css") { call.respondText(todoAppCss, ContentType.Text.CSS) }

    val todoCommonCss = textFromWebjarWithArtifactId("todomvc-common", "base.css")
    get("/learnDrawer.css") { call.respondText(todoCommonCss, ContentType.Text.CSS) }
}
