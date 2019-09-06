package com.hylamobile.voorhees.server.annotations

import com.hylamobile.voorhees.util.Option
import org.springframework.web.bind.annotation.ValueConstants.DEFAULT_NONE
import java.lang.reflect.Parameter

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Param(
    val name: String = "",
    val defaultValue: String = DEFAULT_NONE)

val Param.optionDefaultValue
    get() = if (defaultValue == DEFAULT_NONE) Option.none() else Option.some(defaultValue)

val Parameter.paramAnno: Param?
    get() = getAnnotation(Param::class.java)
