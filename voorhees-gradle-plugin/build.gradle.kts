plugins {
    `java-library`
    id("java-gradle-plugin")
}

gradlePlugin {
    plugins {
        create("voorheesPlugin") {
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
    api("net.bytebuddy:byte-buddy:1.10.1")

    testCompileOnly(gradleTestKit())
    testImplementation("junit:junit:4.12")
}

repositories {
    jcenter()
}
