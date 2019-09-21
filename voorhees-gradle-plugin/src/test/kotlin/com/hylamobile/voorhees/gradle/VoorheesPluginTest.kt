package com.hylamobile.voorhees.gradle

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class VoorheesPluginTest {

    @Rule
    @JvmField
    val testProjectDir = TemporaryFolder()

    @Before
    fun setup() {
        testProjectDir.newFile("settings.gradle").writeText("""
            rootProject.name = "voorhees-plugin-test"                
        """.trimIndent())

        testProjectDir.newFile("build.gradle").writeText("""
            plugins {
                id "java"
                id "maven-publish"
                id "voorhees"
            }
            
            group = "com.hylamobile"
            version = "0.0.1"

            voorhees {
                packagesToScan = ["com.hylamobile"]
                artifact = "voorhees-plugin-test-client"
            }
            
            dependencies {
                compile("com.hylamobile:voorhees-server:1.0.0")
            }
            
            repositories {
                jcenter()
                mavenLocal()
                maven {
                    url mavenRepoUrl
                    credentials {
                        username mavenRepoUsername
                        password mavenRepoPassword
                    }
                }
            }

            publishing {
                repositories {
                    maven {
                        url mavenRepoUrl
                        credentials {
                            username mavenRepoUsername
                            password mavenRepoPassword
                        }
                    }
                }
            }
            """.trimIndent())

        File("src/test/java").copyRecursively(testProjectDir.newFolder("src", "main", "java"))
        File(System.getProperty("user.home"), ".gradle/gradle.properties")
            .copyTo(File(testProjectDir.root, "gradle.properties"))
    }

    @Test
    fun `should generate bytecode`() {
        val task = "publishJsonRpcClient"
        val result = GradleRunner.create()
            .withArguments(task)
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withDebug(true)
            .build()
        assertEquals(SUCCESS, result.task(":$task")?.outcome)
    }
}
