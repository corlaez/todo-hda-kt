package com.corlaez

import com.corlaez.todo.tech.TodoHttp4k
import com.corlaez.util.*
import org.http4k.core.*
import org.http4k.lens.Header.CONTENT_TYPE
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory

class Http4kApp : KoinComponent {
    private val todoHttp4k: TodoHttp4k by inject()
    private val logger = LoggerFactory.getLogger(Http4kApp::class.java)

    init {
        AppConfig.initialize()
    }

    fun start(port: Int) {
        logger.warn("Responding at http://127.0.0.1:$port")
        handlers().asServer(Netty(port)).start()
    }

    private fun jsResponse() = Response(Status.OK).with(CONTENT_TYPE of ContentType.Text("application/javascript"))

    fun handlers() = routes(
        *todoHttp4k.routingArray,
        "/htmx.js" bind Method.GET to { jsResponse().body(htmxJs) },
    )
}
