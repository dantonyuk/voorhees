package com.hylamobile.voorhees.gradle

import com.hylamobile.voorhees.gradle.Reflection.publicMethods
import java.lang.reflect.*

class TypeCollector(private val packagesToScan: Array<String>) {

    val collectedClasses: MutableSet<Class<*>> = mutableSetOf()

    private val alreadyConsidered = mutableSetOf<Type>()

    fun collect(method: Method) {
        collect(method.genericReturnType)
        method.genericParameterTypes.asSequence().forEach(this::collect)
    }

    fun collect(type: Type?) {
        if (type == null || !alreadyConsidered.add(type)) return
        if (collectedClasses.contains(type)) return

        when (type) {
            is Class<*> -> when {
                type.isArray -> collect(type.componentType)
                packagesToScan.any { p -> type.name.startsWith(p) } -> {
                    collectedClasses.add(type)
                    collect(type.genericSuperclass)
                    type.genericInterfaces.forEach(this::collect)
                    type.publicMethods.forEach(this::collect)
                }
            }
            is GenericArrayType -> collect(type.genericComponentType)
            is ParameterizedType -> {
                collect(type.rawType)
                type.actualTypeArguments.forEach(this::collect)
            }
            is TypeVariable<*> -> {
                type.genericDeclaration.typeParameters.forEach(this::collect)
                type.bounds.asSequence().forEach(this::collect)
            }
        }
    }
}
