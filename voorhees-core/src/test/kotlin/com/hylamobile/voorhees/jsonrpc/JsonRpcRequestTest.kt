package com.hylamobile.voorhees.jsonrpc

import com.fasterxml.jackson.databind.node.IntNode
import org.junit.Assert.*
import org.junit.Test
import java.io.StringWriter

class JsonRpcRequestTest {

    @Test
    fun `null request id should be serialized to null`() {
        val request = Request("eval").jsonString

        assertEquals("{\"method\":\"eval\",\"params\":null,\"id\":null,\"jsonrpc\":\"2.0\"}", request)
    }

    @Test
    fun `string request id should be serialized to text`() {
        val request = Request("eval", id = StringId("req0001")).jsonString

        assertEquals("{\"method\":\"eval\",\"params\":null,\"id\":\"req0001\",\"jsonrpc\":\"2.0\"}", request)
    }

    @Test
    fun `number request id should be serialized to numeric`() {
        val request = Request("eval", id = NumberId(42)).jsonString

        assertEquals("{\"method\":\"eval\",\"params\":null,\"id\":42,\"jsonrpc\":\"2.0\"}", request)
    }

    @Test
    fun `null request id should be deserialized to null`() {
        val request = "{\"method\":\"eval\",\"params\":null,\"id\":null,\"jsonrpc\":\"2.0\"}".parseRequest()

        assertEquals(null, request.id)
    }

    @Test
    fun `text request id should be deserialized to string`() {
        val request = "{\"method\":\"eval\",\"params\":null,\"id\":\"req0001\",\"jsonrpc\":\"2.0\"}".parseRequest()

        assertEquals(StringId("req0001"), request.id)
    }

    @Test
    fun `numeric request id should be deserialized to number`() {
        val request = "{\"method\":\"eval\",\"params\":null,\"id\":42,\"jsonrpc\":\"2.0\"}".parseRequest()

        assertEquals(NumberId(42), request.id)
    }

    @Test(expected = InvalidRequestException::class)
    fun `float request id should throw InvalidRequest`() {
        rethrowCause {
            "{\"method\":\"eval\",\"params\":null,\"id\":42.0,\"jsonrpc\":\"2.0\"}".parseRequest()
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `object request id should throw InvalidRequest`() {
        rethrowCause {
            "{\"method\":\"eval\",\"params\":null,\"id\":{},\"jsonrpc\":\"2.0\"}".parseRequest()
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `array request id should throw InvalidRequest`() {
        rethrowCause {
            "{\"method\":\"eval\",\"params\":null,\"id\":[],\"jsonrpc\":\"2.0\"}".parseRequest()
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `null version should throw InvalidRequest`() {
        rethrowCause {
            "{\"method\":\"eval\",\"params\":null,\"id\":null,\"jsonrpc\":null}".parseRequest()
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `numeric version should throw InvalidRequest`() {
        rethrowCause {
            "{\"method\":\"eval\",\"params\":null,\"id\":null,\"jsonrpc\":42}".parseRequest()
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `object version should throw InvalidRequest`() {
        rethrowCause {
            "{\"method\":\"eval\",\"params\":null,\"id\":null,\"jsonrpc\":{}}".parseRequest()
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `array version should throw InvalidRequest`() {
        rethrowCause {
            "{\"method\":\"eval\",\"params\":null,\"id\":null,\"jsonrpc\":[]}".parseRequest()
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `not 2_0 version should throw InvalidRequest`() {
        rethrowCause {
            "{\"method\":\"eval\",\"params\":null,\"id\":null,\"jsonrpc\":\"1.0\"}".parseRequest()
        }
    }

    @Test
    fun `2_0 version should be parsed`() {
        val request = "{\"method\":\"eval\",\"params\":null,\"id\":null,\"jsonrpc\":\"2.0\"}".parseRequest()

        assertEquals("2.0", request.jsonrpc.version)
    }

    @Test fun `null params should be parsed as null`() {
        val request = "{\"method\":\"eval\",\"params\":null,\"id\":null,\"jsonrpc\":\"2.0\"}".parseRequest()

        assertNull(request.params)
    }

    @Test fun `array params should be parsed as by-position params`() {
        val request = "{\"method\":\"eval\",\"params\":[1,2,3],\"id\":null,\"jsonrpc\":\"2.0\"}".parseRequest()

        assertNotNull(request.params)
        assertEquals(ByPositionParams::class.java, request.params?.javaClass)
        assertEquals(listOf(IntNode(1), IntNode(2), IntNode(3)), (request.params as ByPositionParams).params)
    }

    @Test fun `object params should be parsed as by-name params`() {
        val request = "{\"method\":\"eval\",\"params\":{\"first\": 1, \"second\": 2},\"id\":null,\"jsonrpc\":\"2.0\"}"
            .parseRequest()

        assertNotNull(request.params)
        assertEquals(ByNameParams::class.java, request.params?.javaClass)
        assertEquals(mapOf("first" to IntNode(1), "second" to IntNode(2)), (request.params as ByNameParams).params)
    }

    private inline fun rethrowCause(block: () -> Unit) {
        try {
            block()
        }
        catch (ex: Throwable) {
            throw ex.cause ?: ex
        }
    }
}
