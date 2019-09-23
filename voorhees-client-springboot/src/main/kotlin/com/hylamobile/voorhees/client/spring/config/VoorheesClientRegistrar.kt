package com.hylamobile.voorhees.client.spring.config

import com.hylamobile.voorhees.client.JsonRpcClient
import com.hylamobile.voorhees.client.ServerConfig
import com.hylamobile.voorhees.client.Transport
import com.hylamobile.voorhees.client.TransportGroup
import com.hylamobile.voorhees.jsonrpc.Request
import com.hylamobile.voorhees.jsonrpc.jsonString
import org.springframework.beans.factory.BeanClassLoaderAware
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.env.Environment
import org.springframework.core.type.AnnotationMetadata
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.ClassUtils
import org.springframework.web.client.RestTemplate

class VoorheesClientRegistrar : ImportBeanDefinitionRegistrar, EnvironmentAware, BeanClassLoaderAware {

    private lateinit var environment: Environment
    private lateinit var beanClassLoader: ClassLoader

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun setBeanClassLoader(beanClassLoader: ClassLoader) {
        this.beanClassLoader = beanClassLoader
    }

    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        ifConfigPresents { config ->
            Registrar(config, registry, beanClassLoader).apply {
                registerJsonRpcClients()
                registerJsonRpcServices()
            }
        }
    }

    private fun ifConfigPresents(block: (VoorheesProperties) -> Unit) {
        val binder = Binder.get(environment).bind("voorhees.client", VoorheesProperties::class.java)
        binder.ifBound(block)
    }

    private class Registrar(
        val clientConfig: VoorheesProperties,
        val registry: BeanDefinitionRegistry,
        val beanClassLoader: ClassLoader) {

        fun registerJsonRpcClients() {
            clientConfig.services.forEach { (service, info) ->
                val beanDef = BeanDefinitionBuilder
                    .genericBeanDefinition(JsonRpcClient::class.java)
                    .addConstructorArgValue(transportGroup(info))
                    .setFactoryMethod("of")
                    .beanDefinition
                registry.registerBeanDefinition("${service.uniform}JsonRpcClient", beanDef)
            }
        }

        fun registerJsonRpcServices() {
            val scanner = JsonRpcClientScanner()
            scanner.findCandidateComponents(clientConfig.basePackage)
                .forEach { beanDef ->
                    registerJsonRpcService(beanDef.beanClassName)
                }
        }

        private fun registerJsonRpcService(beanClassName: String?) {
            checkNotNull(beanClassName)

            val service = clientConfig.services.asSequence()
                .filter { (service, info) ->
                    service != "default" &&
                        info.targets.any { p -> beanClassName.startsWith(p) }
                }
                .map { (service, _) -> service }
                .firstOrNull() ?: "default"

            val beanClass = ClassUtils.resolveClassName(beanClassName, beanClassLoader)
            val beanName = ClassUtils.getShortNameAsProperty(beanClass)
            val serviceBeanDef = BeanDefinitionBuilder
                .genericBeanDefinition()
                .addConstructorArgValue(beanClass)
                .setFactoryMethodOnBean("getService", "${service.uniform}JsonRpcClient")
                .beanDefinition
            registry.registerBeanDefinition(beanName, serviceBeanDef)
        }

        private val String.uniform
            get() = """[-_](\w)""".toRegex().replace(this) {
                it.value.substring(1).toUpperCase()
            }

        private fun transportGroup(props: VoorheesProperties.ClientProperties): TransportGroup {
            val serverConfig = ServerConfig(props.endpoint)
            return { location ->
                RestTemplateTransport(serverConfig.withLocation(location))
            }
        }
    }

    private class RestTemplateTransport(serverConfig: ServerConfig) : Transport(serverConfig) {
        override fun getResponseAsString(request: Request): String {
            val restTemplate = RestTemplate()
            val httpHeaders = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
            val httpEntity = HttpEntity(request.jsonString, httpHeaders)

            return restTemplate.postForObject(
                this.serverConfig.url, httpEntity, String::class.java) ?: "null"
        }
    }
}
