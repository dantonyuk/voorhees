package com.hylamobile.voorhees.client.spring.annotation

import com.hylamobile.voorhees.client.spring.config.VoorheesClientRegistrar
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Import(VoorheesClientRegistrar::class)
@Configuration
annotation class EnableVoorheesClient
