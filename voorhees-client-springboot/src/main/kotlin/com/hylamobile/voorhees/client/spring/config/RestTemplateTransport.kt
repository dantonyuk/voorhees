package com.hylamobile.voorhees.client.spring.config

import com.hylamobile.voorhees.client.ServerConfig
import com.hylamobile.voorhees.client.Transport
import com.hylamobile.voorhees.jsonrpc.Request
import com.hylamobile.voorhees.jsonrpc.jsonString
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate

class RestTemplateTransport(private val restTemplate: RestTemplate, serverConfig: ServerConfig) : Transport(serverConfig) {
    override fun getResponseAsString(request: Request): String {
        val httpHeaders = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val httpEntity = HttpEntity(request.jsonString, httpHeaders)

        return restTemplate.postForObject(
            this.serverConfig.url, httpEntity, String::class.java) ?: "null"
    }
}
