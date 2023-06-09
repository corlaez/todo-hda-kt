package com.corlaez.util

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.html.HTML
import kotlinx.html.html
import kotlinx.html.stream.appendHTML

suspend fun ApplicationCall.receiveParametersOrEmpty(): Parameters = runCatching {
    receiveParameters()
}.getOrElse { Parameters.Empty }
