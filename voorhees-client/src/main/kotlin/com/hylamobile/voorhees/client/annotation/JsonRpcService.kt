package com.hylamobile.voorhees.client.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class JsonRpcService(
    val location: String,
    val prefix: String = "")
