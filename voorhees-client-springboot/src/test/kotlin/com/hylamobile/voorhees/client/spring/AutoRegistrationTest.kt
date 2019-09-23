package com.hylamobile.voorhees.client.spring

import com.hylamobile.voorhees.client.spring.config.SpringJsonRpcClient
import com.hylamobile.voorhees.client.spring.user.FirstUserService
import com.hylamobile.voorhees.client.spring.user.SecondUserService
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
}
