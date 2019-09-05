package com.hylamobile.voorhees.util

inline fun <reified T> Sequence<T>.toArray(size: Int): Array<T> {
    val iterator = iterator()
    return Array(size) { iterator.next() }
}
