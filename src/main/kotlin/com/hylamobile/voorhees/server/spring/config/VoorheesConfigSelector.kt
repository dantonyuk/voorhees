package com.hylamobile.voorhees.server.spring.config

import org.springframework.context.annotation.ImportSelector
import org.springframework.core.type.AnnotationMetadata

class VoorheesConfigSelector : ImportSelector {

    override fun selectImports(importingClassMetadata: AnnotationMetadata): Array<String> =
        arrayOf(VoorheesAutoConfiguration::class.java.name)
}
