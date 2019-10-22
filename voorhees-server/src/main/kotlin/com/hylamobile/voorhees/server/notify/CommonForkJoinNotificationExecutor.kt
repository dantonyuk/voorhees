package com.hylamobile.voorhees.server.notify

import com.hylamobile.voorhees.jsonrpc.Request
import com.hylamobile.voorhees.server.NotificationExecutor
import com.hylamobile.voorhees.server.RemoteMethod
import java.util.concurrent.ForkJoinPool

@Suppress("UNUSED")
class CommonForkJoinNotificationExecutor : NotificationExecutor {

    override fun execute(method: RemoteMethod, request: Request) {
        ForkJoinPool.commonPool().execute {
            method.invoke(request)
        }
    }
}
