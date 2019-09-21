package com.hylamobile.voorhees.gradle

import org.gradle.api.Project
import org.gradle.api.internal.provider.DefaultProvider
import org.gradle.api.provider.Property

open class VoorheesExtension(private val project: Project) {
    var packagesToScan: Array<String> = arrayOf("")
    val group = stringProperty { project.group.toString() }
    val artifact = stringProperty { project.name + "-client" }
    val version = stringProperty { project.version.toString() }

    @Suppress("UnstableApiUsage")
    private fun stringProperty(defaultValue: () -> String): Property<String> =
        project.objects.property(String::class.java).convention(DefaultProvider(defaultValue))
}
