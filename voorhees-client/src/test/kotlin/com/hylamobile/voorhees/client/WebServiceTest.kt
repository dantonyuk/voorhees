package com.hylamobile.voorhees.client

import com.fasterxml.jackson.databind.node.TextNode
import com.hylamobile.voorhees.client.annotation.JsonRpcService
import com.hylamobile.voorhees.client.annotation.Param
import com.hylamobile.voorhees.jsonrpc.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockserver.client.server.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response

class WebServiceTest {

    companion object {
        @JsonRpcService(location = "/test")
        interface RemoteService {
            fun plus(l: Int, r: Int): Int

            fun replicate(@Param(name = "str") str: String): String

            fun replicate2(str: String): String

            fun breakALeg(): String

            fun breakAnArm(): String

            fun birthday(person: Person) = person.copy(age = person.age + 1)

            fun birthdays(people: List<Person>) = people.map { it.copy(age = it.age + 1) }
        }

        data class Person(var name: String, var age: Int) {
            @Suppress("UNUSED")
            constructor() : this("", 0)
        }
    }

    private val localServerPort: Int = 37143
    private lateinit var mockServer: MockServerClient

    @Before
    fun setUp() {
        mockServer = ClientAndServer.startClientAndServer(localServerPort)
    }

    @After
    fun tearDown() {
        mockServer.close()
    }

    private val client
        get() = JsonRpcClient.of(ServerConfig("http://localhost:$localServerPort/"))

    @Test
    fun `regular call should work`() {
        mockServer.`when`(request()
            .withMethod("POST")
            .withPath("/test")
            .withBody("{\"method\":\"plus\",\"params\":[3,4],\"id\":1,\"jsonrpc\":\"2.0\"}"))
            .respond(response()
                .withStatusCode(200)
                .withBody("{\"result\":7,\"error\":null,\"id\":1,\"jsonrpc\":\"2.0\"}"))

        val testService = client.getService(RemoteService::class.java)
        val result = testService.plus(3, 4)
        assertEquals(7, result)
    }

    @Test
    fun `short-call by named parameters should work`() {
        mockServer.`when`(request()
            .withMethod("POST")
            .withPath("/test")
            .withBody("{\"method\":\"replicate\",\"params\":{\"str\":\"test\"},\"id\":1,\"jsonrpc\":\"2.0\"}"))
            .respond(response()
                .withStatusCode(200)
                .withBody(Response.success("testtest", NumberId(1)).jsonString))

        val testService = client.getService(RemoteService::class.java)
        val result = testService.replicate("test")
        assertEquals("testtest", result)
    }

    @Test
    fun `short-call by positional parameters should work`() {
        mockServer.`when`(request()
            .withMethod("POST")
            .withPath("/test")
            .withBody("{\"method\":\"replicate2\",\"params\":[\"test\"],\"id\":1,\"jsonrpc\":\"2.0\"}"))
            .respond(response()
                .withStatusCode(200)
                .withBody(Response.success("testtest", NumberId(1)).jsonString))

        val testService = client.getService(RemoteService::class.java)
        val result = testService.replicate2("test")
        assertEquals("testtest", result)
    }

    @Test
    fun `internal error response should fail`() {
        mockServer.`when`(request()
            .withMethod("POST")
            .withPath("/test")
            .withBody("{\"method\":\"breakALeg\",\"params\":null,\"id\":1,\"jsonrpc\":\"2.0\"}"))
            .respond(response()
                .withStatusCode(200)
                .withBody(Response.internalError("A leg is broken", NumberId(1)).jsonString))

        try {
            val testService = client.getService(RemoteService::class.java)
            testService.breakALeg()
        } catch (e: JsonRpcException) {
            assertEquals(ErrorCode.INTERNAL_ERROR.code, e.error.code)
            assertEquals(ErrorCode.INTERNAL_ERROR.message, e.error.message)
            assertEquals("A leg is broken", (e.error.data as TextNode).textValue())
        }
    }

    @Test
    fun `custom exception response should fail`() {
        mockServer.`when`(request()
            .withMethod("POST")
            .withPath("/test")
            .withBody("{\"method\":\"breakAnArm\",\"params\":null,\"id\":1,\"jsonrpc\":\"2.0\"}"))
            .respond(response()
                .withStatusCode(200)
                .withBody(Response.internalError("An arm is broken", NumberId(1)).jsonString))

        try {
            val testService = client.getService(RemoteService::class.java)
            testService.breakAnArm()
        } catch (e: JsonRpcException) {
            assertEquals(ErrorCode.INTERNAL_ERROR.code, e.error.code)
            assertEquals(ErrorCode.INTERNAL_ERROR.message, e.error.message)
            assertEquals("An arm is broken", (e.error.data as TextNode).textValue())
        }
    }

    @Test
    fun `POJO method should work`() {
        mockServer.`when`(request()
            .withMethod("POST")
            .withPath("/test")
            .withBody("{\"method\":\"birthday\",\"params\":[{\"name\":\"johnny\",\"age\":20}],\"id\":1,\"jsonrpc\":\"2.0\"}"))
            .respond(response()
                .withStatusCode(200)
                .withBody(Response.success(Person("johnny", 21), NumberId(1)).jsonString))

        val testService = client.getService(RemoteService::class.java)
        val result = testService.birthday(Person("johnny", 20))
        assertEquals(21, result.age)
    }

    @Test
    fun `POJO collection method should work`() {
        mockServer.`when`(request()
            .withMethod("POST")
            .withPath("/test")
            .withBody("{\"method\":\"birthdays\",\"params\":[[{\"name\":\"johnny\",\"age\":20}]],\"id\":1,\"jsonrpc\":\"2.0\"}"))
            .respond(response()
                .withStatusCode(200)
                .withBody(Response.success(listOf(Person("johnny", 21)), NumberId(1)).jsonString))

        val testService = client.getService(RemoteService::class.java)
        val result = testService.birthdays(listOf(Person("johnny", 20)))
        assertEquals(21, result[0].age)
    }
}
