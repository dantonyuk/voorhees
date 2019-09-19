package com.hylamobile.voorhees.server.annotation

/**
 * Just a fake annotation to not depend on voorhees-server
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class JsonRpcService(val locations: Array<String>)
