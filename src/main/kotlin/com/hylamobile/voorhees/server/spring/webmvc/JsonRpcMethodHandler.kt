package com.hylamobile.voorhees.server.spring.webmvc

import com.fasterxml.jackson.databind.JsonNode
import com.hylamobile.voorhees.server.annotations.Param
import com.hylamobile.voorhees.server.annotations.normalizedDefault
import com.hylamobile.voorhees.jsonrpc.*
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.core.DefaultParameterNameDiscoverer



class JsonRpcMethodHandler(private val bean: Any, private val method: Method) : JsonRpcHandler {

    companion object {
        const val REQ_ATTR_NAME = "jsonrpc:request"

        val parameterNameDiscoverer = DefaultParameterNameDiscoverer()

        data class MethodParameter(
            val name: String,
            val type: Class<*>,
            val defaultValue: String? = null)
    }

    private val paramByName: LinkedHashMap<String, MethodParameter> =
        method.parameters
            .zip(parameterNameDiscoverer.getParameterNames(method) ?: emptyArray())
            .map { (param, discoveredName) ->
                when (val anno = param.getAnnotation(Param::class.java)) {
                    null -> MethodParameter(discoveredName, param.type)
                    else -> {
                        val paramName = anno.name.ifEmpty { discoveredName }
                        val defaultValue = anno.defaultValue.normalizedDefault
                        MethodParameter(paramName, param.type, defaultValue)
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
        fun checkNull() = requiredParamNames.isEmpty()

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

    private fun convertToArguments(params: Params?): Array<Any?> {
        fun nulls() = sequence { while (true) yield(null) }

        val jsonValues = when (params) {
            null -> nulls()
            is ByNameParams -> paramNames.asSequence().map { params.params[it] }
            is ByPositionParams -> params.params.asSequence() + nulls()
        }

        fun JsonNode.parse(type: Class<*>) = Json.parseNode(this, type)
        fun String.parseJson(type: Class<*>) = Json.parse<Any?>(this, type)

        return jsonValues
            .zip(paramByName.values.asSequence())
            .map { (node, param) ->
                node?.parse(param.type) ?: param.defaultValue?.parseJson(param.type)
            }
            .toList()
            .toTypedArray()
    }
}
