package com.corlaez

import com.corlaez.todo.TodoFilter
import org.htmlunit.Page
import org.htmlunit.html.HtmlElement
import org.htmlunit.html.HtmlInput
import org.htmlunit.html.HtmlPage
import org.htmlunit.html.Keyboard
import org.http4k.core.HttpHandler
import org.slf4j.LoggerFactory

class HttpPerson(handler: HttpHandler) : Person {
    private val logger = LoggerFactory.getLogger(TodoHtmlTest::class.java)
    private val webClient = createWebClient(handler)// Remove param to use real webClient

    lateinit var page: HtmlPage
    var pageXml = ""

    private val pathCheckbox = "input[@type='checkbox']"
    private fun pathWithText(t: String) = "*[normalize-space(text())='$t']"
    private fun fpathCheckboxBeforeText(t: String) = pathWithText(t).let {
        "//$it/preceding-sibling::$pathCheckbox | //$it/preceding::$pathCheckbox"
    }
    private val enterChar = Char(13)
    private fun String.asKeyboard() = this.fold(Keyboard()) { k, c -> k.type(c); k }
    private fun <E : HtmlElement> String.xpath() = page.getByXPath<E>(this)
    private fun autofocusInput() = "//*[@autofocus='autofocus']".xpath<HtmlInput>().first()
    private fun firstTextInput() = "//input[not(@type)] | //input[@type='text']".xpath<HtmlInput>().first()
    private fun elementWithText(text: String, i: Int = 0) = "//*[normalize-space(text())='$text']".xpath<HtmlElement>()[i]
    private fun inputWithText(text: String, i: Int = 0) = "//input[@value='$text']".xpath<HtmlInput>()[i]
    private fun elementToggleAll() = "//$pathCheckbox/following::*[@hx-patch]".xpath<HtmlElement>().first()
    private fun elementDeleteTodo(t: String) = "//${pathWithText(t)}/following::*[@hx-delete]".xpath<HtmlElement>().first()
    private fun checkboxPrecedingText(text: String) = fpathCheckboxBeforeText(text).xpath<HtmlInput>().last()// Last because they show in the order they appear
    private fun waitForBackgroundJs() {
        webClient.waitForBackgroundJavaScript(500)
        pageXml = page.asXml()
    }

    fun httpGetRequest(path: String) {
        logger.info("httpGetRequest $path")
        page = webClient.getPage("http://127.0.0.1:$port")
        waitForBackgroundJs()
    }

    fun getAutofocusInputValue(): String {
        return autofocusInput().value
    }

    fun isElementWithTextPresent(text: String): Boolean {
        return runCatching { elementWithText(text) != null }.getOrElse { false }
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