package com.hylamobile.voorhees.gradle

import com.hylamobile.voorhees.jsonrpc.JsonRpcException
import com.hylamobile.voorhees.server.annotation.DontExpose
import com.hylamobile.voorhees.server.annotation.JsonRpcService as ServerJsonRpcService
import com.hylamobile.voorhees.client.annotation.JsonRpcService as ClientJsonRpcService
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.annotation.AnnotationDescription
import net.bytebuddy.description.modifier.Visibility
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import java.io.File
import java.lang.reflect.*
import java.net.URL
import java.net.URLClassLoader

class ClientGenerator(
    private val packagesToScan: Array<String>,
    private val classPath: Array<URL>,
    private val genDir: File) {

    private val namingPattern = "%s.%s"

    private val classesToGenerate: MutableSet<Class<*>> = mutableSetOf()

    init {
        genDir.apply {
            if (!exists()) {
                mkdirs()
            }

            check(isDirectory) { "${genDir.canonicalPath} is not a directory" }
        }
    }

    fun generate() {
        val buildClassLoader = URLClassLoader(classPath, ClasspathHelper.contextClassLoader())
        val serviceClasses = Reflections(packagesToScan, buildClassLoader)
            .getTypesAnnotatedWith(ServerJsonRpcService::class.java)

        for (serviceClass in serviceClasses) {
            val location = serviceClass.getAnnotation(ServerJsonRpcService::class.java).locations[0]
            val jsonRpcServiceAnno = AnnotationDescription.Builder
                .ofType(ClientJsonRpcService::class.java)
                .define("location", location)
                .build()

            var remoteInterface = ByteBuddy().makeInterface()
                .name(namingPattern.format(serviceClass.`package`.name, serviceClass.simpleName))
                .annotateType(jsonRpcServiceAnno)
                .merge(Visibility.PUBLIC)

            for (method in serviceClass.publicMethods) {
                if (method.isAnnotationPresent(DontExpose::class.java)) continue

                remoteInterface = remoteInterface
                    .defineMethod(method.name, method.genericReturnType, Visibility.PUBLIC)
                    .withParameters(*method.genericParameterTypes).withoutCode()

                gatherClassesFromMethod(method)
            }

            remoteInterface.make().saveIn(genDir)
        }

        val errorClasses = Reflections(packagesToScan, buildClassLoader)
            .getSubTypesOf(JsonRpcException::class.java)
        errorClasses.forEach(this::classesFrom)

        classesToGenerate.forEach {
            ByteBuddy().rebase(it).make().saveIn(genDir)
        }
    }

    private fun gatherClassesFromMethod(method: Method) {
        classesFrom(method.genericReturnType)
        method.genericParameterTypes.asSequence().forEach(this::classesFrom)
    }

    private fun classesFrom(type: Type?) {
        if (classesToGenerate.contains(type)) return

        when (type) {
            is Class<*> -> when {
                type.isArray -> classesFrom(type.componentType)
                packagesToScan.any { p -> type.name.startsWith(p) } -> {
                    classesToGenerate.add(type)
                    classesFrom(type.genericSuperclass)
                    type.genericInterfaces.forEach(this::classesFrom)
                    type.publicMethods.forEach(this::gatherClassesFromMethod)
                }
            }
            is GenericArrayType -> classesFrom(type.genericComponentType)
            is TypeVariable<*> -> {
                type.genericDeclaration.typeParameters.forEach(this::classesFrom)
                type.bounds.asSequence().forEach(this::classesFrom)
            }
        }
    }

    private val Class<*>.publicMethods
        get() = declaredMethods.filter { Modifier.isPublic(it.modifiers) }
}
