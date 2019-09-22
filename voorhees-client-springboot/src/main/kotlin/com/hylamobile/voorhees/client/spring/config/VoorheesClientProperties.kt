package com.hylamobile.voorhees.client.spring.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "voorhees.client")
class VoorheesClientProperties {
    var basePackage: String = ""
    var services: Map<String, ServiceProperties> = mutableMapOf()

    open class ServiceProperties {
        var endpoint: String = ""
        var targets: Array<String> = arrayOf()
    }
}
