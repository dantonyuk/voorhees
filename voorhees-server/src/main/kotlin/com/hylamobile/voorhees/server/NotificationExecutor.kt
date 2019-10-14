package com.hylamobile.voorhees.server

import com.hylamobile.voorhees.jsonrpc.Request

interface NotificationExecutor {

    fun execute(method: RemoteMethod, request: Request)
}
