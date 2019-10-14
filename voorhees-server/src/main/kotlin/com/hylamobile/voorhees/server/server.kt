package com.hylamobile.voorhees.server

import com.hylamobile.voorhees.jsonrpc.ErrorCode

fun ErrorCode.toMethod(data: Any?, config: RemoteConfig) = RemoteError(toError(data), config)

