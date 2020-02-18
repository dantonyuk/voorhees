package com.hylamobile.voorhees.client.annotation

/**
 * Just a fake annotation to not depend on voorhees-client
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class JsonRpcMethod(
    val name: String = "")
