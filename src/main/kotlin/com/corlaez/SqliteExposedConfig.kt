package com.corlaez

import com.corlaez.util.AppConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.sqlite.SQLiteConfig
import java.sql.Connection

class SqliteExposedConfig(private val runMode: RunMode) {
    private val sqlitePath = if (runMode.isFakeDb()) {
        null
    } else {
        "jdbc:sqlite:data.db"
    }

    init {
        if(!runMode.isFakeDb()) {
            AppConfig.registerInitFunction {
                Database.connect(sqlitePath!!, "org.sqlite.JDBC",
                    setupConnection = {
                        SQLiteConfig().apply {
//                        setSharedCache(true)
//                        setJournalMode(SQLiteConfig.JournalMode.MEMORY)
//                        setLockingMode(SQLiteConfig.LockingMode.WAL)
                            apply(it)
                        }
                    },
                )
                TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
            }
        }
    }

    fun registerTable(table: Table) {
        if (!runMode.isFakeDb()) {
            AppConfig.registerInitFunction {
                transaction { SchemaUtils.create(table) }
            }
        }
    }
}
