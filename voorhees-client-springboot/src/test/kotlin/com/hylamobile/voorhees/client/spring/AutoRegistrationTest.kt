package com.hylamobile.voorhees.client.spring

import com.fasterxml.jackson.databind.node.ArrayNode
import com.hylamobile.voorhees.client.spring.config.SpringJsonRpcClient
import com.hylamobile.voorhees.client.spring.error.GeneralException
import com.hylamobile.voorhees.client.spring.user.FirstUserService
import com.hylamobile.voorhees.client.spring.user.SecondUserService
import com.hylamobile.voorhees.client.spring.user.UserException
import com.hylamobile.voorhees.jsonrpc.CustomJsonRpcException
import com.hylamobile.voorhees.jsonrpc.Error
import com.hylamobile.voorhees.jsonrpc.Response
import com.hylamobile.voorhees.jsonrpc.jsonString
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.mockserver.client.server.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpEntity
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.RestTemplate

@RunWith(SpringRunner::class)
@SpringBootTest
class AutoRegistrationTest {

    @field:Autowired(required = false)
    private lateinit var defaultJsonRpcClient: SpringJsonRpcClient

    @field:Autowired(required = false)
    private lateinit var userServiceJsonRpcClient: SpringJsonRpcClient

    @field:Autowired(required = false)
    private lateinit var firstService: FirstService

    @field:Autowired(required = false)
    private lateinit var secondService: SecondService

    @field:Autowired(required = false)
    private lateinit var firstUserService: FirstUserService

    @field:Autowired(required = false)
    private lateinit var secondUserService: SecondUserService

    private val localServerPort: Int = 37143
    private lateinit var mockServer: MockServerClient

    @SpyBean
    private lateinit var userRestTemplate: RestTemplate

    @Before
    fun setUp() {
        mockServer = ClientAndServer.startClientAndServer(localServerPort)
    }

    @After
    fun tearDown() {
        mockServer.close()
    }

    @Test
    fun `clients should be registered`() {
        assertNotNull(defaultJsonRpcClient)
        assertNotNull(userServiceJsonRpcClient)
    }

    @Test
    fun `services should be registered`() {
        assertNotNull(firstService)
        assertNotNull(secondService)
        assertNotNull(firstUserService)
        assertNotNull(secondUserService)
    }

    @Test
    fun `default service should respond`() {
        mockServer.`when`(HttpRequest.request()
            .withMethod("POST")
            .withPath("/first")
            .withBody("{\"method\":\"plus\",\"params\":[3,4],\"id\":1,\"jsonrpc\":\"2.0\"}"))
            .respond(HttpResponse.response()
                .withStatusCode(200)
                .withBody("{\"result\":7,\"error\":null,\"id\":1,\"jsonrpc\":\"2.0\"}"))

        assertEquals(7, firstService.plus(3, 4))

        verify(userRestTemplate, never()).postForObject(
            anyString(), any(HttpEntity::class.java), any(Class::class.java))
    }

    @Test
    fun `user service should respond`() {
        mockServer.`when`(HttpRequest.request()
            .withMethod("POST")
            .withPath("/user-service/first")
            .withBody("{\"method\":\"plus\",\"params\":[3,4],\"id\":1,\"jsonrpc\":\"2.0\"}"))
            .respond(HttpResponse.response()
                .withStatusCode(200)
                .withBody("{\"result\":7,\"error\":null,\"id\":1,\"jsonrpc\":\"2.0\"}"))

        assertEquals(7, firstUserService.plus(3, 4))

        verify(userRestTemplate, times(1)).postForObject(
            anyString(), any(HttpEntity::class.java), any(Class::class.java))
    }

    @Test
    fun `user exception should be caught in user service`() {
        mockServer.`when`(HttpRequest.request()
            .withMethod("POST")
            .withPath("/user-service/first")
            .withBody("{\"method\":\"plus\",\"params\":[3,4],\"id\":1,\"jsonrpc\":\"2.0\"}"))
            .respond(HttpResponse.response()
                .withStatusCode(200)
                .withBody(Response.error(Error(42, "User exception", listOf(1, 2, 3))).jsonString))

        try {
            firstUserService.plus(3, 4)
        }
        catch (ex: UserException) {
            verify(userRestTemplate, times(1)).postForObject(
                anyString(), any(HttpEntity::class.java), any(Class::class.java))

            assertEquals(42, ex.error.code)
            assertEquals("User exception", ex.error.message)
            assertEquals(listOf(1, 2, 3), ex.error.data)
            return
        }

        fail()
    }

    @Test
    fun `user exception should be caught in default service`() {
        mockServer.`when`(HttpRequest.request()
            .withMethod("POST")
            .withPath("/first")
            .withBody("{\"method\":\"plus\",\"params\":[3,4],\"id\":1,\"jsonrpc\":\"2.0\"}"))
            .respond(HttpResponse.response()
                .withStatusCode(200)
                .withBody(Response.error(Error(42, "User exception", listOf(1, 2, 3))).jsonString))

        try {
            firstService.plus(3, 4)
        }
        catch (ex: UserException) {
            verify(userRestTemplate, never()).postForObject(
                anyString(), any(HttpEntity::class.java), any(Class::class.java))

            assertEquals(42, ex.error.code)
            assertEquals("User exception", ex.error.message)
            assertEquals(listOf(1, 2, 3), ex.error.data)
            return
        }

        fail()
    }

    @Test
    fun `general exception should not be caught in user service`() {
        mockServer.`when`(HttpRequest.request()
            .withMethod("POST")
            .withPath("/user-service/first")
            .withBody("{\"method\":\"plus\",\"params\":[3,4],\"id\":1,\"jsonrpc\":\"2.0\"}"))
            .respond(HttpResponse.response()
                .withStatusCode(200)
                .withBody(Response.error(Error(100, "General exception", listOf(1, 2, 3))).jsonString))

        try {
            firstUserService.plus(3, 4)
        }
        catch (ex: CustomJsonRpcException) {
            verify(userRestTemplate, times(1)).postForObject(
                anyString(), any(HttpEntity::class.java), any(Class::class.java))

            assertEquals(100, ex.error.code)
            assertEquals("General exception", ex.error.message)
            assertTrue(ex.error.data is ArrayNode)
            return
        }

        fail()
    }

    @Test
    fun `general exception should be caught in default service`() {
        mockServer.`when`(HttpRequest.request()
            .withMethod("POST")
            .withPath("/first")
            .withBody("{\"method\":\"plus\",\"params\":[3,4],\"id\":1,\"jsonrpc\":\"2.0\"}"))
            .respond(HttpResponse.response()
                .withStatusCode(200)
                .withBody(Response.error(Error(100, "General exception", listOf(1, 2, 3))).jsonString))

        try {
            firstService.plus(3, 4)
        }
        catch (ex: GeneralException) {
            verify(userRestTemplate, never()).postForObject(
                anyString(), any(HttpEntity::class.java), any(Class::class.java))

            assertEquals(100, ex.error.code)
            assertEquals("General exception", ex.error.message)
            assertEquals(listOf(1, 2, 3), ex.error.data)
            return
        }

        fail()
    }
}
