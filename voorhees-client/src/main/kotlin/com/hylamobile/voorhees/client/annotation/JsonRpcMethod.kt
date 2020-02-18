package com.hylamobile.voorhees.client.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class JsonRpcMethod(
    val name: String = "")
