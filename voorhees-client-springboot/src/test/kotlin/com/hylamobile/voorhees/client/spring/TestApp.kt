package com.hylamobile.voorhees.client.spring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@Configuration
open class TestApp {

    @Bean
    open fun userRestTemplate() = RestTemplate()
}
