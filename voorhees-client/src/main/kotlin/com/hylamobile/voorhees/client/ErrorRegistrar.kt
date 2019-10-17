package com.hylamobile.voorhees.client

import com.fasterxml.jackson.core.TreeNode
import com.hylamobile.voorhees.jsonrpc.*

class ErrorRegistrar {

    private val exceptions: MutableMap<Int, (Error) -> Unit> = mutableMapOf(
        ErrorCode.PARSE_ERROR.code to { error -> throw ParseErrorException(error.data) },
        ErrorCode.INVALID_REQUEST.code to { error -> throw InvalidRequestException(error.data) },
        ErrorCode.METHOD_NOT_FOUND.code to { error -> throw MethodNotFoundException(error.data) },
        ErrorCode.INVALID_PARAMS.code to { error -> throw InvalidParamsException(error.data) },
        ErrorCode.INTERNAL_ERROR.code to { error -> throw InternalErrorException(error.data) }
    )

    fun <T : JsonRpcException> registerException(errorCode: Int, exClass: Class<T>) {
        var defaultCons: ((Error) -> Unit)? = null
        var messageCons: ((Error) -> Unit)? = null
        var dataCons: ((Error) -> Unit)? = null
        var fullCons: ((Error) -> Unit)? = null

        for (cons in exClass.constructors) {
            val types = cons.genericParameterTypes
            when (types.size) {
                0 -> defaultCons = { error ->
                    throw cons.newInstance() as Exception
                }
                1 ->
                    if (types[0] == String::class.java) {
                        messageCons = { error ->
                            throw cons.newInstance(error.message) as Exception
                        }
                    }
                    else {
                        dataCons = { error ->
                            val node = error.data as TreeNode?
                            throw cons.newInstance(node.parseAs(types[0])) as Exception
                        }
                    }
                2 -> if (types[0] == String::class.java) {
                    fullCons = { error ->
                        val node = error.data as TreeNode?
                        throw cons.newInstance(error.message, node.parseAs(types[1])) as Exception
                    }
                }
            }
        }

        val cons = when {
            fullCons != null -> fullCons
            dataCons != null -> dataCons
            messageCons != null -> messageCons
            defaultCons != null -> defaultCons
            else -> throw IllegalArgumentException("Appropriate constructor not found")
        }

        exceptions[errorCode] = cons
    }

    fun handleError(error: Error) {
        val handler = exceptions.getOrDefault(error.code) { e -> throw CustomJsonRpcException(e) }
        handler(error)
    }
}
