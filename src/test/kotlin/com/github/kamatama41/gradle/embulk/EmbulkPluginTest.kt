package com.github.kamatama41.gradle.embulk

import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class EmbulkPluginTest {
    val project: Project by lazy {
        val tmpDir = Files.createTempDirectory("gradle-embulk-plugin").toFile()
        Git.init(tmpDir)
        val project = ProjectBuilder.builder().withProjectDir(tmpDir).build()
        project.pluginManager.apply("com.github.kamatama41.embulk")
        project
    }

    @Test fun classpath() {
        val classpath = project.tasks.findByName("classpath")
        assertTrue("classpath is a Copy task", classpath is Copy)
    }
}
