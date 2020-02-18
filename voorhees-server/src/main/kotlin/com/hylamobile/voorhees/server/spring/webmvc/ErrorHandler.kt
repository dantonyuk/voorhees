package com.hylamobile.voorhees.server.spring.webmvc

import org.springframework.web.HttpRequestHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ErrorHandler(
    private val statusCode: Int,
    private val body: String? = null) : HttpRequestHandler {

    override fun handleRequest(request: HttpServletRequest, response: HttpServletResponse) {
        if (body != null) {
            response.writer.print(body)
        }
        response.sendError(statusCode)
    }
}
