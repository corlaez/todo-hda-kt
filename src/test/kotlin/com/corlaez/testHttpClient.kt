package com.corlaez

import org.htmlunit.*
import org.htmlunit.util.NameValuePair
import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.slf4j.LoggerFactory

const val testPort = 3033

val koinApp = RunMode.TEST.startKoin()

fun createWebClient(client4k: HttpHandler = JavaHttpClient()): WebClient =  WebClient().apply {
    options.isJavaScriptEnabled = true
    options.isThrowExceptionOnScriptError = true
    options.isCssEnabled = true
    ajaxController = NicelyResynchronizingAjaxController()
    webConnection = Http4kWebConnection(client4k)
}

private class Http4kWebConnection(private val client4k: HttpHandler) : WebConnection {
    private val logger = LoggerFactory.getLogger(Http4kWebConnection::class.java)

    override fun close() {}

    override fun getResponse(webRequest: WebRequest): WebResponse {
        logIncomingRequest(webRequest)
        val request4k = webRequest.toHtt4kRequest()
        val response4k = client4k(request4k)
        val webResponseData = response4k.toWebResponseData()
        return WebResponse(webResponseData, webRequest, 1000)
    }

    private fun WebRequest.http4kMethod() = Method.valueOf(httpMethod.toString())
    private fun WebRequest.http4kHeaders() = additionalHeaders.entries.map { (f, s) -> f to s }
    private fun Response.bodyAsBytes() = body.payload.let { buf -> ByteArray(buf.remaining()).also { buf.get(it) } }
    private fun Response.webHeaders() = headers.map { (k, v) -> NameValuePair(k, v) }
    private fun logIncomingRequest(webRequest: WebRequest) {
        val isHTMX = webRequest.getAdditionalHeader("HX-Request") == "true"
        val htmx = if(isHTMX) " htmx " else "      "
        if(!listOf("/todoApp.css","/learnDrawer.css").contains(webRequest.url.path))
            with(webRequest) { logger.info("$httpMethod$htmx$url") }
    }
    private fun WebRequest.toHtt4kRequest() = Request(http4kMethod(), url.toString())
        .let { r -> requestBody?.let { r.body(Body(it)) } ?: r }
        .headers(http4kHeaders())
        .let { r ->
            parameters.fold(r) { acc, p ->
                acc.query(p.name, p.value)
            }
        }
    private fun Response.toWebResponseData() = WebResponseData(bodyAsBytes(), status.code, status.description, webHeaders())
}
