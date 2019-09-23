package com.hylamobile.voorhees.client.spring.config

class VoorheesProperties {
    var basePackage: String = ""
    var services: Map<String, ClientProperties> = mutableMapOf()

    open class ClientProperties {
        var endpoint: String = ""
        var targets: Array<String> = arrayOf()
        var restTemplate: String? = null
    }
}
