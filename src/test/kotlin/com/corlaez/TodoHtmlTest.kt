package com.corlaez

import com.corlaez.todo.TodoFilter
import org.junit.jupiter.api.*
import kotlin.test.*
import kotlin.test.Test

class TodoHtmlTest {
    val ka = koinApp// Ensures module is created before instanciating the component Http4kApp
    private val handlerProvider = Http4kApp()//.start() inits a real web server
    private val person = HttpPerson(handlerProvider.handlers())// Remove param to use real webClient
    private val expectedMainPageText = """
        Ktor + htmx
        Kotlin
        Kotlin Hypermedia Example
        Source
        Kotlin is a concise, strongly typed, cross-platform and fun language developed by Jetbrains. It's the recommended language to develop in Android but it is a general porpoise language and it can be used to write web servers.
        Kotlin
        If you have other helpful links to share, or find any of the links above no longer work, please let us know.
        todos
        unchecked
        Double-click to edit a todo
        Created by Armando Cordova
        Inpired by TodoMVC
    """.trimIndent()

    @AfterEach
    fun cleanup() {
        assertEquals(expectedMainPageText, person.page.asNormalizedText())
    }

    @Test
    fun createEditContentAndDeleteOneTodo() {
        val todoText = "Buy soda"
        val editedTodoText = "Buy soda and eat dinner"
        person.httpGetRequest("/")
        assertEquals(expectedMainPageText, person.page.asNormalizedText())

        person.createTodo(todoText)
        assertEquals("", person.getAutofocusInputValue(), "Expected input to be empty")
        assertTrue(person.isElementWithTextPresent(todoText))

        person.editTodoText("Buy soda", " and eat dinner")
        assertFalse(person.isElementWithTextPresent(todoText))
        assertTrue(person.isElementWithTextPresent(editedTodoText))


        person.deleteTodo(editedTodoText)
        assertFalse(person.isElementWithTextPresent(editedTodoText))
    }

    @Test
    fun createInDifferentFilters() {
        val todoText = "Buy soda"
        person.httpGetRequest("/")
        person.createTodo(todoText)

        person.selectFilter(TodoFilter.ALL)
        assertTrue(person.isElementWithTextPresent(todoText))
        person.selectFilter(TodoFilter.ACTIVE)
        assertTrue(person.isElementWithTextPresent(todoText))
        person.selectFilter(TodoFilter.COMPLETED)
        assertFalse(person.isElementWithTextPresent(todoText))

        val secondTodo = "Todo2"
        person.createTodo(secondTodo)
        assertFalse(person.isElementWithTextPresent(todoText))
        assertFalse(person.isElementWithTextPresent(secondTodo))

        val thirdTodo = "Todo3"
        person.selectFilter(TodoFilter.ACTIVE)
        person.createTodo(thirdTodo)
        assertTrue(person.isElementWithTextPresent(todoText))
        assertTrue(person.isElementWithTextPresent(secondTodo))
        assertTrue(person.isElementWithTextPresent(thirdTodo))

        person.selectFilter(TodoFilter.ALL)
        person.deleteTodo(todoText)
        person.deleteTodo(secondTodo)
        person.deleteTodo(thirdTodo)
    }

    @Test
    fun editTodoToggleAndUseFilters() {
        person.httpGetRequest("/")
        val todo = "todo1"
        person.createTodo(todo)

        person.selectFilter(TodoFilter.ALL)
        person.editTodoToggle(todo)
        assertTrue(person.isElementWithTextPresent(todo))
        person.selectFilter(TodoFilter.ACTIVE)
        assertFalse(person.isElementWithTextPresent(todo))
        person.selectFilter(TodoFilter.COMPLETED)
        assertTrue(person.isElementWithTextPresent(todo))

        person.editTodoToggle(todo)
        assertFalse(person.isElementWithTextPresent(todo))
        person.selectFilter(TodoFilter.ACTIVE)
        assertTrue(person.isElementWithTextPresent(todo))
        person.selectFilter(TodoFilter.ALL)
        assertTrue(person.isElementWithTextPresent(todo))

        person.deleteTodo(todo)
    }

    @Test
    fun bulkToggleBulkDelete() {
        person.httpGetRequest("/")
        assertEquals(expectedMainPageText, person.page.asNormalizedText())
        val todo1 = "todo1"
        val todo2 = "todo2"
        val todo3 = "todo3"
        person.createTodo(todo1)
        person.createTodo(todo2)
        person.createTodo(todo3)

        person.toggleAll()// Marks All as completed
        person.deleteAllCompleted()// Deletes all
    }

    @Test
    fun testHappyPath() {
        person.httpGetRequest("/")
        assertEquals(expectedMainPageText, person.page.asNormalizedText())

        val firstTodo = "Buy soda"
        person.createTodo(firstTodo)
        assertEquals("", person.getAutofocusInputValue(), "Expected input to be empty")
        assertTrue(person.isElementWithTextPresent(firstTodo))

        person.selectFilter(TodoFilter.COMPLETED)
        assertFalse(person.isElementWithTextPresent(firstTodo))

        person.selectFilter(TodoFilter.ACTIVE)
        assertTrue(person.isElementWithTextPresent(firstTodo))

        person.editTodoToggle(firstTodo)
        assertFalse(person.isElementWithTextPresent(firstTodo))

        person.selectFilter(TodoFilter.ALL)
        assertTrue(person.isElementWithTextPresent(firstTodo))

        val firstTodoEdit = " and Eat lunch"
        val firstTodoNewText = "$firstTodo$firstTodoEdit"
        person.editTodoText(firstTodo, firstTodoEdit)
        assertFalse(person.isElementWithTextPresent(firstTodo))
        assertTrue(person.isElementWithTextPresent(firstTodoNewText))

        person.selectFilter(TodoFilter.COMPLETED)
        assertTrue(person.isElementWithTextPresent(firstTodoNewText))

        person.deleteTodo(firstTodoNewText)
        assertFalse(person.isElementWithTextPresent(firstTodoNewText))

        person.createTodo("Buy bread")
        person.createTodo("Buy milk")
        person.createTodo("Sleep early")
        assertFalse(person.isElementWithTextPresent("Buy bread"))
        assertFalse(person.isElementWithTextPresent("Buy milk"))
        assertFalse(person.isElementWithTextPresent("Sleep early"))

        person.toggleAll()
        assertTrue(person.isElementWithTextPresent("Buy bread"))
        assertTrue(person.isElementWithTextPresent("Buy milk"))
        assertTrue(person.isElementWithTextPresent("Sleep early"))

        person.deleteAllCompleted()
        assertFalse(person.isElementWithTextPresent("Buy bread"))
        assertFalse(person.isElementWithTextPresent("Buy milk"))
        assertFalse(person.isElementWithTextPresent("Sleep early"))
    }
}
