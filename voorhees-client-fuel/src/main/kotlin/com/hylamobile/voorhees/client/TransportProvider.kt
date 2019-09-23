package com.hylamobile.voorhees.client

import com.github.kittinunf.fuel.core.RequestExecutionOptions
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.hylamobile.voorhees.jsonrpc.Request
import com.hylamobile.voorhees.jsonrpc.jsonString
import java.nio.charset.Charset

class FuelTransportProvider : TransportProvider {

    override fun transport(serverConfig: ServerConfig): Transport =
        FuelTransport(serverConfig)
}

class FuelTransport(val serverConfig: ServerConfig) : Transport {

    override fun getResponseAsString(endpoint: String, request: Request): String =
        endpoint.httpPost()
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
