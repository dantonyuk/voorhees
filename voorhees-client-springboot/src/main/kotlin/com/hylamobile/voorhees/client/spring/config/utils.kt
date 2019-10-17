package com.hylamobile.voorhees.client.spring.config

val String.uniform
    get() = """[-_](\w)""".toRegex().replace(this) {
        it.value.substring(1).toUpperCase()
    }
