package com.corlaez.todo.tech

import com.corlaez.todo.*
import com.corlaez.util.*
import kotlinx.html.html
import kotlinx.html.stream.createHTML
import org.http4k.core.*
import org.http4k.core.body.formAsMap
import org.http4k.routing.bind
import org.http4k.routing.path
import org.slf4j.LoggerFactory

class TodoHttp4k(private val todoUseCases: TodoUseCases) {
    private val logger = LoggerFactory.getLogger(this::class.java.name)
    private fun Request.getFilter(): TodoFilter =
        query("selectedFilter")
            ?.let(TodoFilter::valueOf)
            ?: TodoFilter.ALL
    private fun Request.getId() = path("id")?.toInt()
    private fun Request.getTodoDTO(formMap: Map<String, List<String?>>): TodoDTO {
        val id = getId()
        val content = formMap["content"]?.first()
        val completed = formMap["completed"]?.first().checkboxToBoolean().getOrThrow()
        return TodoDTO(
            id,
            content,
            completed,
        ).also { logger.debug(it.toString()) }
    }

    val routingArray = arrayOf(
        "/" bind Method.GET to {
            val filter = it.getFilter()
            val (list, todoInfo) = todoUseCases.list(filter)
            respondHtml {
                renderHtmx(TodoViewResponse(list, todoInfo, filter, autofocusMainInput = true))
            }
        },
        "/todos/{id}" bind Method.GET to {
            val filter = it.getFilter()
            val editingId = it.getId()!!
            val (list, todoInfo) = todoUseCases.list(filter)
            respondHtml {
                renderHtmx(TodoViewResponse(list, todoInfo, filter, editingId))
            }
        },
        "/todos/toggle" bind Method.PATCH to {
            val formParams = it.formAsMap()
            val filter = it.getFilter()
            val todoDTO = it.getTodoDTO(formParams)
            todoUseCases.updateCompletedToAll(todoDTO.completed!!)
            val (list, todoInfo) = todoUseCases.list(filter)
            respondHtml {
                renderHtmx(TodoViewResponse(list, todoInfo, filter))
            }
        },
        "/todos/completed" bind Method.DELETE to {
            val filter = it.getFilter()
            todoUseCases.deleteCompleted()
            val (list, todoInfo) = todoUseCases.list(filter)
            respondHtml {
                renderHtmx(TodoViewResponse(list, todoInfo, filter))
            }
        },
        "/todo" bind Method.POST to {
            val formParams = it.formAsMap()
            val filter = it.getFilter()
            val todoDTO = it.getTodoDTO(formParams).copy(completed = false)
            if (todoDTO.content.isNullOrBlank())
                Response(Status.NO_CONTENT)
            else {
                todoUseCases.addTodo(todoDTO)
                respondHtml {
                    val (list, todoInfo) = todoUseCases.list(filter)
                    renderHtmx(TodoViewResponse(list, todoInfo, filter, autofocusMainInput = true))
                }
            }
        },
        "/todo/{id}" bind Method.DELETE to {
            val filter = it.getFilter()
            val id = it.getId()!!
            todoUseCases.delete(id)
            val (list, todoInfo) = todoUseCases.list(filter)
            respondHtml {
                renderHtmx(TodoViewResponse(list, todoInfo, filter))
            }
        },
        "/todo/{id}" bind Method.PATCH to {
            val formParams = it.formAsMap()
            val filter = it.getFilter()
            val todoDTO = it.getTodoDTO(formParams)
            todoDTO.id!!
            todoUseCases.update(todoDTO)
            val (list, todoInfo) = todoUseCases.list(filter)
            respondHtml {
                renderHtmx(TodoViewResponse(list, todoInfo, filter, autofocusMainInput = true))
            }
        },
        "/todoApp.css" bind Method.GET to { cssResponse().body(todoAppCss) },
        "/learnDrawer.css" bind Method.GET to { cssResponse().body(todoCommonCss) },
    )
}