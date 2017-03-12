package com.github.kamatama41.gradle.embulk

import org.gradle.api.Project
import java.io.File

open class EmbulkExtension(project: Project) {
    lateinit var category: String
    lateinit var name: String
    lateinit var embulkVersion: String
    var authors: Array<String> = emptyArray()
    var summary: String? = null
    var description: String? = null
    var email: String? = null
    var licenses = arrayOf("MIT")
    var homepage = ""
    var jrubyVersion = "1.7.19" // TODO: update (currently v9.1.x)

    var checkstyleVersion = "6.14.1"
    var checkstyleConfig = project.resources.text.fromFile("config/checkstyle/checkstyle.xml").asFile()
    var checkstyleDefaultConfig = project.resources.text.fromFile("config/checkstyle/default.xml").asFile()
    var checkstyleIgnoreFailures = true

    var workDir = project.file("${project.projectDir.absolutePath}/.gradle/embulk")
    val binFile get() = File("${workDir.absolutePath}/$embulkVersion/embulk")
    var configYaml = "config.yml"
    var outputYaml = "output.yml"
}
