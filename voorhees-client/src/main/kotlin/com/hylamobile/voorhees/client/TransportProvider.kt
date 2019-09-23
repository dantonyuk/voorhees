package com.hylamobile.voorhees.client

typealias TransportGroup = (location: String) -> Transport

interface TransportProvider {

    fun transportGroup(serverConfig: ServerConfig): TransportGroup
}

