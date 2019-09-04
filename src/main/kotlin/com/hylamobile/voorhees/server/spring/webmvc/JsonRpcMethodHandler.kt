package com.hylamobile.voorhees.server.spring.webmvc

import com.fasterxml.jackson.databind.JsonNode
import com.hylamobile.voorhees.server.annotations.Param
import com.hylamobile.voorhees.server.annotations.normalizedDefault
import com.hylamobile.voorhees.jsonrpc.*
import com.hylamobile.voorhees.util.Option
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.core.DefaultParameterNameDiscoverer
import java.lang.reflect.Type
import kotlin.collections.LinkedHashMap


class JsonRpcMethodHandler(private val bean: Any, private val method: Method) : JsonRpcHandler {

    companion object {
        const val REQ_ATTR_NAME = "jsonrpc:request"

        val parameterNameDiscoverer = DefaultParameterNameDiscoverer()

        data class MethodParameter(
            val name: String,
            val type: Type,
            val defaultValue: Option<Any?> = Option.none())
    }

    private val paramByName: LinkedHashMap<String, MethodParameter> =
        method.parameters
            .zip(parameterNameDiscoverer.getParameterNames(method) ?: emptyArray())
            .map { (param, discoveredName) ->
                val type = param.parameterizedType
                when (val anno = param.getAnnotation(Param::class.java)) {
                    null -> MethodParameter(discoveredName, type)
                    else -> {
                        val paramName = anno.name.ifEmpty { discoveredName }
                        val defaultValue: Option<Any?> = anno.defaultValue.normalizedDefault
                            ?.run {
                                Option.some(
                                    try {
                                        Json.parse<Any?>(this, type)
                                    }
                                    catch (ex: java.io.IOException) {
                                        this
                                    })
                            } ?: Option.none()

                        MethodParameter(paramName, type, defaultValue)
                    }
                }
            }
            .map { it.name to it }
            .toMap(LinkedHashMap())

    private val parameters = paramByName.values
    private val paramNames = parameters.map { it.name }

    private val requiredParamNames =
        paramByName.values.filter { it.defaultValue.isEmpty }.map { it.name }

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
        catch (ex: InvocationTargetException) {
            ex.printStackTrace()
            val targetEx = ex.targetException
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

        fun JsonNode.parse(type: Type) = Json.parseNode(this, type)

        return jsonValues
            .zip(paramByName.values.asSequence())
            .map { (node, param) ->
                node?.parse(param.type) ?: param.defaultValue.getOrNull
            }
            .toList()
            .toTypedArray()
    }
}
