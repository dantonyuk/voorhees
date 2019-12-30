package com.hylamobile.voorhees.gradle

import com.hylamobile.voorhees.jsonrpc.JsonRpcException
import com.hylamobile.voorhees.server.annotation.DontExpose
import com.hylamobile.voorhees.server.annotation.JsonRpcService as ServerJsonRpcService
import com.hylamobile.voorhees.client.annotation.JsonRpcService as ClientJsonRpcService
import com.hylamobile.voorhees.gradle.Reflection.publicMethods
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.annotation.AnnotationDescription
import net.bytebuddy.description.modifier.Visibility
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import java.io.File
import java.net.URL
import java.net.URLClassLoader

class ClientGenerator(
    private val packagesToScan: Array<String>,
    private val classPath: Array<URL>,
    private val genDir: File) {

    private val namingPattern = "%s.%s"

    private val typeCollector = TypeCollector(packagesToScan)

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
            val serverAnno = serviceClass.getAnnotation(ServerJsonRpcService::class.java)
            val jsonRpcServiceAnno = AnnotationDescription.Builder
                .ofType(ClientJsonRpcService::class.java)
                .define("location", serverAnno.locations[0])
                .define("prefix", serverAnno.prefix)
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

                typeCollector.collect(method)
            }

            remoteInterface.make().saveIn(genDir)
        }

        val errorClasses = Reflections(packagesToScan, buildClassLoader)
            .getSubTypesOf(JsonRpcException::class.java)
        errorClasses.forEach(typeCollector::collect)

        typeCollector.collectedClasses.forEach {
            ByteBuddy().rebase(it).make().saveIn(genDir)
        }
    }
}
