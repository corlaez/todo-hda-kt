package com.corlaez.todo.tech

import com.corlaez.todo.*
import com.corlaez.util.checkboxToBoolean
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

class TodoKtor (private val todoUseCases: TodoUseCases) {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    private fun ApplicationCall.getFilter(): TodoFilter =
        request.queryParameters["selectedFilter"]
        ?.let(TodoFilter::valueOf)
        ?: TodoFilter.ALL
    private fun ApplicationCall.getId() = parameters["id"]?.toInt()
    private fun ApplicationCall.getTodoDTO(formParams: Parameters): TodoDTO {
        val id = getId()
        val content = formParams["content"]
        val completed = formParams["completed"].checkboxToBoolean().getOrThrow()
        return TodoDTO(
            id,
            content,
            completed,
        ).also { logger.debug(it.toString()) }
    }

    fun registerRoutes(routing: Routing) = with(routing) {
        // view list of todos
        get("/") {
            val filter = call.getFilter()
            val (list, todoInfo) = todoUseCases.list(filter)
            call.respondHtml {
                renderHtmx(TodoViewResponse(list, todoInfo, filter, autofocusMainInput = true))
            }
        }
        // view list of todos in edit mode
        get("/todos/{id}") {
            val filter = call.getFilter()
            val editingId = call.getId()!!
            val (list, todoInfo) = todoUseCases.list(filter)
            call.respondHtml {
                renderHtmx(TodoViewResponse(list, todoInfo, filter, editingId))
            }
        }
        // update all todos completed value
        patch("/todos/toggle") {
            val formParams = call.receiveParameters()
            val filter = call.getFilter()
            val todoDTO = call.getTodoDTO(formParams)
            todoUseCases.updateCompletedToAll(todoDTO.completed!!)
            val (list, todoInfo) = todoUseCases.list(filter)
            call.respondHtml {
                renderHtmx(TodoViewResponse(list, todoInfo, filter))
            }
        }
        // delete all completed todos
        delete("/todos/completed") {
            val filter = call.getFilter()
            todoUseCases.deleteCompleted()
            val (list, todoInfo) = todoUseCases.list(filter)
            call.respondHtml {
                renderHtmx(TodoViewResponse(list, todoInfo, filter))
            }
        }
        // create atodo
        post("/todo") {
            val formParams = call.receiveParameters()
            val filter = call.getFilter()
            val todoDTO = call.getTodoDTO(formParams).copy(completed = false)
            if (todoDTO.content.isNullOrBlank())
                call.respond(HttpStatusCode.NoContent)
            else {
                todoUseCases.addTodo(todoDTO)
                call.respondHtml {
                    val (list, todoInfo) = todoUseCases.list(filter)
                    renderHtmx(TodoViewResponse(list, todoInfo, filter, autofocusMainInput = true))
                }
            }
        }
        // delete atodo
        delete("/todo/{id}") {
            val filter = call.getFilter()
            val id = call.getId()!!
            todoUseCases.delete(id)
            val (list, todoInfo) = todoUseCases.list(filter)
            call.respondHtml {
                renderHtmx(TodoViewResponse(list, todoInfo, filter))
            }
        }
        // edit atodo
        patch("/todo/{id}") {
            val formParams = call.receiveParameters()
            val filter = call.getFilter()
            val todoDTO = call.getTodoDTO(formParams)
            todoDTO.id!!
            todoUseCases.update(todoDTO)
            val (list, todoInfo) = todoUseCases.list(filter)
            call.respondHtml {
                renderHtmx(TodoViewResponse(list, todoInfo, filter, autofocusMainInput = true))
            }
        }
    }
}
