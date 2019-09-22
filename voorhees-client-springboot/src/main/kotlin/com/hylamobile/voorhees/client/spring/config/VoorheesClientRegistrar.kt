package com.hylamobile.voorhees.client.spring.config

import com.hylamobile.voorhees.client.JsonRpcClient
import org.springframework.beans.factory.BeanClassLoaderAware
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.env.Environment
import org.springframework.core.type.AnnotationMetadata
import org.springframework.util.ClassUtils

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

    private fun ifConfigPresents(block: (VoorheesClientProperties) -> Unit) {
        val binder = Binder.get(environment).bind("voorhees.client", VoorheesClientProperties::class.java)
        binder.ifBound(block)
    }

    private class Registrar(
        val clientConfig: VoorheesClientProperties,
        val registry: BeanDefinitionRegistry,
        val beanClassLoader: ClassLoader) {

        fun registerJsonRpcClients() {
            clientConfig.services.forEach { (service, info) ->
                val beanDef = BeanDefinitionBuilder
                    .genericBeanDefinition(JsonRpcClient::class.java)
                    .addConstructorArgValue(info.endpoint)
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
                .genericBeanDefinition(JsonRpcClient::class.java)
                .addConstructorArgValue(beanClass)
                .setFactoryMethodOnBean("getService", "${service.uniform}JsonRpcClient")
                .beanDefinition
            registry.registerBeanDefinition(beanName, serviceBeanDef)
        }

        private val String.uniform
            get() = """[-_](\w)""".toRegex().replace(this) {
                it.value.substring(1).toUpperCase()
            }
    }
}
