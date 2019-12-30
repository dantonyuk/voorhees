package com.hylamobile.voorhees.server

import com.hylamobile.voorhees.server.reflect.ParameterNameDiscoverer

class RemoteConfig(
    val parameterNameDiscoverer: ParameterNameDiscoverer,
    val notificationExecutor: NotificationExecutor,
    val prefix: String = "") {

    fun withPrefix(newPrefix: String) = when (newPrefix) {
        prefix -> this
        else -> RemoteConfig(parameterNameDiscoverer, notificationExecutor, newPrefix)
    }
}
