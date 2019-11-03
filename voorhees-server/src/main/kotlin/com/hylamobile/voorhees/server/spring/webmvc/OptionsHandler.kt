package com.hylamobile.voorhees.server.spring.webmvc

import org.springframework.web.HttpRequestHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class OptionsHandler : HttpRequestHandler {

    companion object {
        @JvmStatic
        val ALLOWED_METHODS = setOf("POST", "OPTIONS")
    }

    override fun handleRequest(request: HttpServletRequest, response: HttpServletResponse) {
        response.setHeader("Allow", "OPTIONS, POST")
        response.sendError(HttpServletResponse.SC_NO_CONTENT)
    }
}
