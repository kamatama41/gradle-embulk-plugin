package com.github.kamatama41.gradle.embulk

import org.gradle.api.Project
import java.io.File

open class EmbulkExtension(project: Project) {
    lateinit var version: String
    lateinit var category: String
    lateinit var name: String
    lateinit var authors: Array<String>
    lateinit var email: String
    var summary: String? = null
    var description: String? = null
    var licenses = arrayOf("MIT")
    var homepage = ""
    var jrubyVersion = "1.7.19" // TODO: update (currently v9.1.x)

    var checkstyleVersion = "6.14.1"
    var checkstyleConfig : File? = null
    var checkstyleDefaultConfig : File? = null
    var checkstyleIgnoreFailures = true

    var workDir = project.file("${project.projectDir.absolutePath}/.gradle/embulk")
    val binFile get() = File("${workDir.absolutePath}/$version/embulk")
    var configYaml = "config.yml"
    var outputYaml = "output.yml"

    val embulkCategory get() = when(category) {
        "file-input" -> "input"
        "file-output" -> "output"
        else -> category
    }
}
