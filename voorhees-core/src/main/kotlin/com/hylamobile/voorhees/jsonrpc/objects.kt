package com.hylamobile.voorhees.jsonrpc

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

/**
 * JSON RPC version
 *
 * The only 2.0 version is supported.
 *
 * @property version String representation of the version object
 */
class Version private constructor(val version: String) {
    companion object {
        /**
         * [ver2_0] is used as a prototype to prevent creating similar
         * objects during JSON parsing.
         */
        val ver2_0 = Version("2.0")
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean =
        other is Version && other.version == version

    /**
     * @suppress
     */
    override fun hashCode(): Int = version.hashCode()
}

sealed class Id<T>
data class StringId(val id: String) : Id<String>()
data class NumberId(val id: Long) : Id<Long>()

// region request

data class Request @JsonCreator constructor(
    @param:JsonProperty("method")   val method: String,
    @param:JsonProperty("params")   val params: Params? = null,
    @param:JsonProperty("id")       val id: Id<*>? = null,
    @param:JsonProperty("jsonrpc")  val jsonrpc: Version = Version.ver2_0)

sealed class Params

data class ByPositionParams(val params: List<JsonNode>) : Params() {
    constructor(vararg nodes: Any?) :
        this(nodes.asSequence().map(Any?::jsonTree).toList())

    override fun toString(): String = params.toString()
}

data class ByNameParams constructor(val params: Map<String, JsonNode>) : Params() {
    constructor(vararg nodes: Pair<String, Any?>) :
        this(nodes.asSequence().map { (k, v) -> k to v.jsonTree }.toMap())

    override fun toString(): String = params.toString()
}

// endregion

// region response

data class Error(
    val code: Int,
    val message: String,
    val data: Any? = null)

data class Response<R>(
    val result: R? = null,
    val error: Error? = null,
    val id: Id<*>? = null,
    val jsonrpc: Version = Version.ver2_0) {

    companion object {
        fun <R> success(result: R, id: Id<*>? = null) =
            Response(result = result, id = id)

        fun error(error: Error, id: Id<*>? = null) =
            Response<Any>(error = error, id = id)

        fun parseError(data: Any?, id: Id<*>? = null) =
            error(ErrorCode.PARSE_ERROR.toError(data), id)

        fun invalidRequest(data: Any?, id: Id<*>? = null) =
            error(ErrorCode.INVALID_REQUEST.toError(data), id)

        fun methodNotFound(data: Any?, id: Id<*>? = null) =
            error(ErrorCode.METHOD_NOT_FOUND.toError(data), id)

        fun invalidParams(data: Any?, id: Id<*>? = null) =
            error(ErrorCode.INVALID_PARAMS.toError(data), id)

        fun internalError(data: Any?, id: Id<*>? = null) =
            error(ErrorCode.INTERNAL_ERROR.toError(data), id)
    }
}

data class ErrorCode(val code: Int, val message: String) {
    companion object {
        val PARSE_ERROR = ErrorCode(-32700, "Parse error")
        val INVALID_REQUEST = ErrorCode(-32600, "Invalid Request")
        val METHOD_NOT_FOUND = ErrorCode(-32601, "Method not found")
        val INVALID_PARAMS = ErrorCode(-32602, "Invalid params")
        val INTERNAL_ERROR = ErrorCode(-32603, "Internal error")
    }

    fun toError(data: Any? = null) = Error(code, message, data)
}

// endregion
