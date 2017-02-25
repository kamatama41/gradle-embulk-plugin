package com.github.kamatama41.gradle.embulk

import org.gradle.api.DefaultTask
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertTrue
import org.junit.Test

class EmbulkPluginTest {
    @Test fun dummyTest() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.github.kamatama41.embulk")

        val embulk = project.tasks.findByName("embulk") as DefaultTask
        assertTrue("embulk is a EmbulkPlugin.EmbulkTask", embulk is EmbulkPlugin.EmbulkTask)
    }
}
