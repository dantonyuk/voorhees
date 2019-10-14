package com.hylamobile.voorhees.server.spring.webmvc

import com.hylamobile.voorhees.jsonrpc.Request
import com.hylamobile.voorhees.server.RemoteConfig
import com.hylamobile.voorhees.server.RemoteServer
import com.hylamobile.voorhees.server.annotation.JsonRpcService
import com.hylamobile.voorhees.server.reflect.ParameterNameDiscoverer
import com.hylamobile.voorhees.util.uriCombine
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.DefaultParameterNameDiscoverer
import org.springframework.core.Ordered
import org.springframework.http.InvalidMediaTypeException
import org.springframework.http.MediaType
import org.springframework.web.servlet.handler.AbstractHandlerMapping
import java.lang.reflect.Method
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest

class JsonRpcHandlerMapping : AbstractHandlerMapping() {

    @Value("\${spring.voorhees.server.handler-mapping.order:-2147483648}")
    private var _order: Int = Ordered.HIGHEST_PRECEDENCE

    @Value("\${spring.voorhees.server.api.prefix:}")
    private var apiPrefix: String = ""

    private lateinit var remoteServers: Map<String, RemoteServer>

    @PostConstruct
    fun init() {
        val config = RemoteConfig(
            object : ParameterNameDiscoverer {
                private val discoverer = DefaultParameterNameDiscoverer()

                override fun parameterNames(method: Method): Array<String>? =
                    discoverer.getParameterNames(method)
            }
        )

        remoteServers = (applicationContext ?: throw IllegalStateException("Should not be thrown"))
            .getBeansWithAnnotation(JsonRpcService::class.java)
            .values
            .flatMap { bean ->
                val jsonRpcAnno = bean.javaClass.getAnnotation(JsonRpcService::class.java)
                jsonRpcAnno.locations.map { loc -> uriCombine(apiPrefix, loc) to RemoteServer(bean, config) }
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
        val remoteServer = remoteServers[httpRequest.realPath] ?: return null
        val jsonRequest: Request = httpRequest.jsonRequest
        val jsonResponse = remoteServer.call(jsonRequest)
        return JsonRpcHandler(jsonResponse)
    }
}
