package com.hylamobile.voorhees.jsonrpc

/**
 * Just a fake class to not depend on voorhees-core
 */
data class Error(
    val code: Int,
    val message: String,
    val data: Any? = null)
