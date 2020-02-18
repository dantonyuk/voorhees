package com.hylamobile.voorhees.server.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class JsonRpcMethod(
    val name: String = "")
