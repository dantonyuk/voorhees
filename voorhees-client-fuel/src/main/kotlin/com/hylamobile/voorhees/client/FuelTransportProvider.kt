package com.hylamobile.voorhees.client

class FuelTransportProvider : TransportProvider {

    override fun transportGroup(serverConfig: ServerConfig): TransportGroup =
        FuelTransportGroup(serverConfig)
}
