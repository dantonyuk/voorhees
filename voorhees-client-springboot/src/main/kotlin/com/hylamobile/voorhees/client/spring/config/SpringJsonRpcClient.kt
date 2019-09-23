package com.hylamobile.voorhees.client.spring.config

import com.hylamobile.voorhees.client.JsonRpcClient
import com.hylamobile.voorhees.client.ServerConfig
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.web.client.RestTemplate

class SpringJsonRpcClient(props: VoorheesProperties.ClientProperties) :
    ApplicationContextAware {

    private lateinit var appContext: ApplicationContext

    private val jsonRpcClient: JsonRpcClient by lazy {
        val serverConfig = ServerConfig(props.endpoint)
        val restTemplateName = props.restTemplate
        return@lazy if (restTemplateName == null)
            JsonRpcClient.of(serverConfig)
        else
            JsonRpcClient { location ->
                val restTemplate = appContext.getBean(restTemplateName, RestTemplate::class.java)
                RestTemplateTransport(restTemplate, serverConfig.withLocation(location))
            }
        }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.appContext = applicationContext
    }

    @Suppress("UNUSED")
    fun <T> getService(type: Class<T>): T =
        jsonRpcClient.getService(type)
}
