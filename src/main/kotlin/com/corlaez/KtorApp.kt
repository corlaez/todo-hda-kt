package com.corlaez

import com.corlaez.todo.tech.TodoKtor
import com.corlaez.util.textFromWebjarWithArtifactId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class KtorApp : KoinComponent {
    private val todoKtor: TodoKtor by inject()

    fun start(port: Int) {
        embeddedServer(Netty, port) {

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
