package com.hylamobile.voorhees.util

sealed class Option<T> {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T> none(): Option<T> = None as Option<T>
        fun <T> some(value: T): Option<T> = Some(value)
    }

    val isEmpty: Boolean
        get() = this == None

    abstract val getOrNull: T?

    fun <R> map(transform: (T) -> R): Option<R> =
        when (this) {
            is None -> none()
            is Some -> some(transform(value))
        }
}

object None : Option<Any>() {
    override val getOrNull: Nothing?
        get() = null
}

data class Some<T>(val value: T) : Option<T>() {
    override val getOrNull
        get() = value
}
