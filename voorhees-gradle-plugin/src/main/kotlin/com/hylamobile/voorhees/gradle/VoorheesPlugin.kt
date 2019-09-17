package com.hylamobile.voorhees.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class VoorheesPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register("generateJsonRpcClient", GenerateClientTask::class.java) {
            task -> task.dependsOn("compileJava")
        }
    }
}
