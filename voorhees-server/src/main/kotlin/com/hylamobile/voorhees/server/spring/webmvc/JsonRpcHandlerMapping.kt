package com.hylamobile.voorhees.server.spring.webmvc

import com.hylamobile.voorhees.jsonrpc.ErrorCode
import com.hylamobile.voorhees.jsonrpc.Request
import com.hylamobile.voorhees.server.annotation.DontExpose
import com.hylamobile.voorhees.server.annotation.JsonRpcService
import com.hylamobile.voorhees.util.uriCombine
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.http.InvalidMediaTypeException
import org.springframework.http.MediaType
import org.springframework.web.servlet.handler.AbstractHandlerMapping
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest

typealias ServiceMethods = List<JsonRpcMethodHandler>
typealias ServiceDescriptor = Map<String, ServiceMethods>

class JsonRpcHandlerMapping : AbstractHandlerMapping() {

    @Value("\${spring.voorhees.server.handler-mapping.order:-2147483648}")
    private var _order: Int = Ordered.HIGHEST_PRECEDENCE

    @Value("\${spring.voorhees.server.api.prefix:}")
    private var apiPrefix: String = ""

    private lateinit var serviceDescriptors: Map<String, ServiceDescriptor>

    @PostConstruct
    fun init() {
        serviceDescriptors = (applicationContext ?: throw IllegalStateException("Should not be thrown"))
            .getBeansWithAnnotation(JsonRpcService::class.java)
            .values
            .flatMap { bean ->
                val clazz = bean.javaClass
                val handlerInfos = clazz.methods
                    .filter { it.getAnnotation(DontExpose::class.java) == null }
                    .groupBy { it.name }
                    .mapValues { it.value.map { m -> JsonRpcMethodHandler(bean, m) }}

                val jsonRpcAnno = clazz.getAnnotation(JsonRpcService::class.java)
                jsonRpcAnno.locations.map { loc -> uriCombine(apiPrefix, loc) to handlerInfos }
            }
            .toMap()
    }

    override fun getOrder(): Int = _order

    override fun getHandlerInternal(httpRequest: HttpServletRequest): Any? {
        fun contentType() =
            try { MediaType.valueOf(httpRequest.contentType) }
            catch (ex: InvalidMediaTypeException) { null }

        fun accept() = MediaType.parseMediaTypes(httpRequest.getHeader("Accept"))

        fun MediaType.compatibleWithJson() = isCompatibleWith(MediaType.APPLICATION_JSON)

        return when {
            httpRequest.method != "POST" -> null
            !(contentType()?.compatibleWithJson() ?: true) -> null
            accept().none { mediaType -> mediaType.compatibleWithJson() } -> null
            else -> findHandler(httpRequest)
        }
    }

    private fun findHandler(httpRequest: HttpServletRequest): JsonRpcHandler? {
        val serviceMethods = serviceDescriptors[httpRequest.realPath] ?: return null

        val jsonRequest: Request = httpRequest.jsonRequest

        val methods = serviceMethods[jsonRequest.method] ?:
            return ErrorCode.METHOD_NOT_FOUND.toHandler("Method ${jsonRequest.method} not found")

        val compatibleHandlers = methods
            .filter { method -> method.compatibleWith(jsonRequest.params) }
            .ifEmpty {
                return ErrorCode.INVALID_PARAMS.toHandler(
                    "Method ${jsonRequest.method} with arguments ${jsonRequest.params} not found")
            }

        return pickBest(compatibleHandlers, jsonRequest)
    }

    private fun pickBest(handlers: List<JsonRpcMethodHandler>, @Suppress("UNUSED_PARAMETER") jsonRequest: Request) = handlers[0]
}
