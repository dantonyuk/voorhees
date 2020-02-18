plugins {
    `java-library`
    id("java-gradle-plugin")
}

gradlePlugin {
    plugins {
        create("voorhees") {
            id = "voorhees"
            displayName = "Plugin for generating Voorhees clients"
            description = "Gradle plugin that helps to generate and publish client library for Voorhees services"
            implementationClass = "com.hylamobile.voorhees.gradle.VoorheesPlugin"
        }
    }
}

dependencies {
    compileOnly(gradleApi())
    api("org.reflections:reflections:0.9.11")
    api("net.bytebuddy:byte-buddy:1.10.8")

    testCompileOnly(gradleTestKit())
    testImplementation("junit:junit:4.12")

    testCompileOnly("org.projectlombok:lombok:1.18.8")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.8")
}

repositories {
    jcenter()
}

tasks.register("publishPlugin") {
    dependsOn("publishVoorheesPluginMarkerMavenPublicationToMavenRepository")
}
