package com.hylamobile.voorhees.server.spring.webmvc

import com.hylamobile.voorhees.jsonrpc.Response
import org.springframework.web.HttpRequestHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JsonRpcHandler(private val jsonResponse: Response<*>) : HttpRequestHandler {

    override fun handleRequest(httpRequest: HttpServletRequest, httpResponse: HttpServletResponse) {
        httpResponse.send(jsonResponse)
    }
}
