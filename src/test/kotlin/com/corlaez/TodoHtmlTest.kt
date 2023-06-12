package com.corlaez

import com.corlaez.todo.TodoFilter
import org.htmlunit.*
import org.htmlunit.html.*
import org.htmlunit.util.WebConnectionWrapper
import org.junit.jupiter.api.*
import org.koin.core.context.GlobalContext.startKoin
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private const val port = 3033
private val server = startKoin {
    modules(appModule)
}.let { Http4kApp() }.also { it.start(port) }
private val webClient = WebClient(). also {
    it.options.isJavaScriptEnabled = true
    it.options.isThrowExceptionOnScriptError = true
    it.options.isCssEnabled = true
    it.ajaxController = NicelyResynchronizingAjaxController()

//    it.webConnection = object : MockWebConnection()

    object : WebConnectionWrapper(it) {
        private val logger = LoggerFactory.getLogger(this::class.java)
        override fun getResponse(request: WebRequest): WebResponse {
            val response = super.getResponse(request)
            val isHTMX = request.getAdditionalHeader("HX-Request") == "true"
            val htmx = if(isHTMX) " htmx " else "      "
            if(!listOf("/todoApp.css","/learnDrawer.css").contains(request.url.path))
                with(request) { logger.info("$httpMethod$htmx$url") }
            return response
        }
    }
}

@TestMethodOrder(value = MethodOrderer.Random::class)
class TodoHtmlTest: Person {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val pathCheckbox = "input[@type='checkbox']"
    private fun pathWithText(t: String) = "*[normalize-space(text())='$t']"
    private fun fpathCheckboxBeforeText(t: String) = pathWithText(t).let {
        "//$it/preceding-sibling::$pathCheckbox | //$it/preceding::$pathCheckbox"
    }
    private fun <E : HtmlElement> String.xpath() = page.getByXPath<E>(this)
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
    private lateinit var page: HtmlPage
    private var pageXml = ""

    private fun String.asKeyboard() = this.fold(Keyboard()) { k, c -> k.type(c); k }

    private fun autofocusInput() = "//*[@autofocus='autofocus']".xpath<HtmlInput>().first()
    private fun firstTextInput() = "//input[not(@type)] | //input[@type='text']".xpath<HtmlInput>().first()
    private fun elementWithText(text: String, i: Int = 0) = "//*[normalize-space(text())='$text']".xpath<HtmlElement>()[i]
    private fun inputWithText(text: String, i: Int = 0) = "//input[@value='$text']".xpath<HtmlInput>()[i]
    private fun elementToggleAll() = "//$pathCheckbox/following::*[@hx-patch]".xpath<HtmlElement>().first()
    private fun elementDeleteTodo(t: String) = "//${pathWithText(t)}/following::*[@hx-delete]".xpath<HtmlElement>().first()
    private fun checkboxPrecedingText(text: String) = fpathCheckboxBeforeText(text).xpath<HtmlInput>().last()// Last because they show in the order they appear
    private fun waitForBackgroundJs() {
        webClient.waitForBackgroundJavaScript(1000)
        pageXml = page.asXml()
    }
    private val enterChar = Char(13)

    @Test
    fun createEditContentAndDeleteOneTodo() {
        val todoText = "Buy soda"
        val editedTodoText = "Buy soda and eat dinner"
        httpGetRequest("/")
        assertEquals(expectedMainPageText, page.asNormalizedText())

        createTodo(todoText)
        assertEquals("", autofocusInput().value, "Expected input to be empty")
        assertDoesNotThrow { elementWithText(todoText) }
        
        editTodoText("Buy soda", " and eat dinner")
        assertThrows<Throwable> { elementWithText(todoText) }
        assertDoesNotThrow { elementWithText(editedTodoText) }

        deleteTodo(editedTodoText)
        assertThrows<Throwable> { elementWithText(editedTodoText) }
    }

    @Test
    fun createInDifferentFilters() {
        val todoText = "Buy soda"
        httpGetRequest("/")
        createTodo(todoText)

        selectFilter(TodoFilter.ALL)
        assertDoesNotThrow { elementWithText(todoText) }
        selectFilter(TodoFilter.ACTIVE)
        assertDoesNotThrow { elementWithText(todoText) }
        selectFilter(TodoFilter.COMPLETED)
        val secondTodo = "Todo2"
        createTodo(secondTodo)
        assertThrows<Throwable> { elementWithText(todoText) }
        assertThrows<Throwable> { elementWithText(secondTodo) }

        val thirdTodo = "Todo3"
        selectFilter(TodoFilter.ACTIVE)
        createTodo(thirdTodo)
        assertDoesNotThrow { elementWithText(todoText) }
        assertDoesNotThrow { elementWithText(secondTodo) }
        assertDoesNotThrow { elementWithText(thirdTodo) }

        selectFilter(TodoFilter.ALL)
        deleteTodo(todoText)
        deleteTodo(secondTodo)
        deleteTodo(thirdTodo)
    }

    @Test
    fun editTodoToggleAndUseFilters() {
        httpGetRequest("/")
        val todo = "todo1"
        selectFilter(TodoFilter.ALL)
        createTodo(todo)

        editTodoToggle(todo)
        assertDoesNotThrow { elementWithText(todo) }
        selectFilter(TodoFilter.ACTIVE)
        assertThrows<Throwable> { elementWithText(todo) }
        selectFilter(TodoFilter.COMPLETED)
        assertDoesNotThrow { elementWithText(todo) }

        editTodoToggle(todo)
        assertThrows<Throwable> { elementWithText(todo) }
        selectFilter(TodoFilter.ACTIVE)
        assertDoesNotThrow { elementWithText(todo) }
        selectFilter(TodoFilter.ALL)
        assertDoesNotThrow { elementWithText(todo) }
    }

    @Test
    fun bulkToggleBulkDelete() {
        httpGetRequest("/")
        assertEquals(expectedMainPageText, page.asNormalizedText())
        selectFilter(TodoFilter.ALL)
        val todo1 = "todo1"
        val todo2 = "todo2"
        val todo3 = "todo3"
        createTodo(todo1)
        createTodo(todo2)
        createTodo(todo3)
        assertThrows<Exception> { deleteAllCompleted() }
    }

    @Test
    fun testHappyPath() {
        httpGetRequest("/")
        assertEquals(expectedMainPageText, page.asNormalizedText())

        val firstTodo = "Buy soda"
        createTodo(firstTodo)
        assertEquals("", autofocusInput().value, "Expected input to be empty")
        elementWithText(firstTodo)

        selectFilter(TodoFilter.COMPLETED)
        assertThrows<Throwable> { elementWithText(firstTodo) }

        selectFilter(TodoFilter.ACTIVE)
        elementWithText(firstTodo)

        editTodoToggle(firstTodo)
        assertThrows<Throwable> { elementWithText(firstTodo) }

        selectFilter(TodoFilter.ALL)
        elementWithText(firstTodo)

        val firstTodoEdit = " and Eat lunch"
        val firstTodoNewText = "$firstTodo$firstTodoEdit"
        editTodoText(firstTodo, firstTodoEdit)
        assertThrows<Throwable> { elementWithText(firstTodo) }
        assertNotNull(elementWithText(firstTodoNewText), "Expected edited todo to be found")

        selectFilter(TodoFilter.COMPLETED)
        elementWithText(firstTodoNewText)

        deleteTodo(firstTodoNewText)
        assertThrows<Throwable> { elementWithText(firstTodoNewText) }

        createTodo("Buy bread")
        createTodo("Buy milk")
        createTodo("Sleep early")
        assertThrows<Throwable> { elementWithText("Buy bread") }
        assertThrows<Throwable> { elementWithText("Buy milk") }
        assertThrows<Throwable> { elementWithText("Sleep early") }

        toggleAll()
        elementWithText("Buy bread")
        elementWithText("Buy milk")
        elementWithText("Sleep early")

        deleteAllCompleted()
        assertThrows<Throwable> { elementWithText("Buy bread") }
        assertThrows<Throwable> { elementWithText("Buy milk") }
        assertThrows<Throwable> { elementWithText("Sleep early") }
    }

    override fun httpGetRequest(path: String) {
        logger.info("httpGetRequest $path")
        page = webClient.getPage("http://127.0.0.1:$port")
        waitForBackgroundJs()
    }

    override fun createTodo(content: String) {
        logger.info("createTodo $content")
        firstTextInput().type("$content$enterChar".asKeyboard())
        waitForBackgroundJs()
    }

    override fun editTodoText(text: String, newText: String) {
        logger.info("editTodoText $text, $newText")
        logger.info("editTodoText:mouseOver")
        elementWithText(text).dblClick<Page>()
        waitForBackgroundJs()
        logger.info("editTodoText:clickButton")
        inputWithText(text).type("$newText$enterChar")
        waitForBackgroundJs()
    }

    override fun editTodoToggle(text: String) {
        logger.info("editTodoToggle $text")
        checkboxPrecedingText(text).click<Page>()
        waitForBackgroundJs()
    }

    override fun deleteTodo(content: String) {
        logger.info("deleteTodo $content")
        elementWithText(content).mouseOver()// to make delete button visible
        waitForBackgroundJs()
        elementDeleteTodo(content).click<Page>()
        waitForBackgroundJs()
    }

    override fun toggleAll() {
        logger.info("toggleAll")
        elementToggleAll().click<Page>()
        waitForBackgroundJs()
    }

    override fun deleteAllCompleted() {
        logger.info("deleteAllCompleted")
        elementWithText("Clear Completed").click<Page>()
        waitForBackgroundJs()
    }

    override fun selectFilter(todoFilter: TodoFilter) {
        logger.info("selectFilter $todoFilter")
        val text = when (todoFilter) {
            TodoFilter.ALL -> "All"
            TodoFilter.ACTIVE -> "Active"
            TodoFilter.COMPLETED -> "Completed"
        }
        elementWithText(text).click<Page>()
        waitForBackgroundJs()
    }
}
