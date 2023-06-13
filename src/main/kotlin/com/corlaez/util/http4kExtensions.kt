package com.corlaez.util

import kotlinx.html.HTML
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import org.http4k.core.ContentType
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Header
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("com.corlaez.respondHtml")

fun respondHtml(block: HTML.() -> Unit): Response {
    val text = buildString {
        append("<!DOCTYPE html>\n")
        appendHTML().html(block = block)
    }
    logger.trace(text)
    return Response(Status.OK)
        .with(Header.CONTENT_TYPE of ContentType.TEXT_HTML)
        .body(text)
}

fun cssResponse() = Response(Status.OK)
    .with(Header.CONTENT_TYPE of ContentType.Text("text/css"))
