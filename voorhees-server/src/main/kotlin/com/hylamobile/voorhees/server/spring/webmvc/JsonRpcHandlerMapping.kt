package com.hylamobile.voorhees.server.spring.webmvc

import com.hylamobile.voorhees.jsonrpc.Request
import com.hylamobile.voorhees.server.NotificationExecutor
import com.hylamobile.voorhees.server.RemoteConfig
import com.hylamobile.voorhees.server.RemoteServer
import com.hylamobile.voorhees.server.annotation.JsonRpcService
import com.hylamobile.voorhees.server.reflect.ParameterNameDiscoverer
import com.hylamobile.voorhees.util.uriCombine
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.DefaultParameterNameDiscoverer
import org.springframework.core.Ordered
import org.springframework.http.MediaType
import org.springframework.web.servlet.handler.AbstractHandlerMapping
import java.lang.reflect.Method
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse.*

class JsonRpcHandlerMapping : AbstractHandlerMapping() {

    @Value("\${spring.voorhees.server.handler-mapping.order:-2147483648}")
    private var _order: Int = Ordered.HIGHEST_PRECEDENCE

    @Value("\${spring.voorhees.server.api.prefix:}")
    private var apiPrefix: String = ""

    @Value("\${spring.voorhees.server.notification-executor:" +
        "com.hylamobile.voorhees.server.notify.CommonForkJoinNotificationExecutor}")
    private var notificationExecutorClass: String = ""

    private lateinit var config: RemoteConfig

    private lateinit var remoteServers: MutableMap<String, RemoteServer>

    @PostConstruct
    fun init() {
        config = createConfig()
        initRemoteServers(config)
    }

    override fun getOrder(): Int = _order

    override fun getHandlerInternal(httpRequest: HttpServletRequest): Any? {
        val remoteServer = remoteServers[httpRequest.realPath] ?: return null

        return httpRequest.run {
            when {
                method != "POST" ->
                    ErrorHandler(SC_METHOD_NOT_ALLOWED)
                !(contentMediaType?.isJsonCompatible ?: true) ->
                    ErrorHandler(SC_UNSUPPORTED_MEDIA_TYPE)
                acceptedMediaTypes.none { it.isJsonCompatible } ->
                    ErrorHandler(SC_NOT_ACCEPTABLE, MediaType.APPLICATION_JSON_VALUE)
                else -> {
                    val jsonResponse = remoteServer.call(jsonRequest)
                    JsonRpcHandler(jsonResponse.getOrNull)
                }
            }
        }
        return when {
            httpRequest.method != "POST" ->
                ErrorHandler(SC_METHOD_NOT_ALLOWED)
            !(httpRequest.contentMediaType?.isJsonCompatible ?: true) ->
                ErrorHandler(SC_UNSUPPORTED_MEDIA_TYPE)
            httpRequest.acceptedMediaTypes.none { it.isJsonCompatible } ->
                ErrorHandler(SC_NOT_ACCEPTABLE, MediaType.APPLICATION_JSON_VALUE)
            else -> {
                val jsonRequest: Request = httpRequest.jsonRequest
                val jsonResponse = remoteServer.call(jsonRequest)
                JsonRpcHandler(jsonResponse.getOrNull)
            }
        }
    }

    fun registerService(service: Any, vararg locations: String) {
        locations.map { loc -> uriCombine(apiPrefix, loc) to RemoteServer(service, config) }
            .forEach { (loc, server) -> remoteServers[loc] = server }
    }

    private fun createConfig(): RemoteConfig =
        RemoteConfig(
            object : ParameterNameDiscoverer {
                private val discoverer = DefaultParameterNameDiscoverer()

                override fun parameterNames(method: Method): Array<String>? =
                    discoverer.getParameterNames(method)
            },
            Class.forName(notificationExecutorClass).newInstance() as NotificationExecutor)

    private fun initRemoteServers(config: RemoteConfig) {
        remoteServers = (applicationContext ?: throw IllegalStateException("Should not be thrown"))
            .getBeansWithAnnotation(JsonRpcService::class.java)
            .values
            .flatMap { bean ->
                val jsonRpcAnno = bean.javaClass.getAnnotation(JsonRpcService::class.java)
                jsonRpcAnno.locations.map { loc -> uriCombine(apiPrefix, loc) to RemoteServer(bean, config) }
            }
            .toMap().toMutableMap()
    }
}
