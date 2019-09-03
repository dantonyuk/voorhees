package com.hylamobile.voorhees.server.spring.config

import com.hylamobile.voorhees.server.spring.webmvc.JsonRpcHandlerMapping
import org.springframework.context.annotation.Bean

class VoorheesConfig {

    @Bean
    fun jsonRpcHandlerMapping() = JsonRpcHandlerMapping()
}
