package com.hylamobile.voorhees.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.AbstractCompile
import java.io.File

open class GenerateClientTask : DefaultTask() {

    private val extension = project.extensions.getByType(VoorheesExtension::class.java)

    @get:InputDirectory
    val buildDir: File
        get() = project.buildDir

    @get:OutputDirectory
    val genDir: File
        get() = File(buildDir, "classes/voorhees")

    @TaskAction
    fun apply() {
        val classPath = project.tasks
            .flatMap { if (it is AbstractCompile) listOf(it.destinationDir) else listOf() }
            .map { it.toURI().toURL() }
            .toTypedArray()

        ClientGenerator(extension.packageToScan, classPath, genDir).generate()
    }
}
