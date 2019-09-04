package com.hylamobile.voorhees.server.spring.webmvc

import com.fasterxml.jackson.databind.JsonNode
import com.hylamobile.voorhees.server.annotations.Param
import com.hylamobile.voorhees.server.annotations.normalizedDefault
import com.hylamobile.voorhees.jsonrpc.*
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JsonRpcMethodHandler(private val bean: Any, private val method: Method) : JsonRpcHandler {

    companion object {
        const val REQ_ATTR_NAME = "jsonrpc:request"

        data class MethodParameter(
            val name: String,
            val type: Class<*>,
            val defaultValue: String? = null)
    }

    private val paramByName: LinkedHashMap<String, MethodParameter> =
        method.parameters.map {
            when (val anno = it.getAnnotation(Param::class.java)) {
                null -> MethodParameter(it.name, it.type)
                else -> {
                    val name = anno.name.ifEmpty { it.name }
                    val defaultValue = anno.defaultValue.normalizedDefault
                    MethodParameter(name, it.type, defaultValue)
                }
            }
        }
        .map { it.name to it }
        .toMap(LinkedHashMap())

    private val parameters = paramByName.values
    private val paramNames = parameters.map { it.name }

    private val requiredParamNames =
        paramByName.values.filter { it.defaultValue == null }.map { it.name }

    fun compatibleWith(requestParams: Params?): Boolean {
        fun checkNull() = method.parameterCount == 0

        fun checkPos(requestParams: ByPositionParams) =
            requestParams.params.size.let {
                requiredParamNames.size <= it && it <= method.parameterCount
            }

        fun checkNamed(requestParams: ByNameParams) =
            requestParams.params.keys.let {
                it.containsAll(requiredParamNames) && paramNames.containsAll(it)
            }

        return when (requestParams) {
            null -> checkNull()
            is ByPositionParams -> checkPos(requestParams)
            is ByNameParams -> checkNamed(requestParams)
        }
    }

    override fun handleRequest(httpRequest: HttpServletRequest, httpResponse: HttpServletResponse) {
        val jsonRequest = httpRequest.getAttribute(REQ_ATTR_NAME) as Request? ?:
            throw IllegalStateException("Can not find $REQ_ATTR_NAME in request attributes")

        val args = convertToArguments(jsonRequest.params)

        try {
            val result = method.invoke(bean, *args)
            val jsonResponse = Response.success(result, jsonRequest.id)

            JsonRpcResponse(jsonResponse).respondTo(httpResponse)
        }
        catch (e: InvocationTargetException) {
            val targetEx = e.targetException
            val error =
                if (targetEx is JsonRpcException) targetEx.error
                else ErrorCode.INTERNAL_ERROR.toError(targetEx.message)

            val jsonResponse = Response.error(error, jsonRequest.id)
            JsonRpcResponse(jsonResponse).respondTo(httpResponse)
        }
    }

    private fun convertToArguments(params: Params?): Array<Any?> =
        when (params) {
            null -> arrayOf()
            is ByNameParams -> convertMapToArguments(params.params)
            is ByPositionParams -> convertArrayToArguments(params.params)
        }

    private fun convertArrayToArguments(params: List<JsonNode>) =
        convertToArguments(params.asSequence() + sequence {
            while (true) yield(null)
        })

    private fun convertMapToArguments(params: Map<String, JsonNode>) =
        convertToArguments(paramNames.asSequence().map {
            params[it]
        })

    private fun convertToArguments(params: Sequence<JsonNode?>): Array<Any?> =
        params
            .zip(paramByName.values.asSequence())
            .map { (node, param) ->
                node?.let {
                    Json.parseNode(it, param.type)
                } ?: param.defaultValue?.let { Json.parse<Any?>(it, param.type) }
            }
            .toList()
            .toTypedArray()
}
