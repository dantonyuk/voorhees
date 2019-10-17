package com.hylamobile.voorhees.client.spring.config

import com.hylamobile.voorhees.jsonrpc.JsonRpcException
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AssignableTypeFilter
import java.lang.reflect.Modifier

class JsonRpcErrorScanner : ClassPathScanningCandidateComponentProvider(false) {

    init {
        addIncludeFilter(AssignableTypeFilter(JsonRpcException::class.java))
    }

    override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition): Boolean {
        return beanDefinition.metadata.run {
            val codeField = try {
                Class.forName(className).getDeclaredField("CODE")
            }
            catch (ex: NoSuchFieldException) {
                return false
            }

            isIndependent && isConcrete && Modifier.isStatic(codeField.modifiers) && codeField.type == Int::class.java
        }
    }
}
