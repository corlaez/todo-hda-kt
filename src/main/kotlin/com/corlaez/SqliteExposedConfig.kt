package com.corlaez

import com.corlaez.util.AppConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.sql.DriverManager

class SqliteExposedConfig {
    init {
        AppConfig.registerInitFunction {
            if (!System.getenv("PROD").toBoolean()) {
                val devSqlitePath = "jdbc:sqlite:file:test?mode=memory&cache=shared"
                Database.connect(devSqlitePath, "org.sqlite.JDBC")
                // Prevents the connection to be closed which would destroy the in memory db for sqlite
                DriverManager.getConnection(devSqlitePath)
            } else {
                Database.connect("jdbc:sqlite:/data/data.db", "org.sqlite.JDBC")
            }
            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        }
    }

    fun registerTable(table: Table) {
        if (!System.getenv("PROD").toBoolean()) {
            // Create tables for in memory db
            transaction { SchemaUtils.create(table) }
        }
    }
}
