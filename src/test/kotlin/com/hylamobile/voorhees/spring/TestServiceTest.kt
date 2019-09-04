package com.hylamobile.voorhees.spring

import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.TextNode
import com.hylamobile.voorhees.jsonrpc.*
import org.hamcrest.CoreMatchers.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class TestServiceTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `call method with positional parameters should succeed`() {
        val request = Request("plus", ByPositionParams(listOf(IntNode(3), IntNode(4))), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(Json.serializeRequest(request)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("result").value(`is`(7)))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call method with named parameters should succeed`() {
        val request = Request("plus", ByNameParams(mapOf("l" to IntNode(3), "r" to IntNode(4))), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(Json.serializeRequest(request)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`(7)))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call of explicitly named parameters method with positional parameters should succeed`() {
        val request = Request("replicate", ByPositionParams(listOf(TextNode("test"), IntNode(3))), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(Json.serializeRequest(request)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("result").value(`is`("testtesttest")))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call of explicitly named parameters method with named parameters should succeed`() {
        val request = Request("replicate", ByNameParams(mapOf("str" to TextNode("test"), "times" to IntNode(3))), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(Json.serializeRequest(request)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`("testtesttest")))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call of default-parameters method with unspecified parameter should succeed`() {
        val request = Request("replicate", ByNameParams(mapOf("str" to TextNode("test"))), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(Json.serializeRequest(request)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`("testtest")))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call of default-parameters method with short parameter list should succeed`() {
        val request = Request("replicate", ByPositionParams(listOf(TextNode("test"))), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(Json.serializeRequest(request)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`("testtest")))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call with null as parameters should succeed`() {
        val request = Request("theAnswer", null, NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(Json.serializeRequest(request)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`(42)))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call with empty positional parameters should succeed`() {
        val request = Request("theAnswer", ByPositionParams(listOf()), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(Json.serializeRequest(request)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`(42)))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call with empty named parameters should succeed`() {
        val request = Request("theAnswer", ByNameParams(mapOf()), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(Json.serializeRequest(request)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`(42)))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call of absent method should fail`() {
        val request = Request("noSuchMethod", null, NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(Json.serializeRequest(request)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(nullValue()))
            .andExpect(jsonPath("error.code").value(`is`(-32601)))
            .andExpect(jsonPath("error.message").value(`is`("Method not found")))
            .andExpect(jsonPath("error.data").value(`is`("Method noSuchMethod not found")))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call with insufficient amount of positional parameters should fail`() {
        val request = Request("plus", ByPositionParams(listOf(IntNode(3))), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(Json.serializeRequest(request)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(nullValue()))
            .andExpect(jsonPath("error.code").value(`is`(-32602)))
            .andExpect(jsonPath("error.message").value(`is`("Invalid params")))
            .andExpect(jsonPath("error.data").value(`is`("Method plus with arguments [3] not found")))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call with insufficient amount of named parameters should fail`() {
        val request = Request("plus", ByNameParams(mapOf("l" to IntNode(3))), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(Json.serializeRequest(request)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(nullValue()))
            .andExpect(jsonPath("error.code").value(`is`(-32602)))
            .andExpect(jsonPath("error.message").value(`is`("Invalid params")))
            .andExpect(jsonPath("error.data").value(`is`("Method plus with arguments {l=3} not found")))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call with too many positional parameters should fail`() {
        val request = Request("plus", ByPositionParams(listOf(IntNode(3), IntNode(4), IntNode(5))), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(Json.serializeRequest(request)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(nullValue()))
            .andExpect(jsonPath("error.code").value(`is`(-32602)))
            .andExpect(jsonPath("error.message").value(`is`("Invalid params")))
            .andExpect(jsonPath("error.data").value(`is`("Method plus with arguments [3, 4, 5] not found")))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call with too many named parameters should fail`() {
        val params = mapOf("l" to IntNode(3), "r" to IntNode(4), "m" to IntNode(5))
        val request = Request("plus", ByNameParams(params), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(Json.serializeRequest(request)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(nullValue()))
            .andExpect(jsonPath("error.code").value(`is`(-32602)))
            .andExpect(jsonPath("error.message").value(`is`("Invalid params")))
            .andExpect(jsonPath("error.data").value(`is`("Method plus with arguments {l=3, r=4, m=5} not found")))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `internal error should be handled`() {
        val request = Request("breakALeg", null, NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(Json.serializeRequest(request)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(nullValue()))
            .andExpect(jsonPath("error.code").value(`is`(-32603)))
            .andExpect(jsonPath("error.message").value(`is`("Internal error")))
            .andExpect(jsonPath("error.data").value(`is`("A leg is broken")))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `custom error should be handled`() {
        val request = Request("breakAnArm", null, NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(Json.serializeRequest(request)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(nullValue()))
            .andExpect(jsonPath("error.code").value(`is`(-32603)))
            .andExpect(jsonPath("error.message").value(`is`("Internal error")))
            .andExpect(jsonPath("error.data").value(`is`("An arm is broken")))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `@DontExpose methods should not be exposed`() {
        val request = Request("unexposed", null, NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(Json.serializeRequest(request)))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(nullValue()))
            .andExpect(jsonPath("error.code").value(`is`(-32601)))
            .andExpect(jsonPath("error.message").value(`is`("Method not found")))
            .andExpect(jsonPath("error.data").value(`is`("Method unexposed not found")))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }
}
