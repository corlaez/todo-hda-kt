package com.corlaez.todo

import com.corlaez.util.editOnEnterJs
import com.corlaez.util.htmxCheckboxFix
import kotlinx.html.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(TodoViewResponse::class.java.name)

fun TodoFilter.asQueryParam() = if(this == TodoFilter.ALL) "" else "selectedFilter=${this}"

data class TodoViewResponse(
    val todos: List<TodoDTO>,
    val todosInfo: TodosInfo,
    val selectedFilter: TodoFilter,
    val editingId: Int? = null,
    val autofocusMainInput: Boolean = false,
) {
    val filterQueryParam = selectedFilter.asQueryParam()

    init { logger.trace(this.toString()) }
}

fun HTML.renderHtmx(todoViewResponse: TodoViewResponse) {
    val todos = todoViewResponse.todos
    val selectedFilter = todoViewResponse.selectedFilter
    val editingId = todoViewResponse.editingId
    val todosInfo = todoViewResponse.todosInfo
    fun blockOrNone(b: Boolean) = if(b) "block" else "none"
    head {
        meta(charset = "utf-8")
        title { +"Ktor + htmx" }
        link(href="/todoApp.css", rel = "stylesheet")
        link(href="/learnDrawer.css", rel = "stylesheet")
        script { src = "/htmx.js"; async = true }
        script {
            unsafe {
                +htmxCheckboxFix
            }
        }
    }
    body(classes="learn-bar") {
        aside(classes = "learn") {
            header {
                h3 { +"Kotlin" }
                span(classes = "source-links") {
                  h5 { +"Kotlin Hypermedia Example" }
                  a(href = "https://github.com/corlaez") { +"Source" }
                }
            }
            hr()
            blockQuote(classes = "quote speech-bubble") {
                p { +"Kotlin is a concise, strongly typed, cross-platform and fun language developed by Jetbrains. It's the recommended language to develop in Android but it is a general porpoise language and it can be used to write web servers." }
                footer { a(href = "https://kotlinlang.org/") { +"Kotlin" } }
            }
            footer {
                hr()
                em {
                    +"If you have other helpful links to share, or find any of the links above no longer work, please "
                    a(href="https://github.com/corlaez") { +"let us know" }
                    +"."
                }
            }
        }
        section(classes = "todoapp") {
            // title and input
            header(classes = "header") {
                h1 { +"todos" }
                input(classes = "new-todo", name = "content") {
                    attributes["placeholder"] = "What needs to be done?"
                    attributes["hx-vals"] = "{\"completed\":\"off\"}"
                    attributes["hx-post"] = "/todo?${todoViewResponse.filterQueryParam}"
                    attributes["hx-target"] = "body"
                    attributes["hx-trigger"] = "keyup[key=='Enter']"
                    attributes["hx-on"] = "htmx:beforeRequest: event.target.readOnly = true"
                    required = true
                    autoFocus = todoViewResponse.autofocusMainInput
                }
            }
            // toggle and rows
            section(classes = "main") {
                attributes["style"] = "display: ${blockOrNone(todosInfo.isTotalNotEmpty)}"
                input(classes = "toggle-all", type = InputType.checkBox, name="completed") {
                    id="toggle-all"
                    checked = todosInfo.isAllCompleted
                }
                label {
                    // required because checkbox would be hx-included and therefore not fixed
                    attributes["hx-vals"] = "{\"completed\":${!todosInfo.isAllCompleted}}"
                    attributes["hx-patch"] = "/todos/toggle?${todoViewResponse.filterQueryParam}"
                    attributes["hx-target"] = "body"
                    attributes["for"] = "toggle-all"
                    +"Mark all as complete"
                }
                ul(classes = "todo-list") {
                    todos.forEach { todo ->
                        todo.id!!
                        todo.completed!!
                        todo.content!!
                        val editModeForThisTodo = editingId == todo.id
                        val liClasses = buildList {
                            if (todo.completed) add("completed")
                            if (editModeForThisTodo) add("editing")
                        }.joinToString(" ")
                        li(classes = liClasses) {
                            div(classes = "view") {
                                input(classes = "toggle", type = InputType.checkBox, name = "completed") {
                                    attributes["hx-patch"] = "/todo/${todo.id}?${todoViewResponse.filterQueryParam}"
                                    attributes["hx-target"] = "body"
                                    checked = todo.completed
                                }
                                label {
                                    attributes["hx-get"] = "/todos/${todo.id}?${todoViewResponse.filterQueryParam}"
                                    attributes["hx-target"] = "body"
                                    attributes["hx-trigger"] = "dblclick"
                                    +todo.content
                                }
                                button(classes = "destroy") {
                                    attributes["hx-delete"] = "/todo/${todo.id}?${todoViewResponse.filterQueryParam}"
                                    attributes["hx-target"] = "body"
                                }
                            }
                            if (editModeForThisTodo) {
                                input(classes = "edit", name = "content") {
                                    attributes["hx-get"] = "/?${todoViewResponse.filterQueryParam}"
                                    attributes["hx-target"] = "body"
                                    attributes["hx-trigger"] = "blur, keyup[key=='Escape']"
                                    attributes["onkeydown"] = "editOnEnter(event)"
                                    attributes["onfocus"] = "this.setSelectionRange(this.value.length,this.value.length);"
                                    autoFocus = true
                                    value = todo.content
                                }
                                script {
                                    unsafe {
                                        +editOnEnterJs(todo.id, todoViewResponse.filterQueryParam).trimMargin()
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // counter and buttons
            footer(classes = "footer") {
                attributes["style"] = "display: ${blockOrNone(todosInfo.isTotalNotEmpty)}"
                span(classes = "todo-count") { strong { +todosInfo.activeCount.toString() }; +" items left" }
                ul(classes = "filters") {
                    fun filterClass(forButton: TodoFilter) = if(selectedFilter == forButton) "selected" else ""
                    listOf("All" to TodoFilter.ALL, "Active" to TodoFilter.ACTIVE, "Completed" to TodoFilter.COMPLETED).forEach {
                        li {
                            a(classes = filterClass(it.second)) {
                                attributes["hx-get"] = "/?${it.second.asQueryParam()}"
                                attributes["hx-target"] = "body"
                                attributes["hx-push-url"] = "true"
                                +it.first
                            }
                        }
                    }
                }
                button(classes = "clear-completed") {
                    attributes["style"] = "display: " + if(todosInfo.isCompletedNotEmpty) "block" else "none"
                    attributes["hx-delete"] = "/todos/completed?${selectedFilter.asQueryParam()}"
                    attributes["hx-target"] = "body"
                    +"Clear Completed"
                }
            }
        }
        footer(classes="info") {
            p { +"Double-click to edit a todo" }
            p { +"Created by Armando Cordova" }
            p { +"Inpired by TodoMVC" }
        }
    }
}
