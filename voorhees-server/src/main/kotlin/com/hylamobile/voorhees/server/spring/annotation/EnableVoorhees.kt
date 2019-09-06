package com.hylamobile.voorhees.server.spring.annotation

import com.hylamobile.voorhees.server.spring.config.VoorheesConfigSelector
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Import(VoorheesConfigSelector::class)
@Configuration
annotation class EnableVoorhees
