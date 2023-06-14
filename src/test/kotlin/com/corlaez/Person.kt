package com.corlaez

import com.corlaez.todo.TodoFilter

interface Person {
    fun createTodo(content: String)
    fun editTodoText(text: String, newText: String)
    fun editTodoToggle(text: String)
    fun deleteTodo(content: String)
    fun toggleAll()
    fun deleteAllCompleted()
    fun selectFilter(todoFilter: TodoFilter)
}
