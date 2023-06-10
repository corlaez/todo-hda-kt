package com.corlaez.util

import org.koin.core.KoinApplication
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.slf4j.LoggerFactory

fun KoinApplication.slf4jKoinLogger(level: Level = Level.INFO): KoinApplication {
    logger(object : Logger(level) {
        val slf4jLogger = LoggerFactory.getLogger("Koin")
        override fun display(level: Level, msg: MESSAGE) {
            when(level) {
                Level.DEBUG -> slf4jLogger.debug(msg)
                Level.INFO -> slf4jLogger.info(msg)
                Level.WARNING -> slf4jLogger.warn(msg)
                Level.ERROR -> slf4jLogger.error(msg)
                Level.NONE -> Unit
            }
        }
    })
    return this
}
