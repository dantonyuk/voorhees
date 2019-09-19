package com.hylamobile.voorhees.gradle

import org.gradle.api.Plugin
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.api.internal.artifacts.ArtifactAttributes
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.jvm.tasks.Jar
import java.io.File
import javax.inject.Inject
import org.gradle.api.plugins.internal.JavaConfigurationVariantMapping
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication

const val CLIENT_NAME = "JsonRpcClient"

private fun capitalize(s: String) =
    s[0].toUpperCase() + s.substring(1)

class VoorheesPlugin @Inject constructor(
    private val softwareComponentFactory: SoftwareComponentFactory) : Plugin<ProjectInternal> {

    override fun apply(project: ProjectInternal) {
        val extension = project.extensions.create("voorhees", VoorheesExtension::class.java, project)

        val generateTask = project.tasks.register("generate${capitalize(CLIENT_NAME)}", GenerateClientTask::class.java) { task ->
            task.dependsOn("classes")
            task.group = "jsonrpc"
            task.description = "Generate JSON RPC client classes"
        }

        val jarTask = project.tasks.register("jar${capitalize(CLIENT_NAME)}", Jar::class.java) { task ->
            task.dependsOn(generateTask)
            task.group = "jsonrpc"
            task.description = "Build a jar out of JSON RPC client classes"

            task.archiveFileName.set("${extension.artifact.get()}-${extension.version.get()}.jar")
            task.destinationDirectory.set(File(project.buildDir, "libs"))

            task.from(generateTask.get().genDir)
        }

        if (project.pluginManager.hasPlugin("maven-publish")) {
            val library = softwareComponentFactory.adhoc("${CLIENT_NAME}Library")
            val elements = project.configurations.create("jsonRpcElements") { conf ->
                conf.dependencies.add(
                    DefaultExternalModuleDependency("com.hylamobile", "voorhees-client", "1.0.0"))
                conf.outgoing.apply {
                    artifacts.add(LazyPublishArtifact(jarTask))
                    attributes.attribute(ArtifactAttributes.ARTIFACT_FORMAT, ArtifactTypeDefinition.JAR_TYPE)
                }
            }

            library.addVariantsFromConfiguration(
                elements, JavaConfigurationVariantMapping("compile", false))

            project.components.add(library)

            project.plugins.withId("maven-publish") {
                val publishing = project.extensions.getByType(PublishingExtension::class.java)
                publishing.publications.create(CLIENT_NAME, MavenPublication::class.java) { publication ->
                    check(publication is DefaultMavenPublication)

                    publication.from(library)
                    publication.mavenProjectIdentity.apply {
                        artifactId.set(extension.artifact)
                        groupId.set(extension.group)
                        version.set(extension.version)
                    }
                }

                project.tasks.register("publish${capitalize(CLIENT_NAME)}") { task ->
                    task.dependsOn("publish${capitalize(CLIENT_NAME)}PublicationToMavenRepository")
                    task.group = "jsonrpc"
                    task.description = "Publish JSON RPC client to Maven repository"
                }
            }
        }
    }
}
