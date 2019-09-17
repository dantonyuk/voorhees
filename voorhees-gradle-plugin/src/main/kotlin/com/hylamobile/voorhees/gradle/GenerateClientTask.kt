package com.hylamobile.voorhees.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

class GenerateClientTask : DefaultTask() {

    @InputDirectory
    fun getBuildDir(): File = project.buildDir

    @OutputDirectory
    fun getGenDir(): File = project.buildDir

    @TaskAction
    fun apply() {

    }
}
