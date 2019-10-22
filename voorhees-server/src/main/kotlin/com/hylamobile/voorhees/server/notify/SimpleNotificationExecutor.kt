package com.hylamobile.voorhees.server.notify

import com.hylamobile.voorhees.jsonrpc.Request
import com.hylamobile.voorhees.server.NotificationExecutor
import com.hylamobile.voorhees.server.RemoteMethod

@Suppress("UNUSED")
class SimpleNotificationExecutor : NotificationExecutor {

    override fun execute(method: RemoteMethod, request: Request) {
        method.invoke(request)
    }
}
