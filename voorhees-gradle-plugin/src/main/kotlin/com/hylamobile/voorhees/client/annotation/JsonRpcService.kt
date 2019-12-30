package com.hylamobile.voorhees.client.annotation

/**
 * Just a fake annotation to not depend on voorhees-client
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class JsonRpcService(
    val location: String,
    val prefix: String)
