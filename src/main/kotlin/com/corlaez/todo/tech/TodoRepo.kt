package com.corlaez.todo.tech

import com.corlaez.SqliteExposedConfig
import com.corlaez.todo.TodoDTO
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.dsl.module

interface TodoRepo {
    fun <X> openTransaction(block: () -> X): X
    fun listAll(): List<TodoDTO>
    fun listAllFilterCompleted(completed: Boolean): List<TodoDTO>
    fun insert(todo: TodoDTO)
    fun update(todo: TodoDTO)
    fun delete(id: Int)
    fun get(id: Int): TodoDTO
    fun updateCompletedToAll(value: Boolean)
    fun deleteCompleted()
}

val todoRepoModule = module {
    single<TodoRepo> { TodoRepoExposed(get()) }
}

// lib dependent code
private object TodoTable: IntIdTable() {
    val completed = bool("completed")
    val content = varchar("content", 50)
}

private fun ResultRow.toDTO() = TodoDTO(this[TodoTable.id].value,this[TodoTable.content],this[TodoTable.completed])
private fun Query.toTodoList() = this.mapLazy { it.toDTO() }.toList()

// Repo
private class TodoRepoExposed(sqliteExposedConfig: SqliteExposedConfig) : TodoRepo {
    init {
        sqliteExposedConfig.registerTable(TodoTable)
    }
    override fun <X> openTransaction(block: () -> X): X = transaction {
        block()
    }
    override fun listAll(): List<TodoDTO> {
        return TodoTable.selectAll().toTodoList()
    }
    override fun listAllFilterCompleted(completed: Boolean): List<TodoDTO> {
        return TodoTable.select { TodoTable.completed eq completed }.toTodoList()
    }
    override fun insert(todo: TodoDTO) {
        TodoTable.insert {
            it[completed] = todo.completed!!
            it[content] = todo.content!!
        }
    }
    override fun update(todo: TodoDTO) {
        TodoTable.update({ TodoTable.id eq todo.id } ) {
            if(todo.content != null) it[content] = todo.content
            if(todo.completed != null) it[completed] = todo.completed
        }
    }
    override fun delete(id: Int) {
        TodoTable.deleteWhere { TodoTable.id eq id }
    }
    override fun get(id: Int): TodoDTO {
        return TodoTable.select { TodoTable.id eq id }.first().toDTO()
    }
    override fun updateCompletedToAll(value: Boolean) {
        TodoTable.update { it[completed] = value }
    }
    override fun deleteCompleted() {
        TodoTable.deleteWhere { completed eq true }
    }
}
