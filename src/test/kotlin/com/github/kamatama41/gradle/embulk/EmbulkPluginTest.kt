package com.github.kamatama41.gradle.embulk

import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

class EmbulkPluginTest {
    val project: Project by lazy {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.github.kamatama41.embulk")
        project
    }

    @Ignore @Test fun classpath() {
        val classpath = project.tasks.findByName("classpath")
        assertTrue("classpath is a Copy task", classpath is Copy)
    }
}
