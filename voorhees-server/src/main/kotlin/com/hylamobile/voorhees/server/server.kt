package com.hylamobile.voorhees.server

import com.hylamobile.voorhees.jsonrpc.ErrorCode

fun ErrorCode.toMethod(data: Any?) = RemoteError(toError(data))

