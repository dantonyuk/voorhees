package com.hylamobile.voorhees.server.spring.webmvc

import com.hylamobile.voorhees.jsonrpc.*
import com.hylamobile.voorhees.server.annotations.DontExpose
import com.hylamobile.voorhees.server.annotations.JsonRpcService
import org.springframework.core.Ordered
import org.springframework.http.InvalidMediaTypeException
import org.springframework.http.MediaType
import org.springframework.web.servlet.handler.AbstractHandlerMapping
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest

class JsonRpcHandlerMapping : AbstractHandlerMapping() {

    private lateinit var handlers: Map<String, Map<String, List<JsonRpcMethodHandler>>>

    @PostConstruct
    fun init() {
        handlers = (applicationContext ?: throw IllegalStateException("Should not be thrown"))
            .getBeansWithAnnotation(JsonRpcService::class.java)
            .values
            .flatMap { bean ->
                val clazz = bean.javaClass
                val handlerInfos = clazz.methods
                    .filter { it.getAnnotation(DontExpose::class.java) == null }
                    .groupBy { it.name }
                    .mapValues { it.value.map { m -> JsonRpcMethodHandler(bean, m) }}

                val jsonRpcAnno = clazz.getAnnotation(JsonRpcService::class.java)
                jsonRpcAnno.locations.map { it to handlerInfos }
            }
            .toMap()
    }

    override fun isContextRequired(): Boolean = true

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE

    override fun getHandlerInternal(httpRequest: HttpServletRequest): Any? {
        fun contentType() =
            try { MediaType.valueOf(httpRequest.contentType) }
            catch (ex: InvalidMediaTypeException) { null }

        fun accept() = MediaType.parseMediaTypes(httpRequest.getHeader("Accept"))

        fun MediaType.compatibleWithJson() = isCompatibleWith(MediaType.APPLICATION_JSON)

        return when {
            httpRequest.method != "POST" -> null
            !(contentType()?.compatibleWithJson() ?: true) -> null
            accept().none { it.compatibleWithJson() } -> null
            else -> findHandler(httpRequest)
        }
    }

    private fun findHandler(httpRequest: HttpServletRequest): JsonRpcHandler? {
        try {
            val service = handlers[httpRequest.realPath] ?: return null

            val jsonRequest: Request = httpRequest.reader.use(Json::readRequest)
            httpRequest.setAttribute(JsonRpcMethodHandler.REQ_ATTR_NAME, jsonRequest)

            val methods = service[jsonRequest.method]
                ?: throw MethodNotFoundException("Method ${jsonRequest.method} not found")

            val compatibleHandlers = methods.filter { it.compatibleWith(jsonRequest.params) }
            if (compatibleHandlers.isEmpty())
                throw InvalidParamsException("Method ${jsonRequest.method} with arguments ${jsonRequest.params} not found")

            return pickBest(compatibleHandlers, jsonRequest)
        }
        catch (ex: JsonRpcException) {
            return JsonRpcErrorHandler(ex)
        }
    }

    private fun pickBest(handlers: List<JsonRpcMethodHandler>, @Suppress("UNUSED_PARAMETER") jsonRequest: Request) = handlers[0]

    private val HttpServletRequest.realPath
        get() = pathInfo ?: requestURI.substring(contextPath.length)
}
