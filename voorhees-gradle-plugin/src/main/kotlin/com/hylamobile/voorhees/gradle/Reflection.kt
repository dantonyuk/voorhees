package com.hylamobile.voorhees.gradle

import java.lang.reflect.Modifier

object Reflection {

    val Class<*>.publicMethods
        get() = declaredMethods.filter { Modifier.isPublic(it.modifiers) }
}
