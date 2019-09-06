import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.41"
    id("org.jetbrains.dokka") version "0.9.17"
}

tasks.withType<KotlinCompile> {
    kotlinOptions.javaParameters = true
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.kittinunf.fuel:fuel:2.1.0")
    implementation(project(":voorhees-core"))

    compileOnly("javax.servlet:javax.servlet-api:4.0.1")
    compileOnly("com.fasterxml.jackson.core:jackson-core:2.9.9")
    compileOnly("com.fasterxml.jackson.core:jackson-annotations:2.9.9")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.9.9")
    compileOnly("org.springframework:spring-webmvc:5.1.9.RELEASE")

    testImplementation("junit:junit:4.12")
    testImplementation("org.mock-server:mockserver-netty:5.3.0")
    testImplementation("com.fasterxml.jackson.core:jackson-core:2.9.9")
    testImplementation("com.fasterxml.jackson.core:jackson-annotations:2.9.9")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.9.9")
    testImplementation("org.springframework:spring-webmvc:5.1.9.RELEASE")
}

tasks.dokka {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}
