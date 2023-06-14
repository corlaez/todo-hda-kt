package com.corlaez.todo

import com.corlaez.todo.tech.TodoRepo

enum class TodoFilter { ALL, ACTIVE, COMPLETED }
data class TodosInfo(val activeCount: Int, val isTotalNotEmpty: Boolean, val isCompletedNotEmpty: Boolean) {
    val isAllCompleted = activeCount == 0 && isTotalNotEmpty
}

class TodoUseCases (private val todoRepo: TodoRepo) {
    fun addTodo(todoDTO: TodoDTO) = todoRepo.openTransaction {
        todoRepo.insert(todoDTO)
    }
    fun list(selectedFilter: TodoFilter): Pair<List<TodoDTO>, TodosInfo> = todoRepo.openTransaction {
        val allTodos = todoRepo.listAll()
        val (completedTodos, activeTodos) = allTodos.partition { it.completed == true }
        val filteredList = when(selectedFilter) {
            TodoFilter.ALL -> allTodos
            TodoFilter.ACTIVE -> activeTodos
            TodoFilter.COMPLETED -> completedTodos
        }
        filteredList to TodosInfo(activeTodos.size, allTodos.isNotEmpty(), completedTodos.isNotEmpty())
    }
    fun delete(id: Int) = todoRepo.openTransaction { todoRepo.delete(id) }
    fun get(id: Int) = todoRepo.openTransaction { todoRepo.get(id) }
    fun update(todoDTO: TodoDTO): Unit = todoRepo.openTransaction { todoRepo.update(todoDTO) }
    fun updateCompletedToAll(completed: Boolean) = todoRepo.openTransaction { todoRepo.updateCompletedToAll(completed) }
    fun deleteCompleted() = todoRepo.openTransaction { todoRepo.deleteCompleted() }
}
