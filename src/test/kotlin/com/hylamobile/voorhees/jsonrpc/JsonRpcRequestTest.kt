package com.hylamobile.voorhees.jsonrpc

import com.fasterxml.jackson.databind.node.IntNode
import org.junit.Assert.*
import org.junit.Test
import java.io.StringReader
import java.io.StringWriter

class JsonRpcRequestTest {

    @Test
    fun `null request id should be serialized to null`() {
        val writer = StringWriter()
        Json.writeRequest(Request("eval"), writer)

        assertEquals("{\"method\":\"eval\",\"params\":null,\"id\":null,\"jsonrpc\":\"2.0\"}", writer.toString())
    }

    @Test
    fun `string request id should be serialized to text`() {
        val writer = StringWriter()
        Json.writeRequest(Request("eval", id = StringId("req0001")), writer)

        assertEquals("{\"method\":\"eval\",\"params\":null,\"id\":\"req0001\",\"jsonrpc\":\"2.0\"}", writer.toString())
    }

    @Test
    fun `number request id should be serialized to numeric`() {
        val writer = StringWriter()
        Json.writeRequest(Request("eval", id = NumberId(42)), writer)

        assertEquals("{\"method\":\"eval\",\"params\":null,\"id\":42,\"jsonrpc\":\"2.0\"}", writer.toString())
    }

    @Test
    fun `null request id should be deserialized to null`() {
        val request = Json.readRequest(StringReader(
            "{\"method\":\"eval\",\"params\":null,\"id\":null,\"jsonrpc\":\"2.0\"}"))

        assertEquals(null, request.id)
    }

    @Test
    fun `text request id should be deserialized to string`() {
        val request = Json.readRequest(StringReader(
            "{\"method\":\"eval\",\"params\":null,\"id\":\"req0001\",\"jsonrpc\":\"2.0\"}"))

        assertEquals(StringId("req0001"), request.id)
    }

    @Test
    fun `numeric request id should be deserialized to number`() {
        val request = Json.readRequest(StringReader(
            "{\"method\":\"eval\",\"params\":null,\"id\":42,\"jsonrpc\":\"2.0\"}"))

        assertEquals(NumberId(42), request.id)
    }

    @Test(expected = InvalidRequestException::class)
    fun `float request id should throw InvalidRequest`() {
        rethrowCause {
            Json.readRequest(StringReader(
                "{\"method\":\"eval\",\"params\":null,\"id\":42.0,\"jsonrpc\":\"2.0\"}"))
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `object request id should throw InvalidRequest`() {
        rethrowCause {
            Json.readRequest(StringReader(
                "{\"method\":\"eval\",\"params\":null,\"id\":{},\"jsonrpc\":\"2.0\"}"))
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `array request id should throw InvalidRequest`() {
        rethrowCause {
            Json.readRequest(StringReader(
                "{\"method\":\"eval\",\"params\":null,\"id\":[],\"jsonrpc\":\"2.0\"}"))
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `null version should throw InvalidRequest`() {
        rethrowCause {
            Json.readRequest(StringReader(
                "{\"method\":\"eval\",\"params\":null,\"id\":null,\"jsonrpc\":null}"))
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `numeric version should throw InvalidRequest`() {
        rethrowCause {
            Json.readRequest(StringReader(
                "{\"method\":\"eval\",\"params\":null,\"id\":null,\"jsonrpc\":42}"))
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `object version should throw InvalidRequest`() {
        rethrowCause {
            Json.readRequest(StringReader(
                "{\"method\":\"eval\",\"params\":null,\"id\":null,\"jsonrpc\":{}}"))
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `array version should throw InvalidRequest`() {
        rethrowCause {
            Json.readRequest(StringReader(
                "{\"method\":\"eval\",\"params\":null,\"id\":null,\"jsonrpc\":[]}"))
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `not 2_0 version should throw InvalidRequest`() {
        rethrowCause {
            Json.readRequest(StringReader(
                "{\"method\":\"eval\",\"params\":null,\"id\":null,\"jsonrpc\":\"1.0\"}"))
        }
    }

    @Test
    fun `2_0 version should be parsed`() {
        val request = Json.readRequest(StringReader(
            "{\"method\":\"eval\",\"params\":null,\"id\":null,\"jsonrpc\":\"2.0\"}"))

        assertEquals("2.0", request.jsonrpc.version)
    }

    @Test fun `null params should be parsed as null`() {
        val request = Json.readRequest(StringReader(
            "{\"method\":\"eval\",\"params\":null,\"id\":null,\"jsonrpc\":\"2.0\"}"))

        assertNull(request.params)
    }

    @Test fun `array params should be parsed as by-position params`() {
        val request = Json.readRequest(StringReader(
            "{\"method\":\"eval\",\"params\":[1,2,3],\"id\":null,\"jsonrpc\":\"2.0\"}"))

        assertNotNull(request.params)
        assertEquals(ByPositionParams::class.java, request.params?.javaClass)
        assertEquals(listOf(IntNode(1), IntNode(2), IntNode(3)), (request.params as ByPositionParams).params)
    }

    @Test fun `object params should be parsed as by-name params`() {
        val request = Json.readRequest(StringReader(
            "{\"method\":\"eval\",\"params\":{\"first\": 1, \"second\": 2},\"id\":null,\"jsonrpc\":\"2.0\"}"))

        assertNotNull(request.params)
        assertEquals(ByNameParams::class.java, request.params?.javaClass)
        assertEquals(mapOf("first" to IntNode(1), "second" to IntNode(2)), (request.params as ByNameParams).params)
    }

    private inline fun rethrowCause(block: () -> Unit) {
        try {
            block()
        }
        catch (ex: Throwable) {
            throw (ex.cause ?: ex)
        }
    }
}
