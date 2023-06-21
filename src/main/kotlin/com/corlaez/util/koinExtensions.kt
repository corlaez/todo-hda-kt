package com.corlaez.util

import com.corlaez.RunMode
import org.koin.core.KoinApplication
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.koin.core.module.Module
import org.koin.dsl.module
import org.slf4j.LoggerFactory

fun KoinApplication.slf4jKoinLogger(runMode : RunMode): KoinApplication {
    val level = when(runMode) {
        RunMode.PROD -> Level.WARNING
        RunMode.NON_PROD -> Level.INFO
        RunMode.TEST -> Level.NONE
    }
    logger(object : Logger(level) {
        val slf4jLogger = LoggerFactory.getLogger("com.corlaez.Koin")
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

inline fun <reified T> (() -> T)?.singleModuleOrNull(): Module? {
    val fn = this ?: return null
    return module {
        single { fn() }
    }
}
