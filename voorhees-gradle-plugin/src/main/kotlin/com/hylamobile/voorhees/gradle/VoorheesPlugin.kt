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

@Suppress("unused")
class VoorheesPlugin @Inject constructor(
    private val softwareComponentFactory: @Suppress("UnstableApiUsage") SoftwareComponentFactory
) : Plugin<ProjectInternal> {

    companion object {
        const val CLIENT_NAME = "jsonRpcClient"
        const val CAPITALIZED_CLIENT_NAME = "JsonRpcClient"
    }

    override fun apply(project: ProjectInternal) {
        val extension = project.extensions.create("voorhees", VoorheesExtension::class.java, project)

        val generateTask = project.tasks.register("generate$CAPITALIZED_CLIENT_NAME", GenerateClientTask::class.java) { task ->
            task.dependsOn("classes")
            task.group = "jsonrpc"
            task.description = "Generate JSON RPC client classes"
        }

        val jarTask = project.tasks.register("jar$CAPITALIZED_CLIENT_NAME", Jar::class.java) { task ->
            task.dependsOn(generateTask)
            task.group = "jsonrpc"
            task.description = "Build a jar out of JSON RPC client classes"

            task.archiveFileName.set("${extension.artifact.get()}-${extension.version.get()}.jar")
            task.destinationDirectory.set(File(project.buildDir, "libs"))

            task.from(generateTask.get().genDir)
        }

        project.plugins.withId("maven-publish") {
            val library = softwareComponentFactory.adhoc("${CLIENT_NAME}Library")
            val elements = project.configurations.create("jsonRpcElements") { conf ->
                conf.dependencies.add(
                    DefaultExternalModuleDependency("com.hylamobile", "voorhees-client", "2.0.0-RC3"))
                conf.outgoing.apply {
                    artifacts.add(LazyPublishArtifact(jarTask))
                    @Suppress("UnstableApiUsage")
                    attributes.attribute(ArtifactAttributes.ARTIFACT_FORMAT, ArtifactTypeDefinition.JAR_TYPE)
                }
            }

            library.addVariantsFromConfiguration(
                elements, JavaConfigurationVariantMapping("compile", false))

            project.components.add(library)

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

            project.tasks.register("publish$CAPITALIZED_CLIENT_NAME") { task ->
                task.dependsOn("publish${CAPITALIZED_CLIENT_NAME}PublicationToMavenRepository")
                task.group = "jsonrpc"
                task.description = "Publish JSON RPC client to Maven repository"
            }
        }
    }
}
