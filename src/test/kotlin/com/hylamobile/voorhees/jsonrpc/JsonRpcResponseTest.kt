package com.hylamobile.voorhees.jsonrpc

import com.fasterxml.jackson.databind.node.IntNode
import org.junit.Assert
import org.junit.Test
import java.io.StringReader
import java.io.StringWriter

class JsonRpcResponseTest {

    @Test
    fun `null response id should be serialized to null`() {
        val writer = StringWriter()
        Json.writeResponse(Response.success("result", null), writer)

        Assert.assertEquals("{\"result\":\"result\",\"error\":null,\"id\":null,\"jsonrpc\":\"2.0\"}", writer.toString())
    }

    @Test
    fun `string response id should be serialized to text`() {
        val writer = StringWriter()
        Json.writeResponse(Response.success("result", StringId("resp0001")), writer)

        Assert.assertEquals("{\"result\":\"result\",\"error\":null,\"id\":\"resp0001\",\"jsonrpc\":\"2.0\"}", writer.toString())
    }

    @Test
    fun `number request id should be serialized to numeric`() {
        val writer = StringWriter()
        Json.writeResponse(Response.success("result", NumberId(42)), writer)

        Assert.assertEquals("{\"result\":\"result\",\"error\":null,\"id\":42,\"jsonrpc\":\"2.0\"}", writer.toString())
    }

    @Test
    fun `null response id should be deserialized to null`() {
        val response = Json.readResponse(StringReader(
            "{\"result\":\"result\",\"error\":null,\"id\":null,\"jsonrpc\":\"2.0\"}"))

        Assert.assertEquals(null, response.id)
    }

    @Test
    fun `text response id should be deserialized to string`() {
        val response = Json.readResponse(StringReader(
            "{\"result\":\"result\",\"error\":null,\"id\":\"resp0001\",\"jsonrpc\":\"2.0\"}"))

        Assert.assertEquals(StringId("resp0001"), response.id)
    }

    @Test
    fun `numeric response id should be deserialized to number`() {
        val response = Json.readResponse(StringReader(
            "{\"result\":\"result\",\"error\":null,\"id\":42,\"jsonrpc\":\"2.0\"}"))

        Assert.assertEquals(NumberId(42), response.id)
    }

    @Test(expected = InvalidRequestException::class)
    fun `float response id should throw InvalidRequest`() {
        rethrowCause {
            Json.readResponse(StringReader(
                "{\"result\":\"result\",\"error\":null,\"id\":42.0,\"jsonrpc\":\"2.0\"}"))
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `object response id should throw InvalidRequest`() {
        rethrowCause {
            Json.readResponse(StringReader(
                "{\"result\":\"result\",\"error\":null,\"id\":{},\"jsonrpc\":\"2.0\"}"))
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `array response id should throw InvalidRequest`() {
        rethrowCause {
            Json.readResponse(StringReader(
                "{\"result\":\"result\",\"error\":null,\"id\":[],\"jsonrpc\":\"2.0\"}"))
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `null version should throw InvalidRequest`() {
        rethrowCause {
            Json.readResponse(StringReader(
                "{\"result\":\"result\",\"error\":null,\"id\":null,\"jsonrpc\":null}"))
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `numeric version should throw InvalidRequest`() {
        rethrowCause {
            Json.readResponse(StringReader(
                "{\"result\":\"result\",\"error\":null,\"id\":null,\"jsonrpc\":42}"))
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `object version should throw InvalidRequest`() {
        rethrowCause {
            Json.readResponse(StringReader(
                "{\"result\":\"result\",\"error\":null,\"id\":null,\"jsonrpc\":{}}"))
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `array version should throw InvalidRequest`() {
        rethrowCause {
            Json.readResponse(StringReader(
                "{\"result\":\"result\",\"error\":null,\"id\":null,\"jsonrpc\":[]}"))
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `not 2_0 version should throw InvalidRequest`() {
        rethrowCause {
            Json.readResponse(StringReader(
                "{\"result\":\"result\",\"error\":null,\"id\":null,\"jsonrpc\":\"1.0\"}"))
        }
    }

    @Test
    fun `2_0 version should be parsed`() {
        val response = Json.readResponse(StringReader(
            "{\"result\":\"result\",\"error\":null,\"id\":null,\"jsonrpc\":\"2.0\"}"))

        Assert.assertEquals("2.0", response.jsonrpc.version)
    }

    @Test
    fun `null result should be parsed as null`() {
        val response = Json.readResponse(StringReader(
            "{\"result\":null,\"error\":null,\"id\":null,\"jsonrpc\":\"2.0\"}"))

        Assert.assertNull(response.result)
    }

    @Test
    fun `null errors should be parsed as null`() {
        val response = Json.readResponse(StringReader(
            "{\"result\":null,\"error\":null,\"id\":null,\"jsonrpc\":\"2.0\"}"))

        Assert.assertNull(response.error)
    }

    @Test
    fun `object params should be parsed as by-name params`() {
        val request = Json.readRequest(StringReader(
            "{\"method\":\"eval\",\"params\":{\"first\": 1, \"second\": 2},\"id\":null,\"jsonrpc\":\"2.0\"}"))

        Assert.assertNotNull(request.params)
        Assert.assertEquals(ByNameParams::class.java, request.params?.javaClass)
        Assert.assertEquals(mapOf("first" to IntNode(1), "second" to IntNode(2)), (request.params as ByNameParams).params)
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
