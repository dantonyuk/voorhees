plugins {
    kotlin("jvm") version "1.3.50"
    id("org.jetbrains.dokka") version "0.9.18"
    `maven-publish`
    signing
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
    }

    group = "com.hylamobile"
    version = "2.0.0-RC1-SNAPSHOT"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "org.gradle.signing")

    version = "2.0.0-RC1-SNAPSHOT"

    tasks.dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
        includes = listOf("src/main/doc/dokka/packages.md")
    }

    val dokkaJar by tasks.creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles Kotlin docs with Dokka"
        archiveClassifier.set("javadoc")
        from(tasks.dokka)
        dependsOn(tasks.dokka)
    }

    val sourcesJar by tasks.creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    val mavenRepoReleaseUrl: String by project
    val mavenRepoSnapshotUrl: String by project
    val mavenRepoUsername: String by project
    val mavenRepoPassword: String by project
    val mavenRepoUrl = if (project.version.toString().endsWith("SNAPSHOT")) mavenRepoSnapshotUrl else mavenRepoReleaseUrl

    publishing {
        publications {
            create<MavenPublication>("maven") {
                artifactId = project.name
                from(components["java"])
                artifact(sourcesJar)
                artifact(dokkaJar)
            }
        }
        repositories {
            maven {
                credentials {
                    username = mavenRepoUsername
                    password = mavenRepoPassword
                }
                url = uri(mavenRepoUrl)
            }
        }
    }
}
