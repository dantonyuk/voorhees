plugins {
    kotlin("jvm") version "1.3.50"
    id("org.jetbrains.dokka") version "0.9.17"
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
    }

    group = "com.hylamobile"
    version = "1.0.0"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.dokka")

    version = "1.0.0"

    tasks.dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/javadoc"
    }
}
