package com.hylamobile.voorhees.client

class FuelTransportGroup (val serverConfig: ServerConfig) : TransportGroup {

    override fun transport(location: String): Transport =
        FuelTransport(serverConfig.withLocation(location))
}
