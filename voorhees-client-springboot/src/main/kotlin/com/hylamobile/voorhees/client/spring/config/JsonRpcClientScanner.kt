package com.hylamobile.voorhees.client.spring.config

import com.hylamobile.voorhees.client.annotation.JsonRpcService
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter

class JsonRpcClientScanner : ClassPathScanningCandidateComponentProvider(false) {

    init {
        addIncludeFilter(AnnotationTypeFilter(JsonRpcService::class.java))
    }

    override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition): Boolean {
        return beanDefinition.metadata.run {
            isIndependent && isInterface
        }
    }
}
