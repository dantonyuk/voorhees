package com.hylamobile.voorhees.server.annotation

/**
 * Just a fake annotation to not depend on voorhees-server
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class JsonRpcMethod(
    val name: String = "")
