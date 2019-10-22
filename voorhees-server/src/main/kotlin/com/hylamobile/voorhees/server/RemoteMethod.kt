package com.hylamobile.voorhees.server

import com.hylamobile.voorhees.jsonrpc.*
import com.hylamobile.voorhees.util.Option

interface RemoteMethod {

    val notificationExecutor: NotificationExecutor

    fun call(request: Request): Option<Response<*>> {
        val isNotification = request.id == null
        return if (isNotification) {
            notificationExecutor.execute(this, request)
            Option.none()
        } else {
            Option.some(invoke(request))
        }
    }

    fun invoke(request: Request): Response<*>
}
