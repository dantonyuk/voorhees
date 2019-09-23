package com.hylamobile.voorhees.client

interface TransportProvider {

    fun transportGroup(serverConfig: ServerConfig): TransportGroup
}

