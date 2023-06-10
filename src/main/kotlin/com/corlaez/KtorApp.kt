package com.corlaez

import com.corlaez.todo.tech.TodoKtor
import com.corlaez.util.htmxJs
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory

class KtorApp : KoinComponent {
    private val todoKtor: TodoKtor by inject()
    private val logger = LoggerFactory.getLogger(KtorApp::class.java)

    fun start(port: Int) {
        embeddedServer(Netty, port) {
            routing {
                todoKtor.registerRoutes(this)
                get("/htmx.js") { call.respondText(htmxJs, ContentType.Application.JavaScript) }
            }
            logger.info("Responding at http://127.0.0.1:$port")
        }.start(false)
    }
}
