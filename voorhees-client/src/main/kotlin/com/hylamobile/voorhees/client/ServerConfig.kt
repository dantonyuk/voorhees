package com.hylamobile.voorhees.client

import com.hylamobile.voorhees.util.uriCombine

data class ServerConfig(
    val url: String,
    var connectTimeout: Int? = null,
    var readTimeout: Int? = null) {

    // for Java
    constructor(url: String) : this(url, null, null)

    fun withLocation(location: String) =
        copy(url = uriCombine(url, location))
}
