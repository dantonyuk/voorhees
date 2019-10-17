package com.hylamobile.voorhees.client.spring.config

import com.hylamobile.voorhees.client.JsonRpcClient
import com.hylamobile.voorhees.client.ServerConfig
import com.hylamobile.voorhees.jsonrpc.JsonRpcException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.web.client.RestTemplate

class SpringJsonRpcClient(
    private val basePackage: String,
    props: VoorheesProperties.ClientProperties) :
    ApplicationContextAware {

    private lateinit var appContext: ApplicationContext

    private val registeredErrors by lazy {
        JsonRpcErrorScanner().findCandidateComponents(basePackage).asSequence()
            .filter { beanDef ->
                props.targets.isEmpty() || props.targets.any {
                    p -> beanDef.beanClassName?.startsWith(p) ?: false
                }
            }
            .map { beanDef ->
                @Suppress("UNCHECKED_CAST")
                (Class.forName(beanDef.beanClassName) as Class<out JsonRpcException>)
            }
    }

    private val jsonRpcClient: JsonRpcClient by lazy {
        val serverConfig = ServerConfig(props.endpoint)
        val restTemplateName = props.restTemplate
        (if (restTemplateName == null)
            JsonRpcClient.of(serverConfig)
        else {
            JsonRpcClient { location ->
                val restTemplate = appContext.getBean(restTemplateName, RestTemplate::class.java)
                RestTemplateTransport(restTemplate, serverConfig.withLocation(location))
            }
        }).apply {
            registeredErrors.forEach { errorClass -> registerException(errorClass) }
        }
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.appContext = applicationContext
    }

    @Suppress("UNUSED")
    fun <T> getService(type: Class<T>): T =
        jsonRpcClient.getService(type)
}
