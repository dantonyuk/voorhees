package com.hylamobile.voorhees.server.annotations

import org.springframework.web.bind.annotation.ValueConstants

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Param(
    val name: String = "",
    val defaultValue: String = ValueConstants.DEFAULT_NONE)

val String.normalizedDefault
    get() = when(this) {
        ValueConstants.DEFAULT_NONE -> null
        else ->this
    }
