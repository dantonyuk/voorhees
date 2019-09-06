package com.hylamobile.voorhees.jsonrpc

import com.fasterxml.jackson.databind.node.IntNode
import org.junit.Assert
import org.junit.Test

class JsonRpcResponseTest {

    @Test
    fun `null response id should be serialized to null`() {
        val response = Response.success("result", null).jsonString

        Assert.assertEquals("{\"result\":\"result\",\"error\":null,\"id\":null,\"jsonrpc\":\"2.0\"}", response)
    }

    @Test
    fun `string response id should be serialized to text`() {
        val response = Response.success("result", StringId("resp0001")).jsonString

        Assert.assertEquals(
            "{\"result\":\"result\",\"error\":null,\"id\":\"resp0001\",\"jsonrpc\":\"2.0\"}", response)
    }

    @Test
    fun `number request id should be serialized to numeric`() {
        val response = Response.success("result", NumberId(42)).jsonString

        Assert.assertEquals("{\"result\":\"result\",\"error\":null,\"id\":42,\"jsonrpc\":\"2.0\"}", response)
    }

    @Test
    fun `null response id should be deserialized to null`() {
        val response = "{\"result\":\"result\",\"error\":null,\"id\":null,\"jsonrpc\":\"2.0\"}".parseResponse()

        Assert.assertEquals(null, response.id)
    }

    @Test
    fun `text response id should be deserialized to string`() {
        val response = "{\"result\":\"result\",\"error\":null,\"id\":\"resp0001\",\"jsonrpc\":\"2.0\"}".parseResponse()

        Assert.assertEquals(StringId("resp0001"), response.id)
    }

    @Test
    fun `numeric response id should be deserialized to number`() {
        val response = "{\"result\":\"result\",\"error\":null,\"id\":42,\"jsonrpc\":\"2.0\"}".parseResponse()

        Assert.assertEquals(NumberId(42), response.id)
    }

    @Test(expected = InvalidRequestException::class)
    fun `float response id should throw InvalidRequest`() {
        rethrowCause {
            "{\"result\":\"result\",\"error\":null,\"id\":42.0,\"jsonrpc\":\"2.0\"}".parseResponse()
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `object response id should throw InvalidRequest`() {
        rethrowCause {
            "{\"result\":\"result\",\"error\":null,\"id\":{},\"jsonrpc\":\"2.0\"}".parseResponse()
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `array response id should throw InvalidRequest`() {
        rethrowCause {
            "{\"result\":\"result\",\"error\":null,\"id\":[],\"jsonrpc\":\"2.0\"}".parseResponse()
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `null version should throw InvalidRequest`() {
        rethrowCause {
            "{\"result\":\"result\",\"error\":null,\"id\":null,\"jsonrpc\":null}".parseResponse()
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `numeric version should throw InvalidRequest`() {
        rethrowCause {
            "{\"result\":\"result\",\"error\":null,\"id\":null,\"jsonrpc\":42}".parseResponse()
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `object version should throw InvalidRequest`() {
        rethrowCause {
            "{\"result\":\"result\",\"error\":null,\"id\":null,\"jsonrpc\":{}}".parseResponse()
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `array version should throw InvalidRequest`() {
        rethrowCause {
            "{\"result\":\"result\",\"error\":null,\"id\":null,\"jsonrpc\":[]}".parseResponse()
        }
    }

    @Test(expected = InvalidRequestException::class)
    fun `not 2_0 version should throw InvalidRequest`() {
        rethrowCause {
            "{\"result\":\"result\",\"error\":null,\"id\":null,\"jsonrpc\":\"1.0\"}".parseResponse()
        }
    }

    @Test
    fun `2_0 version should be parsed`() {
        val response = "{\"result\":\"result\",\"error\":null,\"id\":null,\"jsonrpc\":\"2.0\"}".parseResponse()

        Assert.assertEquals("2.0", response.jsonrpc.version)
    }

    @Test
    fun `null result should be parsed as null`() {
        val response = "{\"result\":null,\"error\":null,\"id\":null,\"jsonrpc\":\"2.0\"}".parseResponse()

        Assert.assertNull(response.result)
    }

    @Test
    fun `null errors should be parsed as null`() {
        val response = "{\"result\":null,\"error\":null,\"id\":null,\"jsonrpc\":\"2.0\"}".parseResponse()

        Assert.assertNull(response.error)
    }

    @Test
    fun `object params should be parsed as by-name params`() {
        val request = "{\"method\":\"eval\",\"params\":{\"first\": 1, \"second\": 2},\"id\":null,\"jsonrpc\":\"2.0\"}"
            .parseRequest()

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
