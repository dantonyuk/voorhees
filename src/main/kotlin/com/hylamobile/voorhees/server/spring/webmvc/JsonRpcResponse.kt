package com.hylamobile.voorhees.server.spring.webmvc

import com.hylamobile.voorhees.jsonrpc.Json
import com.hylamobile.voorhees.jsonrpc.Response
import org.springframework.http.MediaType
import javax.servlet.http.HttpServletResponse

class JsonRpcResponse(private val jsonResponse: Response<*>) {

    fun respondTo(httpResponse: HttpServletResponse) {
        httpResponse.contentType = MediaType.APPLICATION_JSON_UTF8_VALUE
        httpResponse.characterEncoding = "UTF-8"
        httpResponse.writer.use { Json.writeResponse(jsonResponse, it) }
    }
}
