package com.hylamobile.voorhees.server

import com.hylamobile.voorhees.jsonrpc.*

interface RemoteMethod {

    fun call(request: Request): Response<*>
}
