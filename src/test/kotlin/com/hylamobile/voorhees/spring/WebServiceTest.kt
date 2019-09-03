package com.hylamobile.voorhees.spring

import com.fasterxml.jackson.databind.node.TextNode
import com.hylamobile.voorhees.client.JsonRpcClient
import com.hylamobile.voorhees.client.ServerConfig
import com.hylamobile.voorhees.client.annotation.Param
import com.hylamobile.voorhees.jsonrpc.ErrorCode
import com.hylamobile.voorhees.jsonrpc.JsonRpcException
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebServiceTest {

    companion object {
        interface RemoteService {
            fun plus(l: Int, r: Int): Int

            fun replicate(@Param(name = "str") str: String): String

            fun breakALeg(): String

            fun breakAnArm(): String
        }
    }

    @LocalServerPort
    var localServerPort: Int = 0

    @Test
    fun testRemote() {
        val client = JsonRpcClient(ServerConfig("http://localhost:${localServerPort}"))
        val testService = client.getService("/test", RemoteService::class.java)
        val result = testService.plus(3, 4)
        assertEquals(7, result)
    }

    @Test
    fun testDefault() {
        val client = JsonRpcClient(ServerConfig("http://localhost:${localServerPort}"))
        val testService = client.getService("/test", RemoteService::class.java)
        val result = testService.replicate("test")
        assertEquals("testtest", result)
    }

    @Test
    fun testJsonError() {
        try {
            val client = JsonRpcClient(ServerConfig("http://localhost:${localServerPort}"))
            val testService = client.getService("/test", RemoteService::class.java)
            testService.breakALeg()
        } catch (e: JsonRpcException) {
            assertEquals(ErrorCode.INTERNAL_ERROR.code, e.error.code)
            assertEquals(ErrorCode.INTERNAL_ERROR.message, e.error.message)
            assertEquals("A leg is broken", (e.error.data as TextNode).textValue())
        }
    }

    @Test
    fun testException() {
        try {
            val client = JsonRpcClient(ServerConfig("http://localhost:${localServerPort}"))
            val testService = client.getService("/test", RemoteService::class.java)
            testService.breakAnArm()
        } catch (e: JsonRpcException) {
            assertEquals(ErrorCode.INTERNAL_ERROR.code, e.error.code)
            assertEquals(ErrorCode.INTERNAL_ERROR.message, e.error.message)
            assertEquals("An arm is broken", (e.error.data as TextNode).textValue())
        }
    }
}
