package com.hylamobile.voorhees.client

import com.github.kittinunf.fuel.core.RequestExecutionOptions
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.hylamobile.voorhees.jsonrpc.Request
import com.hylamobile.voorhees.jsonrpc.jsonString
import java.nio.charset.Charset

class FuelTransport(serverConfig: ServerConfig) : Transport(serverConfig) {

    override fun getResponseAsString(request: Request): String =
        serverConfig.url.httpPost()
            .apply { updateOptions(executionOptions) }
            .jsonBody(request.jsonString)
            .response()
            .third
            .get().toString(Charset.forName("UTF-8"))

    private fun updateOptions(options: RequestExecutionOptions) {
        serverConfig.connectTimeout?.also { options.timeoutInMillisecond = it }
        serverConfig.readTimeout?.also { options.timeoutReadInMillisecond = it }
    }
}
