package com.corlaez.todo.tech

import com.corlaez.RunMode
import com.corlaez.SqliteExposedConfig
import com.corlaez.todo.TodoDTO
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.dsl.module
import java.util.concurrent.Semaphore

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
    factory<TodoRepo> {
        val runMode: RunMode = get()
        if (runMode.isFakeDb()) TodoRepoFake()
        else TodoRepoExposed(get())
    }
}

private class TodoRepoFake: TodoRepo {
    private var lastId = 0
    private val orderPreservingMap = LinkedHashMap<Int, TodoDTO>()

    override fun <X> openTransaction(block: () -> X): X {
        return block()
    }

    override fun listAll(): List<TodoDTO> = orderPreservingMap.values.toList()

    override fun listAllFilterCompleted(completed: Boolean): List<TodoDTO> = listAll()
        .filter { it.completed == completed }

    override fun insert(todo: TodoDTO) = (++lastId)
        .let { todo.copy(id = it) }
        .let { orderPreservingMap[it.id!!] = it }

    override fun update(todo: TodoDTO) {
        orderPreservingMap[todo.id!!]
            ?.let { if(todo.content != null) it.copy(content = todo.content) else it }
            ?.let { if(todo.completed != null) it.copy(completed = todo.completed) else it }
            ?.let { orderPreservingMap[todo.id] = it }
    }

    override fun delete(id: Int) { orderPreservingMap.remove(id) }

    override fun get(id: Int): TodoDTO = orderPreservingMap[id]!!

    override fun updateCompletedToAll(value: Boolean) {
        orderPreservingMap.keys.forEach { id ->
            orderPreservingMap[id]?.copy(completed = value)?.let { todo ->
                orderPreservingMap[id] = todo
            }
        }
    }

    override fun deleteCompleted() {
        // copy the ids list to avoid concurrent modification (changing while keys iterator is in use)
        orderPreservingMap.keys.toList().forEach { id ->
            if (orderPreservingMap[id]?.completed == true) {
                orderPreservingMap.remove(id)
            }
        }
    }
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
    val semaphore = Semaphore(1)// sql lite having lock issues

    init {
        sqliteExposedConfig.registerTable(TodoTable)
    }
    override fun <X> openTransaction(block: () -> X): X = transaction {
        semaphore.acquire()
        val x = block()
        semaphore.release()
        x
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
