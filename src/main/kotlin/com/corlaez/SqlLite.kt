package com.corlaez

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection
import java.sql.DriverManager

class SqlLite {
    init {
        registerInitFunction(0) {
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
}
