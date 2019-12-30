package com.hylamobile.voorhees.server.spring

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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.util.Base64Utils

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class TestServiceTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `call method with positional parameters should succeed`() {
        val request = Request("plus", ByPositionParams(3, 4), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
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
        val request = Request("plus", ByNameParams("l" to 3, "r" to 4), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`(7)))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call of explicitly named parameters method with positional parameters should succeed`() {
        val request = Request("replicate", ByPositionParams("test", 3), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
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
        val request = Request("replicate", ByNameParams("str" to "test", "times" to 3), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`("testtesttest")))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call of default-parameters method with unspecified parameter should succeed`() {
        val request = Request("replicate", ByNameParams("str" to "test"), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`("testtest")))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call of default-parameters method with null parameters should succeed`() {
        val request = Request("replicate", null, NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`("xaxa")))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call of default-parameters method with empty positional parameters should succeed`() {
        val request = Request("replicate", ByPositionParams(), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`("xaxa")))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call of default-parameters method with empty named parameters should succeed`() {
        val request = Request("replicate", ByNameParams(), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`("xaxa")))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call of default-parameters method with short parameter list should succeed`() {
        val request = Request("replicate", ByPositionParams("test"), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
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
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`(42)))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call with empty positional parameters should succeed`() {
        val request = Request("theAnswer", ByPositionParams(), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`(42)))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call with empty named parameters should succeed`() {
        val request = Request("theAnswer", ByNameParams(), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
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
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
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
        val request = Request("plus", ByPositionParams(3), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
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
        val request = Request("plus", ByNameParams("l" to 3), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
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
        val request = Request("plus", ByPositionParams(3, 4, 5), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
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
        val request = Request("plus", ByNameParams("l" to 3, "r" to 4, "m" to 5), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
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
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
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
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
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
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(nullValue()))
            .andExpect(jsonPath("error.code").value(`is`(-32601)))
            .andExpect(jsonPath("error.message").value(`is`("Method not found")))
            .andExpect(jsonPath("error.data").value(`is`("Method unexposed not found")))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `POJO method with positional parameters should return POJO`() {
        val person = Person("johnny", 20)

        val request = Request("birthday", ByPositionParams(person), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result.age").value(`is`(21)))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `POJO method with named parameters should return POJO`() {
        val person = Person("johnny", 20)

        val request = Request("birthday", ByNameParams("person" to person), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result.age").value(`is`(21)))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `POJO collection method with positional parameters should return POJO`() {
        val person = Person("johnny", 20)

        val request = Request("birthdays", ByPositionParams(listOf(person)), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result[0].age").value(`is`(21)))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `POJO collection method with named parameters should return POJO`() {
        val person = Person("johnny", 20)

        val request = Request("birthdays", ByNameParams("people" to listOf(person)), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON)
                .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result[0].age").value(`is`(21)))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `null should be parsed as default values`() {
        val request = Request("checkNullDefaultValues", null, NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`(true)))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `null should be parsed as default values if parameters are positional`() {
        val request = Request("checkNullDefaultValues", ByPositionParams(), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`(true)))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `null should be parsed as default values if parameters are named`() {
        val request = Request("checkNullDefaultValues", ByNameParams(), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`(true)))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `secured endpoint should fail for anonymous`() {
        val request = Request("secret", null, NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/secured")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `secured endpoint should fail for unauthorized users`() {
        val request = Request("secret", null, NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/secured").basicAuth("user", "password")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isForbidden)
    }

    @Test
    fun `secured endpoint should succeed for admins`() {
        val request = Request("secret", null, NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/secured").basicAuth("admin", "password")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`("password")))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `notification should return nothing`() {
        val request = Request("plus", ByPositionParams(3, 4))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isAccepted)
            .andExpect(jsonPath("$").doesNotExist())
    }

    @Test
    fun `manually registered service should respond`() {
        val request = Request("ping", null, NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/manual")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("result").value(`is`("pong")))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `get method is not allowed`() {
        val request = Request("plus", ByPositionParams(3, 4), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.get("/api/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isMethodNotAllowed)
    }

    @Test
    fun `put method is not allowed`() {
        val request = Request("plus", ByPositionParams(3, 4), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.put("/api/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isMethodNotAllowed)
    }

    @Test
    fun `patch method is not allowed`() {
        val request = Request("plus", ByPositionParams(3, 4), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isMethodNotAllowed)
    }

    @Test
    fun `delete method is not allowed`() {
        val request = Request("plus", ByPositionParams(3, 4), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isMethodNotAllowed)
    }

    @Test
    fun `options method should return allowed methods`() {
        mockMvc.perform(MockMvcRequestBuilders.options("/api/test")
            .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNoContent)
            .andExpect(header().string("Allow", "OPTIONS, POST"))
    }

    @Test
    fun `options method with wrong accept header should fail`() {
        mockMvc.perform(MockMvcRequestBuilders.options("/api/test")
            .accept(MediaType.APPLICATION_XML))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNotAcceptable)
    }

    @Test
    fun `content_type text_xml is not supported`() {
        val request = Request("plus", ByPositionParams(3, 4), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
            .contentType(MediaType.TEXT_XML)
            .accept(MediaType.APPLICATION_JSON)
            .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isUnsupportedMediaType)
    }

    @Test
    fun `accept text_xml is not supported`() {
        val request = Request("plus", ByPositionParams(3, 4), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.TEXT_XML)
            .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNotAcceptable)
            .andExpect(content().string(MediaType.APPLICATION_JSON_VALUE))
    }

    @Test
    fun `call method with default prefix should succeed`() {
        val request = Request("test.plus", ByPositionParams(3, 4), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/prefix")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("result").value(`is`(7)))
            .andExpect(jsonPath("error").value(nullValue()))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    @Test
    fun `call method without default prefix should fail`() {
        val request = Request("plus", ByPositionParams(3, 4), NumberId(1))
        mockMvc.perform(MockMvcRequestBuilders.post("/api/prefix")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .accept(MediaType.APPLICATION_JSON)
            .content(request.jsonString))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("result").value(nullValue()))
            .andExpect(jsonPath("error.code").value(`is`(-32601)))
            .andExpect(jsonPath("error.message").value(`is`("Method not found")))
            .andExpect(jsonPath("error.data").value(`is`("Method plus not found")))
            .andExpect(jsonPath("id").value(`is`(1)))
            .andExpect(jsonPath("jsonrpc").value(`is`("2.0")))
    }

    private fun MockHttpServletRequestBuilder.basicAuth(username: String, password: String): MockHttpServletRequestBuilder =
        header("Authorization",
            "Basic ${Base64Utils.encodeToString("$username:$password".toByteArray(Charsets.UTF_8))}")
}
