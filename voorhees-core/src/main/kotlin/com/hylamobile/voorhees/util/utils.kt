package com.hylamobile.voorhees.util

fun uriCombine(left: String, right: String): String {
    val normalizedLeft = left + if (left.endsWith("/")) "" else "/"
    val normalizedRight = if (right.startsWith("/")) right.substring(1) else right
    return normalizedLeft + normalizedRight
}
