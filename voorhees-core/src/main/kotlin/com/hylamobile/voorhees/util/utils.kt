package com.hylamobile.voorhees.util

/**
 * Combines two parts of URI path to one path.
 *
 * Regardless of slash on the end of left part or on the beginning of the right part
 * there is going to be the only slash between these parts. E.g.:
 *
 * ```
 * uriCombine("left", "right") == "left/right"
 * uriCombine("left/", "right") == "left/right"
 * uriCombine("left", "/right") == "left/right"
 * uriCombine("left/", "/right") == "left/right"
 * ```
 */
fun uriCombine(left: String, right: String): String {
    val normalizedLeft = left + if (left.endsWith("/")) "" else "/"
    val normalizedRight = if (right.startsWith("/")) right.substring(1) else right
    return normalizedLeft + normalizedRight
}
