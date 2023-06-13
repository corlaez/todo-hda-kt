package com.corlaez

import com.corlaez.util.AppConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.sqlite.SQLiteConfig
import java.sql.Connection
import java.sql.DriverManager

class SqliteExposedConfig(private val runMode: RunMode) {
    private val sqlitePath = if (runMode.isDatabaseInMemory()) {
        "jdbc:sqlite:file:test?mode=memory&cache=shared"
    } else {
        "jdbc:sqlite:/data/data.db"
    }

    init {
        AppConfig.registerInitFunction {
            val db = Database.connect(sqlitePath, "org.sqlite.JDBC",
                setupConnection = {
                    SQLiteConfig().apply {
//                        setSharedCache(true)
//                        setJournalMode(SQLiteConfig.JournalMode.MEMORY)
//                        setLockingMode(SQLiteConfig.LockingMode.WAL)
                        apply(it)
                    }
                },
            )
            TransactionManager.defaultDatabase = db
            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
            // Prevents the connection to be closed which would destroy the state in the sqlite in-memory db
            if (runMode.isDatabaseInMemory())
                openConnection()
        }
    }

    fun registerTable(table: Table) {
        if (runMode.isDbSchemaCreatedDuringInit())
            transaction { SchemaUtils.create(table) }
    }

    private fun openConnection(): () -> Unit {
        val con = DriverManager.getConnection(sqlitePath)
        return { con.close() }
    }
}
